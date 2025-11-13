package io.mawhebty.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OTPTypeEnum {
    REGISTRATION(1, "REGISTRATION"),
    PASSWORD_RESET(2, "PASSWORD_RESET"),
    LOGIN(3, "LOGIN");

    private final int id;
    private final String name;
}
