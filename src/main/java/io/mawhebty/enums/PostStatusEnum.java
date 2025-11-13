package io.mawhebty.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PostStatusEnum {
    DRAFT(1, "DRAFT"),
    PENDING_MODERATION(2, "PENDING_MODERATION"),
    PUBLISHED(3, "PUBLISHED"),
    REJECTED(4, "REJECTED");

    private final int id;
    private final String name;
}
