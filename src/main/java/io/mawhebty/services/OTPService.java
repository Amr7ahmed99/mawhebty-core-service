package io.mawhebty.services;

import io.mawhebty.dtos.requests.GenerateOtpRequest;
import io.mawhebty.dtos.requests.OTPVerificationRequest;
import io.mawhebty.dtos.responses.OTPGenerationResponse;
import io.mawhebty.dtos.responses.OTPVerificationResponse;
import io.mawhebty.dtos.responses.TokenResponse;
import io.mawhebty.dtos.responses.UserRegistrationResponseDto;
import io.mawhebty.enums.UserRoleEnum;
import io.mawhebty.exceptions.*;
import io.mawhebty.models.*;
import io.mawhebty.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class OTPService {

    private final UserRepository userRepository;
    private final UserOtpRepository userOtpRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final JWTService jwtService;
    private final TalentProfileRepository talentProfileRepository;
    private final CompanyResearcherProfileRepository companyResearcherProfileRepository;
    private final IndividualResearcherProfileRepository individualResearcherProfileRepository;
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final int OTP_EXPIRATION_MINUTES = 10;
    private static final int MAX_ACTIVE_OTP = 3;
    private final UserProfileService userProfileService;
    // private final UserStatusRepository userStatusRepository;
//    private final OtpTypeRepository otpTypeRepository;


    // Cleanup expired OTPs (can be scheduled every 12h)
    // @Transactional
    // public void cleanupExpiredOtps() {
    //     userOtpRepository.deleteExpiredOtps(LocalDateTime.now());
    // }

    private void sendOtpNotification(User user, String otp) {
        // 3. Send via Email
        if(user.getEmail()!= null && !user.getEmail().isEmpty()){
            emailService.sendVerificationEmail(user.getEmail(), otp);
        }
    }

//    @Transactional
    @Async
    public OTPGenerationResponse generateAndSendOtp(GenerateOtpRequest request){

        try{
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new UserNotFoundException("user not found with email: "+ request.getEmail()));

            // Prevent OTP spam - check if user has too many active OTPs
            Long activeOtpCount = userOtpRepository.countActiveOtpsByUserId(user.getId(), LocalDateTime.now());
            if (activeOtpCount >= MAX_ACTIVE_OTP) {
                throw new IllegalStateException("Too many active OTP requests. Please wait before requesting a new one.");
            }

            // 1. Generate OTP
            String otp = generateVerificationCode();

            // OTPType type= otpTypeRepository.findByName(OTPTypeEnum.REGISTRATION)
            //     .orElseThrow(()-> new ResourceNotFoundException("REGISTRATION otp type not found"));

            // 2. Save to UserOTP table
            UserOTP otpRecord = UserOTP.builder()
                    .user(user)
                    .hashedCode(passwordEncoder.encode(otp))
                    .isUsed(false)
                    // .otpType(type)
                    .expiryDate(LocalDateTime.now().plusMinutes(OTP_EXPIRATION_MINUTES))
                    .build();

            userOtpRepository.save(otpRecord);

            this.sendOtpNotification(user, otp);

        } catch (Exception e) {
            log.error("Failed to generate and send OTP for user with email {}: {}", request.getEmail(), e.getMessage());
            throw new OTPGenerationFailedException(e.getMessage());
        }

        return OTPGenerationResponse.builder()
                .success(true)
                .message("OTP sent to your email address")
                .nextStep("otp_verification")
                .otpExpiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRATION_MINUTES))
                .verificationUrl("/auth/verify-otp")
                .build();
    }

    @Transactional
    public OTPVerificationResponse validateOTP(OTPVerificationRequest request) {

        // 1. Find active OTP record
        UserOTP otpRecord = userOtpRepository
                // .findActiveByUserIdAndTypeName(request.getUserId(), request.getOtpType(), false)
                .findActiveByUserIdAndTypeName(request.getUserId(), false)
                .orElseThrow(() -> new OTPNotFoundException(request.getUserId()));

         // 2. Check if OTP is expired
         if (otpRecord.getExpiryDate().isBefore(LocalDateTime.now())) {
             throw new OTPExpiredException();
         }

         // 3. Check if OTP is already used
         if (otpRecord.isUsed()) {
             throw new OTPAlreadyUsedException();
         }

        // 4. Validate OTP code
        boolean isValid = passwordEncoder.matches(request.getOtpCode(), otpRecord.getHashedCode());

        if (!isValid) {
            return OTPVerificationResponse.builder()
                .success(false)
                .message("Invalid OTP code")
                .user(null)
                .tokenResponse(null)
                .build();
        }
        // 5. Mark OTP as used
        otpRecord.setUsed(true);
        userOtpRepository.save(otpRecord);

        // 6. Update user to be verified
        User user = userRepository.findByIdFetchRoleAndStatusAndUserType(request.getUserId())
                .orElseThrow(() -> new UserNotFoundException(request.getUserId()));

        if(!user.getIsVerified()) {
            user.setIsVerified(true);
            userRepository.save(user);
        }

        // Update user to be verified and pending for moderation, incase the otp type is for registeration
        // if(request.getOtpType().name().equals(OTPTypeEnum.REGISTRATION.name())){
        //     UserStatus pendingModerationStatus= userStatusRepository.findByName(UserStatusEnum.PENDING_MODERATION)
        //         .orElseThrow(()-> new ResourceNotFoundException("PENDING_MODERATION status not found"));

        //     user.setStatus(pendingModerationStatus);
        // }


        // if user has profile and status is ACTIVE return full access and refresh token
        // if user has profile and status is PENDING_MODERATION return limited access token
        TokenResponse tokenResponse= jwtService.determineSuitableTokenResponse(user);

        // prepare user data response
        UserRegistrationResponseDto userRegistrationResponseDto = UserRegistrationResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .phone(user.getPhoneNumber())
                .countryCode(user.getCountryCode())
                .userStatus(user.getStatus() != null? user.getStatus().getId(): null)
                .userRole(user.getRole() != null? user.getRole().getId(): null)
                .userType(user.getUserType() != null? user.getUserType().getId(): null)
                .build();
        // if token is null that's mean this is a new user (not registered)
        if(tokenResponse != null){
            if (user.getRole().getName().equals(UserRoleEnum.TALENT)) {
                TalentProfile profile= talentProfileRepository.findByUserId(user.getId())
                        .orElseThrow(()-> new BadDataException("the user does not have talent profile"));
                userRegistrationResponseDto.setFirstName(profile.getFirstName());
                userRegistrationResponseDto.setLastName(profile.getLastName());
                userRegistrationResponseDto.setImageUrl(profile.getProfilePicture());
            }else{
                ResearcherProfile profile= userProfileService.getResearcherProfileByType(user.getId(), user.getUserType());
                if(profile == null){
                    throw new BadDataException("the user does not have researcher profile");
                }
                if(profile instanceof IndividualResearcherProfile){
                    userRegistrationResponseDto.setFirstName(((IndividualResearcherProfile) profile).getFirstName());
                    userRegistrationResponseDto.setFirstName(((IndividualResearcherProfile) profile).getLastName());
                }else{
                    userRegistrationResponseDto.setCompanyName(((CompanyResearcherProfile) profile).getCompanyName());
                    userRegistrationResponseDto.setCompanyName(((CompanyResearcherProfile) profile).getContactPerson());
                    userRegistrationResponseDto.setCommercialRegNo(((CompanyResearcherProfile) profile).getCommercialRegNo());
                }
                userRegistrationResponseDto.setImageUrl(profile.getProfilePicture());
            }
            userRegistrationResponseDto.setPermissions(jwtService.getFullPermissions(user.getRole()));
        }

        // if user status is DRAFT return otp verified successfully without tokens (tokenResponse is null)
        return OTPVerificationResponse.builder()
                .success(true)
                .message("OTP verified successfully")
                .user(userRegistrationResponseDto)
                .tokenResponse(tokenResponse)
                .build();
    }

    public String generateVerificationCode() {
        return String.valueOf(secureRandom.nextInt(900_000) + 100_000);
    }
}