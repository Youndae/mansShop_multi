package com.example.modulecommon.utils;

import lombok.NoArgsConstructor;

import java.util.Arrays;

@NoArgsConstructor
public final class CommonSearchTypeFinder {

    public static <T extends Enum<T> & CommonSearchType> T from(
            T[] values,
            String value,
            Class<T> enumType
    ) {
        return Arrays.stream(values)
                .filter(v -> v.value().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(
                        () -> new IllegalArgumentException("Invalid" + enumType.getSimpleName() + " : " + value)
                );
    }
}
