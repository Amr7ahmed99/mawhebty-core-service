package io.mawhebty.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MediaModerationStatusEnum {
    PENDING(1, "PENDING"),
    APPROVED(2, "APPROVED"),
    REJECTED(3, "REJECTED");

    private final int id;
    private final String name;
}
