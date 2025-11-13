package io.mawhebty.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ParticipationTypesEnum {
    PROJECT_IDEA(1, "PROJECT_IDEA"),
    PERSONAL_TALENT(2, "PERSONAL_TALENT"),
    PATENT(3, "PATENT");

    private final int id;
    private final String name;

}