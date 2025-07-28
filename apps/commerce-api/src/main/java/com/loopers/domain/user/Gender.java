package com.loopers.domain.user;

public enum Gender {
    MALE,
    FEMALE,
    UNKNOWN;

    public static Gender from(String gender) {
        if (gender == null) return UNKNOWN;

        return switch (gender.toUpperCase()) {
            case "MALE", "M" -> MALE;
            case "FEMALE", "F" -> FEMALE;
            default -> UNKNOWN;
        };
    }
}
