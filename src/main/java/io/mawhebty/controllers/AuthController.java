package io.mawhebty.controllers;

import io.mawhebty.dtos.requests.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.mawhebty.dtos.responses.DraftResponse;
import io.mawhebty.dtos.responses.LoginResponse;
import io.mawhebty.dtos.responses.OTPGenerationResponse;
import io.mawhebty.dtos.responses.OTPVerificationResponse;
import io.mawhebty.dtos.responses.TokenResponse;
import io.mawhebty.services.OTPService;
import io.mawhebty.services.RegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final RegistrationService registrationService;
    private final OTPService otpService;


    // Step 1
    @PostMapping(value= "/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request){
        LoginResponse loginResponse= registrationService.login(request);
        return ResponseEntity.ok(loginResponse);
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
    public ResponseEntity<TokenResponse> confirmRegistration(@Valid @RequestBody ConfirmRegistrationRequest request) {
        TokenResponse response = registrationService.confirmRegistration(request);
        return ResponseEntity.ok(response);
    }
}
