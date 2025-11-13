package io.mawhebty.dtos.responses;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class AdminReviewResponse {
    private boolean success;
    private Long userId;
    private Long mediaId;
    private String newStatus;// UserStatusEnum
    private String mediaStatus;// MediaModerationStatusEnum
    private String message;
    private LocalDateTime reviewedAt;
}
