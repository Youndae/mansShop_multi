package com.example.modulecommon.model.enumuration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum AdminListType {
    NEW("new"),
    ALL("all");

    private final String type;

    public static void validate(String listType) {
        boolean exists = Arrays.stream(values())
                .anyMatch(v -> v.type.equalsIgnoreCase(listType));

        if(!exists)
            throw new IllegalArgumentException("Invalid AdminListType : " + listType);
    }

    public static boolean isAll(String listType) {
        return ALL.type.equalsIgnoreCase(listType);
    }
}
