package io.mawhebty.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginResponse {
    OTPGenerationResponse otpState;
    private boolean isNewUser;
    private Long userId;
}
