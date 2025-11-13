package io.mawhebty.dtos.requests;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminReviewRequest {
    @NotNull
    private String mediaStatus;// MediaModerationStatusEnum

    @NotNull
    private Long userId;

    @NotNull
    private Long mediaId;

    @NotNull
    private String moderationType;// ModerationTypeEnum

    private String rejectionReason;
    private String adminNotes;
}

