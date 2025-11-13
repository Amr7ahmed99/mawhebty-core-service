package io.mawhebty.dtos.responses;

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
    private Long userId;

    private TokenResponse tokenResponse;
    
}
