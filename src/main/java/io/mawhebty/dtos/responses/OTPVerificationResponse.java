package io.mawhebty.dtos.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OTPVerificationResponse {
    private boolean success;
    private String message;
    private UserRegistrationResponseDto user;
    @JsonProperty("token_response")
    private TokenResponse tokenResponse;
    
}
