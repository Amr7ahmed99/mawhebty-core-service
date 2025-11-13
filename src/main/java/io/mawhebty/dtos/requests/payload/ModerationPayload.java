package io.mawhebty.dtos.requests.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ModerationPayload {
    private Long userId;
    private Integer userRole; // TALENT, RESEARCHER
    private Long mediaId;// postId
    private String fileUrl;
    private String fileType; // PROFILE_PICTURE, REGISTRATION_DOCUMENT, etc.
    private LocalDateTime submittedAt;
    private String moderationType; // USER_REGISTRATION, MEDIA_CONTENT, etc.
}