package io.mawhebty.dtos.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GenerateOtpRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email")
    private String email;
}
