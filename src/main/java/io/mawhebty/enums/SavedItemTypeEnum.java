package io.mawhebty.enums;

public enum SavedItemTypeEnum {
    POST(1),
    EVENT(2),
    ARTICLE(3);

    private final int value;

    SavedItemTypeEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static SavedItemTypeEnum fromValue(int value) {
        for (SavedItemTypeEnum type : SavedItemTypeEnum.values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid ItemType value: " + value);
    }
}