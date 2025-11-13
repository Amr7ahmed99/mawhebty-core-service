package io.mawhebty.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserStatusEnum {
    DRAFT(1, "DRAFT"),
    PENDING_MODERATION(2, "PENDING_MODERATION"),
    ACTIVE(3, "ACTIVE"),
    REJECTED(4, "REJECTED"),
    SUSPENDED(5, "SUSPENDED");

    private final int id;
    private final String name;
}
