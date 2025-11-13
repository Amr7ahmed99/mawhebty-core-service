package io.mawhebty.dtos.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email")
    private String email;
}
