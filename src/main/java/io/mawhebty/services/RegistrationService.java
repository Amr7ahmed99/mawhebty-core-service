package io.mawhebty.services;

import io.mawhebty.dtos.FindOrCreateUserDto;
import io.mawhebty.dtos.requests.ConfirmRegistrationRequest;
import io.mawhebty.dtos.responses.*;
import io.mawhebty.enums.*;
import io.mawhebty.exceptions.*;
import io.mawhebty.models.*;
import io.mawhebty.repository.*;
import org.springframework.stereotype.Service;
import io.mawhebty.dtos.requests.DraftRegistrationRequest;
import io.mawhebty.dtos.requests.GenerateOtpRequest;
import io.mawhebty.dtos.requests.LoginRequest;
import io.mawhebty.services.validations.RegistrationValidationService;
import io.mawhebty.support.MessageService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class RegistrationService {
    private final JWTService jwtService;
    private final ModerationQueueService moderationQueueService;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserStatusRepository userStatusRepository;
    private final S3Service s3Service;
    private final UserProfileService userProfileService;
    private final PostRepository postRepository;
    private final PostTypeRepository postTypeRepository;
    private final PostStatusRepository postStatusRepository;
    private final PostVisibilityRepository visibilityRepository;
    private final MediaModerationRepository mediaModerationRepository;
    private final MediaModerationStatusRepository mediaModerationStatusRepository;
    private final OTPService otpService;
    private final RegistrationValidationService validationService;
    private final TalentFormValueService talentFormValueService;
    private final TalentCategoryRepository talentCategoryRepository;
    private final TalentSubCategoryRepository talentSubCategoryRepository;
    private final UserService userService;
    private final UserTypeRepository userTypeRepository;
    private final MessageService messageService; // Added

    @Transactional
    public LoginResponse login(LoginRequest request) {

        FindOrCreateUserDto result  = userService.findOrCreateByEmail(request.getEmail());

        OTPGenerationResponse otpGenRes = otpService.generateAndSendOtp(
                GenerateOtpRequest.builder()
                        .email(request.getEmail())
                        .build()
        );

        return LoginResponse.builder()
                .userId(result.getUser().getId())
                .isNewUser(result.getIsNew())
                .otpState(otpGenRes)
                .build();
    }

    @Transactional(rollbackOn = {Exception.class})
    public DraftResponse createDraftUser(DraftRegistrationRequest request) {
        String fullPhone = (request.getPrefixCode().replace("+","") + request.getPhone()).trim();
        if (!fullPhone.matches("^[0-9]{8,20}$")) {
            throw new BadDataException(
                    messageService.getMessage("invalid.phone.number",
                            new Object[]{fullPhone})
            );
        }

        // 1. Validate unique phone
        if (userRepository.findByFullPhone(fullPhone).isPresent()) {
            throw new PhoneAlreadyExistsException(
                    messageService.getMessage("phone.already.exists")
            );
        }

        // check if not verified
        User user = userRepository.findByEmailFetchStatus(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException(
                        messageService.getMessage("user.not.found.email",
                                new Object[]{request.getEmail()})
                ));
        if (!user.getIsVerified()) {
            throw new UserNotVerified(
                    messageService.getMessage("user.not.verified",
                            new Object[]{request.getEmail()})
            );
        }

        if(!UserStatusEnum.DRAFT.getName().equals(user.getStatus().getName())){
            throw new IllegalStateException(
                    messageService.getMessage("user.already.exists.status",
                            new Object[]{user.getStatus().getName()})
            );
        }

        UserRole role = userRoleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageService.getMessage("invalid.user.role",
                                new Object[]{request.getRoleId()})
                ));

        user.setPhoneNumber(request.getPhone());
        user.setCountryCode(request.getPrefixCode());
        user.setRole(role);

        // validate Category
        TalentCategory talentCategory = talentCategoryRepository.findByPartnerId(request.getCategoryId())
                .orElseThrow(() -> new BadDataException(
                        messageService.getMessage("talent.category.not.found",
                                new Object[]{request.getCategoryId()})
                ));

        TalentSubCategory talentSubCategory = null;
        if (request.getSubCategoryId() != null) {
            talentSubCategory = talentSubCategoryRepository.findByPartnerId(request.getSubCategoryId())
                    .orElseThrow(() -> new BadDataException(
                            messageService.getMessage("talent.subcategory.not.found",
                                    new Object[]{request.getSubCategoryId()})
                    ));
        }

        // 2. custom validations based on user role
        List<TalentCategoryFormKeys> requestCategoryFormData = new ArrayList<>();
        boolean isIndividualResearcher = false;
        if (role.getName().equals(UserRoleEnum.TALENT)) {
            validationService.validateTalentRegistration(request, requestCategoryFormData, talentCategory);
        }
        else {

            if (request.getUserTypeId() == null) {
                throw new BadDataException(
                        messageService.getMessage("user.type.required")
                );
            }

            UserType userType = userTypeRepository.findById(request.getUserTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            messageService.getMessage("invalid.user.type",
                                    new Object[]{request.getUserTypeId()})
                    ));

            user.setUserType(userType);

            isIndividualResearcher = userType.getType().equals(UserTypeEnum.INDIVIDUAL);

            validationService.validateResearcherRegistration(request, isIndividualResearcher);

            if (isIndividualResearcher) {
                UserStatus activeStatus = userStatusRepository.findByName(UserStatusEnum.ACTIVE.getName())
                        .orElseThrow(() -> new UserStatusNotFoundException(
                                messageService.getMessage("status.not.found",
                                        new Object[]{UserStatusEnum.ACTIVE.getName()})
                        ));

                user.setStatus(activeStatus);
                User savedUser = userRepository.save(user);

                IndividualResearcherProfile profile= (IndividualResearcherProfile) userProfileService.createResearcherProfile(
                        savedUser,request, talentCategory, talentSubCategory, true);

                String fullAccessToken = jwtService.generateToken(savedUser.getId(), savedUser.getEmail(),
                        savedUser.getRole(), "FULL_ACCESS", user.getStatus());
                String refreshToken = jwtService.generateRefreshToken(savedUser.getId(), savedUser.getEmail());

                TokenResponse tokenResponse = TokenResponse.builder()
                        .accessToken(fullAccessToken)
                        .refreshToken(refreshToken)
                        .tokenType("FULL_ACCESS")
                        .expiresIn(jwtService.getRemainingTokenTime(fullAccessToken))
                        .build();

                UserRegistrationResponseDto userRegistrationResponseDto= this.prepareUserRegistrationResponse(user, profile);
                userRegistrationResponseDto.setPermissions(jwtService.getFullPermissions(role));

                return DraftResponse.successWithoutFileAndWithToken(userRegistrationResponseDto, tokenResponse);
            }
        }

        // 3. Upload file to S3
        String fileUrl = s3Service.uploadFile(
                request.getFile(),
                role.getName().equals(UserRoleEnum.TALENT)? s3Service.getAwsRekognitionFolderInBucket(): s3Service.getAdminFolderInBucket()
        );

        User savedUser = userRepository.save(user);

        // 4. Create user profile
        Object profile = this.createUserProfile(request, role, savedUser, talentCategory, talentSubCategory);

        // 5. Add Talent Category Form Data
        boolean isTalentAndHasCategoryFormData = (profile instanceof TalentProfile) && !requestCategoryFormData.isEmpty();
        if (isTalentAndHasCategoryFormData) {
            talentFormValueService.saveTalentFormValues((TalentProfile) profile, request.getTalentCategoryFormMap(), requestCategoryFormData);
        }

        // 6. Create post with PENDING_MODERATION status
        createFirstPost(savedUser, fileUrl, talentCategory, talentSubCategory);

        return DraftResponse.success(this.prepareUserRegistrationResponse(savedUser, profile), fileUrl);
    }

    private Object createUserProfile(DraftRegistrationRequest request, UserRole role, User user,
                                     TalentCategory talentCategory, TalentSubCategory talentSubCategory) {
        if (role.getName().equals(UserRoleEnum.TALENT)) {
            return userProfileService.createTalentProfile(user, request, talentCategory, talentSubCategory);
        }

        return userProfileService.createResearcherProfile(user, request, talentCategory, talentSubCategory, false);
    }

    private void createFirstPost(User user, String mediaUrl, TalentCategory tc, TalentSubCategory tsc) {
        try {
            PostType registrationFileType = postTypeRepository.findByName(PostTypeEnum.REGISTRATION_FILE.getName())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            messageService.getMessage("post.type.not.found",
                                    new Object[]{PostTypeEnum.REGISTRATION_FILE.getName()})
                    ));

            PostStatus draftStatus = postStatusRepository.findByName(PostStatusEnum.DRAFT.getName())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            messageService.getMessage("post.status.not.found",
                                    new Object[]{PostStatusEnum.DRAFT.getName()})
                    ));

            PostVisibility privateVisibility = visibilityRepository.findByName(PostVisibilityEnum.PRIVATE.getName())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            messageService.getMessage("post.visibility.not.found",
                                    new Object[]{PostVisibilityEnum.PRIVATE.getName()})
                    ));

            String postTitle= messageService.getMessage("first.post.title");
            String postCaption= messageService.getMessage("first.post.caption");

            Post firstPost = Post.builder()
                    .ownerUser(user)
                    .type(registrationFileType)
                    .title(postTitle)
                    .caption(postCaption)
                    .mediaUrl(mediaUrl)
                    .visibility(privateVisibility)
                    .status(draftStatus)
                    .category(tc)
                    .subCategory(tsc)
                    .build();

            Post savedPost= postRepository.save(firstPost);

            if(savedPost.getId() == null){
                throw new Exception(
                        messageService.getMessage("post.not.created",
                                new Object[]{user.getId(), firstPost})
                );
            }

            log.info(messageService.getMessage("post.created.success"), firstPost);

        } catch (Exception e) {
            log.error(messageService.getMessage("post.creation.failed",
                    new Object[]{user.getId()}), e);
            throw new PostNotCreatedException(
                    messageService.getMessage("post.creation.failed.message",
                            new Object[]{e.getMessage()})
            );
        }
    }

    private MediaModeration createMediaModerationRecord() {
        MediaModerationStatus status= mediaModerationStatusRepository.findByName(MediaModerationStatusEnum.PENDING.getName())
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageService.getMessage("media.moderation.status.not.found",
                                new Object[]{MediaModerationStatusEnum.PENDING.getName()})
                ));

        return mediaModerationRepository.save(MediaModeration.builder()
                .status(status)
                .build());
    }

    @Transactional
    public ConfirmRegistrationResponse confirmRegistration(ConfirmRegistrationRequest request){
        User user = userRepository.findByEmailFetchStatusAndUserType(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException(
                        messageService.getMessage("user.not.found.email",
                                new Object[]{request.getEmail()})
                ));

        // user must be drafted and verified
        if (!UserStatusEnum.DRAFT.getName().equals(user.getStatus().getName()) || !user.getIsVerified()) {
            throw new IllegalStateException(
                    messageService.getMessage("user.not.ready.confirmation")
            );
        }

        UserStatus pendingModerationStatus = userStatusRepository.findByName(UserStatusEnum.PENDING_MODERATION.getName())
                .orElseThrow(() -> new UserStatusNotFoundException(
                        messageService.getMessage("status.not.found",
                                new Object[]{UserStatusEnum.PENDING_MODERATION.getName()})
                ));

        user.setStatus(pendingModerationStatus);
        userRepository.save(user);

        Post userRegisterationPost= postRepository.findByOwnerUserId(user.getId())
                .orElseThrow(()-> new ResourceNotFoundException(
                        messageService.getMessage("registration.file.not.found")
                ));

        boolean isTalent= UserRoleEnum.TALENT.equals(user.getRole().getName());

        boolean messageWasSent= false;

        if(isTalent){
            messageWasSent= this.moderationQueueService.sendFileForModeration(user.getId(), user.getRole().getId(),"REGISTRATION_MEDIA",
                    ModerationTypeEnum.USER_REGISTRATION.name(), userRegisterationPost.getId(), userRegisterationPost.getMediaUrl());
        }

        if (!isTalent && UserTypeEnum.COMPANY.equals(user.getUserType().getType())) {
            messageWasSent= this.moderationQueueService.sendFileForModeration(user.getId(), user.getRole().getId(),"REGISTRATION_DOCUMENT",
                    ModerationTypeEnum.USER_REGISTRATION.name(), userRegisterationPost.getId(), userRegisterationPost.getMediaUrl());
        }

        if(messageWasSent){
            PostStatus postPendingModerationStatus = postStatusRepository.findByName(PostStatusEnum.PENDING_MODERATION.getName())
                    .orElseThrow(() -> new UserStatusNotFoundException(
                            messageService.getMessage("status.not.found",
                                    new Object[]{PostStatusEnum.PENDING_MODERATION.getName()})
                    ));
            userRegisterationPost.setStatus(postPendingModerationStatus);
            userRegisterationPost.setMediaModeration(this.createMediaModerationRecord());
            postRepository.save(userRegisterationPost);
        }

        String limitedToken = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole(), "LIMITED_ACCESS", user.getStatus());
        String refreshToken = jwtService.generateRefreshToken(user.getId(), user.getEmail());

        Object profile= userProfileService.getUserProfile(user);

        TokenResponse tokenResponse= TokenResponse.builder()
                .accessToken(limitedToken)
                .refreshToken(refreshToken)
                .tokenType("LIMITED_ACCESS")
                .expiresIn(jwtService.getRemainingTokenTime(limitedToken))
                .build();

        UserRegistrationResponseDto userRegistrationResponseDto= this.prepareUserRegistrationResponse(user, profile);
        userRegistrationResponseDto.setPermissions(jwtService.getLimitedPermissions(user.getRole()));

        return ConfirmRegistrationResponse.builder()
                .message(messageService.getMessage("registration.pending.moderation"))
                .user(userRegistrationResponseDto)
                .tokenResponse(tokenResponse)
                .build();
    }

    private UserRegistrationResponseDto prepareUserRegistrationResponse(User user, Object profile){
        UserRegistrationResponseDto userRegistrationResponseDto = UserRegistrationResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .phone(user.getPhoneNumber())
                .countryCode(user.getCountryCode())
                .userStatus(user.getStatus() != null? user.getStatus().getId(): null)
                .userRole(user.getRole() != null? user.getRole().getId(): null)
                .userType(user.getUserType() != null? user.getUserType().getId(): null)
                .build();

        if (profile instanceof TalentProfile) {
            userRegistrationResponseDto.setFirstName(((TalentProfile) profile).getFirstName());
            userRegistrationResponseDto.setLastName(((TalentProfile) profile).getLastName());
            userRegistrationResponseDto.setImageUrl(((TalentProfile) profile).getProfilePicture());
        }else{
            if(profile instanceof IndividualResearcherProfile){
                userRegistrationResponseDto.setFirstName(((IndividualResearcherProfile) profile).getFirstName());
                userRegistrationResponseDto.setLastName(((IndividualResearcherProfile) profile).getLastName());
            }else{
                userRegistrationResponseDto.setCompanyName(((CompanyResearcherProfile) profile).getCompanyName());
                userRegistrationResponseDto.setContactPerson(((CompanyResearcherProfile) profile).getContactPerson());
                userRegistrationResponseDto.setCommercialRegNo(((CompanyResearcherProfile) profile).getCommercialRegNo());
            }
            userRegistrationResponseDto.setImageUrl(((ResearcherProfile) profile).getProfilePicture());
        }

        return userRegistrationResponseDto;
    }
}