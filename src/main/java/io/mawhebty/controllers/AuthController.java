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
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.*;
import io.mawhebty.services.OTPService;
import io.mawhebty.services.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

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
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request, @RequestParam(required = false) boolean error){
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


    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(
            @CookieValue(name = "refresh_token", required = false) String refreshTokenCookie,
            @RequestHeader(name = "X-Refresh-Token", required = false) String refreshTokenHeader,
            HttpServletRequest request,
            HttpServletResponse response) {

        String refreshToken = jwtService.getRefreshToken(refreshTokenCookie, refreshTokenHeader);
//        boolean isFromCookie = refreshTokenCookie != null && refreshToken.equals(refreshTokenCookie);

        // Validate refresh token
        if (!jwtService.isRefreshToken(refreshToken)) {
            throw new BadDataException("Invalid refresh token");
        }

        try {
            // Extract user info
            String email = jwtService.getUserEmailFromToken(refreshToken);
            User user = userService.findByEmailFetchStatus(email);

            // Validate user
            Long tokenUserId = jwtService.getUserIdFromToken(refreshToken);
            if (!Objects.equals(user.getId(), tokenUserId)) {
                throw new BadDataException("Invalid refresh token claims");
            }

            if (jwtService.validateToken(refreshToken)) {
                // Generate new access token
                String newAccessToken = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole(),
                        user.getStatus().getName().equals(UserStatusEnum.ACTIVE.getName())? "FULL_ACCESS": "LIMITED_ACCESS", user.getStatus());

                // Rotate refresh token
                String newRefreshToken = jwtService.generateRefreshToken(user.getId(), user.getEmail());

                // Set new refresh token in cookie
                ResponseCookie newRefreshTokenCookie = ResponseCookie.from("refresh_token", newRefreshToken)
                        .httpOnly(true)
                        .secure(true)
                        .sameSite("Strict")
                        .maxAge(7 * 24 * 60 * 60)
                        .path("/api/auth/refresh")
                        .build();

                response.addHeader(HttpHeaders.SET_COOKIE, newRefreshTokenCookie.toString());

                return ResponseEntity.ok()
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + newAccessToken)
                        .body(Map.of("access_token", newAccessToken));
            }
        } catch (ExpiredJwtException e) {
            // Clear expired refresh token cookie
            ResponseCookie clearCookie = ResponseCookie.from("refresh_token", "")
                    .httpOnly(true)
                    .secure(true)
                    .sameSite("Strict")
                    .maxAge(0)
                    .path("/api/auth/refresh")
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, clearCookie.toString());

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Refresh token expired, please login again");
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Invalid refresh token");
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        // Clear refresh token cookie
        ResponseCookie clearCookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .maxAge(0)
                .path("/api/auth/refresh")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, clearCookie.toString());

        return ResponseEntity.ok().body("Logged out successfully");
    }
}
