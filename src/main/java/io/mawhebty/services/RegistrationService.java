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

    @Transactional
//    @RateLimited(attempts = 5, duration = 15)
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
            throw new BadDataException("Invalid full phone number: " + fullPhone));
        }

        // 1. Validate unique phone
        if (userRepository.existsByPhone(fullPhone)) {
            throw new PhoneAlreadyExistsException();
        }

        // check if not verified
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + request.getEmail()));
        if (!user.getIsVerified()) {
            throw new UserNotVerified("user not verified with email: " + request.getEmail());
        }

        UserRole role = userRoleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid user role with ID: " + request.getRoleId()));

        user.setPhone(fullPhone);
        user.setRole(role);

        // validate Category
        TalentCategory talentCategory = talentCategoryRepository.findByPartnerId(request.getCategoryId())
                .orElseThrow(() -> new BadDataException("Talent category not found with id: " + request.getCategoryId()));

        TalentSubCategory talentSubCategory= null;
        if(request.getSubCategoryId() != null){
            talentSubCategory= talentSubCategoryRepository.findByPartnerId(request.getSubCategoryId())
                    .orElseThrow(() -> new BadDataException("Talent subCategory not found with id: " + request.getSubCategoryId()));
        }

        // 2. custom validations based on user role
        List<TalentCategoryFormKeys> requestCategoryFormData = new ArrayList<>();
        if (request.getRoleId().equals(UserRoleEnum.TALENT.getId())) {
            validationService.validateTalentRegistration(request, requestCategoryFormData, talentCategory);
        } else {
            validationService.validateResearcherRegistration(request);
            boolean isIndividualResearcher= request.getUserTypeId().equals(UserTypeEnum.INDIVIDUAL.getId());
            if (isIndividualResearcher) {
                // Create User as individual researcher with profile
                UserStatus activeStatus = userStatusRepository.findByName(UserStatusEnum.ACTIVE.getName())
                        .orElseThrow(() -> new UserStatusNotFoundException(UserStatusEnum.ACTIVE.name() + " status not found"));

//            user.setEmail(request.getEmail()); // already verified by otp
//            user.setPassword(passwordEncoder.encode(request.getPassword()));
                user.setStatus(activeStatus);
                User savedUser = userRepository.save(user);

                userProfileService.createResearcherProfile(savedUser, request, talentCategory, talentSubCategory);

                // Assign full permissions to individual researcher
                // Generate FULL_ACCESS token
                String fullAccessToken = jwtService.generateToken(savedUser.getId(), savedUser.getEmail(), savedUser.getRole(), "FULL_ACCESS");
                String refreshToken = jwtService.generateRefreshToken(savedUser.getId(), savedUser.getEmail());

                TokenResponse tokenResponse= TokenResponse.builder()
                        .accessToken(fullAccessToken)
                        .refreshToken(refreshToken)
                        .tokenType("FULL_ACCESS")
                        .expiresIn(jwtService.getRemainingTokenTime(fullAccessToken))
                        .permissions(jwtService.getFullPermissions(savedUser.getRole()))
                        .userStatus(savedUser.getStatus().getName())
                        .userRole(savedUser.getRole().getName())
                        // .message("Registration pending moderation. You have limited access until approved.")
                        .build();

                // in case user is individual researcher, skip file handling and post creation and return
                return DraftResponse.successWithoutFileAndWithToken(savedUser.getId(), tokenResponse);
            }
        }

        boolean isCompony = request.getUserTypeId() != null && request.getUserTypeId().equals(UserTypeEnum.COMPANY.getId());

        // 3. Upload file to S3 in special bucket name depending on the file and user role
        String fileUrl = s3Service.uploadFile(
                request.getFile(),
                isCompony ? s3Service.getAdminFolderInBucket() : s3Service.getAwsRekognitionFolderInBucket()
        );

        // 4. Update user record
        UserStatus draftStatus = userStatusRepository.findByName(UserStatusEnum.DRAFT.getName())
                .orElseThrow(() -> new UserStatusNotFoundException("DRAFT status not found"));
        user.setRole(role);
        user.setStatus(draftStatus);
        User savedUser = userRepository.save(user);

        // 5. Create user profile
        Object profile = this.createUserProfile(request, request.getRoleId(), savedUser, talentCategory, talentSubCategory);

        // 6. Add Talent Category Form Data
        boolean isTalentAndHasCategoryFormData = (profile instanceof TalentProfile) && !requestCategoryFormData.isEmpty();
        if (isTalentAndHasCategoryFormData) {
            talentFormValueService.saveTalentFormValues((TalentProfile) profile, request.getTalentCategoryFormMap(), requestCategoryFormData);
        }

        // 7. Create post with PENDING_MODERATION status
        createFirstPost(savedUser, fileUrl);

        return DraftResponse.success(savedUser.getId(), fileUrl);
    }

    private Object createUserProfile(DraftRegistrationRequest request, Integer roleId, User user,
                                     TalentCategory talentCategory, TalentSubCategory talentSubCategory) {
        //Create role-specific profile
        if (roleId.equals(UserRoleEnum.TALENT.getId())) {
            return userProfileService.createTalentProfile(user, request, talentCategory, talentSubCategory);
        }

        return userProfileService.createResearcherProfile(user, request, talentCategory, talentSubCategory);
    }

    private void createFirstPost(User user, String mediaUrl) {
        try {
            // 1. Fetch required entities
            PostType registrationFileType = postTypeRepository.findByName(PostTypeEnum.REGISTRATION_FILE.getName())
                .orElseThrow(() -> new ResourceNotFoundException("REGISTRATION_FILE post type not found"));


            PostStatus draftStatus = postStatusRepository.findByName(PostStatusEnum.DRAFT.getName())
                .orElseThrow(() -> new ResourceNotFoundException("PENDING_MODERATION post status not found"));


            PostVisibility privateVisibility = visibilityRepository.findByName(PostVisibilityEnum.PRIVATE.getName())
                .orElseThrow(() -> new ResourceNotFoundException("PRIVATE visibility not found"));


            // 2. Determine post title based on user role
            String postTitle= "first post title";
            // if (user.getRole().getName().name().equals(UserRoleEnum.TALENT.name())) {
            //     TalentProfile talentProfile = talentProfileRepository.findByUserId(user.getId())
            //         // .orElseThrow(() -> new NotFoundException("Talent profile not found"));
            //         .orElseThrow(() -> new ResourceNotFoundException("Talent profile not found"));

            //     postTitle = talentProfile.getFullName() + " - Profile Video";
            // } else {
            //     ResearcherProfile researcherProfile = researcherProfileRepository.findByUserId(user.getId())
            //         // .orElseThrow(() -> new NotFoundException("Researcher profile not found"));
            //         .orElseThrow(() -> new Exception("Researcher profile not found"));

            //     String profileName = researcherProfile.getIsCompany() ?
            //         researcherProfile.getCompanyName() : researcherProfile.getContactPerson();
            //     postTitle = profileName + " - Profile Video";
            // }

            // 3. Create post caption based on user role
            String postCaption= "first post caption";
            // if (user.getRole().getName().equals(UserRoleEnum.TALENT.name())) {
            //     TalentProfile talentProfile = talentProfileRepository.findByUserId(user.getId())
            //         // .orElseThrow(() -> new NotFoundException("Talent profile not found"));
            //         .orElseThrow(() -> new Exception("Talent profile not found"));

            //     postCaption = "Welcome to my Talent Scout profile! " +
            //                  "I" + "specializing in " + talentProfile.getCategory().getName() + ". " +
            //                  talentProfile.getShortBio();
            // } else {
            //     postCaption = "Welcome to my Talent Scout profile! " +
            //                  "We're looking for amazing talents to collaborate with.";
            // }

            // 4. Create and save the post
            Post firstPost = Post.builder()
                .ownerUser(user)
                .type(registrationFileType)
                .title(postTitle)
                .caption(postCaption)
                .mediaUrl(mediaUrl)
                // .durationSeconds(120) // 2 minutes default for profile videos
                .visibility(privateVisibility)
                .status(draftStatus)
                .build();

            Post savedPost= postRepository.save(firstPost);

            if(savedPost.getId() == null){
                throw new Exception("post not created for user " + user.getId() +", post: "+ firstPost);
            }

            log.info("post created successfully: {}", firstPost);

        } catch (Exception e) {
            // Log the error but don't fail the entire registration
            log.error("Failed to create first post for user {}", user.getId(), e);
        }
    }

    private MediaModeration createMediaModerationRecord() {
        MediaModerationStatus status= mediaModerationStatusRepository.findByName(MediaModerationStatusEnum.PENDING.getName())
            .orElseThrow(() -> new ResourceNotFoundException("PENDING status not found"));

        MediaModeration moderation = mediaModerationRepository.save(MediaModeration.builder()
//            .post(post)
            .status(status)
            // .checkedAt(LocalDateTime.now())
            // .moderatorId() //Admin, AWS
            .build());
        
        return moderation;
    }

    @Transactional
    public ConfirmRegistrationResponse confirmRegistration(ConfirmRegistrationRequest request){
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found with email: "+ request.getEmail()));

        // user must be drafted and verified
        if (!user.getStatus().getName().equals(UserStatusEnum.DRAFT.getName()) || !user.getIsVerified()) {
            throw new IllegalStateException("User not ready for confirmation");
        }

        UserStatus pendingModerationStatus = userStatusRepository.findByName(UserStatusEnum.PENDING_MODERATION.getName())
            .orElseThrow(() -> new UserStatusNotFoundException("PENDING_MODERATION status not found"));

        user.setStatus(pendingModerationStatus);
        userRepository.save(user);

        // get registration post to extract media_url and send it to moderationQueue
        Post userRegisterationPost= postRepository.findByOwnerUserId(user.getId())
            .orElseThrow(()-> new ResourceNotFoundException("User registration file not found"));

        boolean isTalent= user.getRole().getId().equals(UserRoleEnum.TALENT.getId());

        boolean messageWasSent= false;

        // 2. Send to moderation queue
        // in case the role is talent, send it to SQS to be reviewed by awsRekognition
        if(isTalent){
            messageWasSent= this.moderationQueueService.sendFileForModeration(user.getId(), user.getRole().getId(),"REGISTRATION_MEDIA",
                    ModerationTypeEnum.USER_REGISTRATION.name(), userRegisterationPost.getId(), userRegisterationPost.getMediaUrl());
        }

        // in case the role is researcher and userType is company, send it to SQS to be reviewed by admin_dashboard
        if (!isTalent && user.getResearcherProfile().getUserType().getId().equals(UserTypeEnum.COMPANY.getId())) {
            messageWasSent= this.moderationQueueService.sendFileForModeration(user.getId(), user.getRole().getId(),"REGISTRATION_DOCUMENT",
                    ModerationTypeEnum.USER_REGISTRATION.name(), userRegisterationPost.getId(), userRegisterationPost.getMediaUrl());
        }

        // in case the message successfully delivered to sqs, update post status
        if(messageWasSent){
            PostStatus postPendingModerationStatus = postStatusRepository.findByName(PostStatusEnum.PENDING_MODERATION.getName())
                .orElseThrow(() -> new UserStatusNotFoundException("PENDING_MODERATION status not found"));
            userRegisterationPost.setStatus(postPendingModerationStatus);
            // create moderation record for this post
            userRegisterationPost.setMediaModeration(this.createMediaModerationRecord());
            postRepository.save(userRegisterationPost);

            // create moderation record for this post
//            this.createMediaModerationRecord(userRegisterationPost);
        }

        // 3. Generate LIMITED token
        String limitedToken = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole(), "LIMITED_ACCESS");
        String refreshToken = jwtService.generateRefreshToken(user.getId(), user.getEmail());


        TokenResponse tokenResponse= TokenResponse.builder()
                .accessToken(limitedToken)
                .refreshToken(refreshToken)
                .tokenType("LIMITED_ACCESS")
                .expiresIn(jwtService.getRemainingTokenTime(limitedToken))
                .permissions(jwtService.getLimitedPermissions(user.getRole()))
                .userStatus(user.getStatus().getName())
                .userRole(user.getRole().getName())
                // .message(".")
                .build();

        return ConfirmRegistrationResponse.builder()
                .message("Registration pending moderation. You have limited access until approved")
                .tokenResponse(tokenResponse)
                .build();
    }
}
