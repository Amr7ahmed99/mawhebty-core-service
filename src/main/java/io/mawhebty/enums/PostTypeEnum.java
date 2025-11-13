package io.mawhebty.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PostTypeEnum {
    PROFILE_VIDEO(1, "PROFILE_VIDEO"),
    REEL(2, "REEL"),
    IMAGE(3, "IMAGE"),
    DOCUMENT(4, "DOCUMENT"),
    REGISTRATION_FILE(5, "REGISTRATION_FILE");

    private final int id;
    private final String name;
}
