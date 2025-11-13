package io.mawhebty.dtos.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OTPVerificationRequest {
    private Long userId;
    // private String phone;
    // private String prefixCode;
    private String otpCode;
    // private String email;
    // private OTPTypeEnum otpType;
}
