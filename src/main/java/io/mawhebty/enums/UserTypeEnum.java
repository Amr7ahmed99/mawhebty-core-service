package io.mawhebty.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserTypeEnum {
    INDIVIDUAL(1, "INDIVIDUAL"),
    COMPANY(2, "COMPANY");

    private final int id;
    private final String name;

}