package io.mawhebty.enums;

import lombok.Getter;

@Getter
public enum ArticleSectionType {
    TEXT("text", "نص"),
    IMAGE("image", "صورة"),
    VIDEO("video", "فيديو"),
    CODE("code", "كود"),
    QUOTE("quote", "اقتباس"),
    HEADING("heading", "عنوان"),
    EMBED("embed", "محتوى مضمن"),
    GALLERY("gallery", "معرض صور"),
    LIST("list", "قائمة"),
    TABLE("table", "جدول"),
    CALL_TO_ACTION("cta", "دعوة للعمل"),
    DIVIDER("divider", "فاصل");

    private final String value;
    private final String arabicName;

    ArticleSectionType(String value, String arabicName) {
        this.value = value;
        this.arabicName = arabicName;
    }

    public static ArticleSectionType fromValue(String value) {
        for (ArticleSectionType type : ArticleSectionType.values()) {
            if (type.getValue().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid ArticleSectionType value: " + value);
    }
}