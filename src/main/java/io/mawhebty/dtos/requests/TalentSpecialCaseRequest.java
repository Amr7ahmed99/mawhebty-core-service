package io.mawhebty.dtos.requests;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TalentSpecialCaseRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email")
    private String email;

    @NotBlank
    @Size(max = 250)
    private String shortBrief;

    @NotNull
    private MultipartFile file; // Document
}
