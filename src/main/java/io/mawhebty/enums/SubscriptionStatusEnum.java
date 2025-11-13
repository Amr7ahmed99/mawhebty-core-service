package io.mawhebty.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum SubscriptionStatusEnum {
    ACTIVE(1, "ACTIVE"),
    EXPIRED(2, "EXPIRED"),
    CANCELLED(3, "CANCELLED"),
    PENDING_RENEWAL(4, "PENDING_RENEWAL");

    private final Integer id;
    private final String name;

    // Utility methods
    public static SubscriptionStatusEnum fromId(Integer id) {
        if (id == null) return null;
        return Arrays.stream(values())
                .filter(status -> status.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid status id: " + id));
    }

    public static SubscriptionStatusEnum fromName(String name) {
        if (name == null) return null;
        return Arrays.stream(values())
                .filter(status -> status.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid status name: " + name));
    }
}