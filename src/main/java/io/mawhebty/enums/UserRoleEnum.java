package io.mawhebty.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserRoleEnum {
    TALENT(1, "TALENT"),
    RESEARCHER(2, "RESEARCHER");

    private final int id;
    private final String name;

}