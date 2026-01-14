package com.example.moduleorder.model.enumuration;

import com.example.modulecommon.utils.CommonSearchType;
import lombok.AllArgsConstructor;

import java.util.Arrays;

@AllArgsConstructor
public enum AdminOrderSearchType implements CommonSearchType {
    RECIPIENT("recipient"),
    USER_ID("userId");

    private final String value;

    public static AdminOrderSearchType from(String value) {
        return Arrays.stream(values())
                .filter(v -> v.value.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(
                        () -> new IllegalArgumentException("Invalid AdminOrderSearchType : " + value)
                );
    }

    @Override
    public String value() {
        return value;
    }
}
