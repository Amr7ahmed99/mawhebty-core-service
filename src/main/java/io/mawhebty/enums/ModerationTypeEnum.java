package io.mawhebty.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ModerationTypeEnum {
    USER_REGISTRATION(1, "USER_REGISTRATION"),
    MEDIA_CONTENT(2, "MEDIA_CONTENT"),
    PROFILE_UPDATE(3, "PROFILE_UPDATE"),
    DOCUMENT_VERIFICATION(4, "DOCUMENT_VERIFICATION");

    private final int id;
    private final String name;
}
