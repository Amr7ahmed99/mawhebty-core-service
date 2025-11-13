package io.mawhebty.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GenderEnum {
    MALE(1, "MALE"),
    FEMALE(2, "FEMALE");

    private final int id;
    private final String name;
}
