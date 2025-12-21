package io.mawhebty.controllers;

import io.jsonwebtoken.ExpiredJwtException;
import io.mawhebty.dtos.requests.*;
import io.mawhebty.dtos.responses.*;
import io.mawhebty.enums.UserStatusEnum;
import io.mawhebty.exceptions.BadDataException;
import io.mawhebty.models.User;
import io.mawhebty.services.JWTService;
import io.mawhebty.services.UserService;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import io.mawhebty.services.OTPService;
import io.mawhebty.services.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final RegistrationService registrationService;
    private final OTPService otpService;
    private final JWTService jwtService;
    private final UserService userService;



    // Step 1
    @PostMapping(value= "/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request, @Nullable @RequestParam boolean error){
        if (error){
            throw new IllegalStateException("Google authentication failed");
        }
        LoginResponse loginResponse= registrationService.login(request);
        return ResponseEntity.ok(loginResponse);
    }

    @GetMapping("/google")
    public void googleLogin(HttpServletResponse response) throws IOException {
        response.sendRedirect("/oauth2/authorization/google");
    }

    // Step 2
    @PostMapping("/verify-otp")
    public ResponseEntity<OTPVerificationResponse> verifyOTP(
            @Valid @RequestBody OTPVerificationRequest request) {
        OTPVerificationResponse response = otpService.validateOTP(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/register/draft", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DraftResponse> createDraftRegistration(
            @Valid @ModelAttribute DraftRegistrationRequest request) {
        DraftResponse response = registrationService.createDraftUser(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/send-otp")
    public ResponseEntity<OTPGenerationResponse> generateOTP(@Valid @RequestBody GenerateOtpRequest request) {
        OTPGenerationResponse response = otpService.generateAndSendOtp(request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("register/confirm")
    public ResponseEntity<ConfirmRegistrationResponse> confirmRegistration(@Valid @RequestBody ConfirmRegistrationRequest request) {
        ConfirmRegistrationResponse response = registrationService.confirmRegistration(request);
        return ResponseEntity.ok(response);
    }

    // TODO: MAKE THIS LOGIC IN doD
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(String refreshToken) {
        if (refreshToken == null) {
            throw new BadDataException("Refresh token missing");
        }

        try {
            String email = jwtService.extractUserEmail(refreshToken);
            User user = userService.findByEmail(email);

            if (jwtService.validateToken(refreshToken)) {
                String newAccessToken = jwtService.generateToken(user.getId(), user.getEmail(),
                        user.getRole(), user.getStatus().getName().equals(UserStatusEnum.ACTIVE.getName())? "FULL_ACCESS": "LIMITED_ACCESS");

                return ResponseEntity.ok().body(Map.of("accessToken", newAccessToken));
            }
        } catch (ExpiredJwtException e) {
//            log.error("Refresh token expired", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token expired, please login again");
        }

//        log.error("Invalid refresh token");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token");
    }
}
