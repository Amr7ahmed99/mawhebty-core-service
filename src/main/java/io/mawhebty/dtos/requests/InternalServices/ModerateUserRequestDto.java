package io.mawhebty.dtos.requests.InternalServices;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class ModerateUserRequestDto {
    @NotNull
    @Min(1)
    @JsonProperty("user_id")
    private Long userId;
    @NotNull
    @Min(1)
    @JsonProperty("media_id")
    private Long mediaId;
    @NotBlank
    private String decision;
    @JsonProperty("reviewed_by")
    private Long moderatorId;
    @JsonProperty("moderation_type")
    @NotBlank
    private String moderationType;// DOCUMENT_VERIFICATION, USER_REGISTRATION
    @JsonProperty("file_type")
    @NotBlank
    private String fileType;// SPECIAL_CASE_DOCUMENT, REGISTRATION_MEDIA, REGISTRATION_DOCUMENT

    private String reason;
}
