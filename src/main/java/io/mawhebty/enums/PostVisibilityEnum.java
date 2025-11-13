package io.mawhebty.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PostVisibilityEnum {
    PUBLIC(1, "PUBLIC"),
    PRIVATE(2, "PRIVATE"),
    FOLLOWERS_ONLY(3, "FOLLOWERS_ONLY");

    private final int id;
    private final String name;
}
