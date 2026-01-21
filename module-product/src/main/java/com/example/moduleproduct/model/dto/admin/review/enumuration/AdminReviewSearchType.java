package com.example.moduleproduct.model.dto.admin.review.enumuration;

import com.example.modulecommon.utils.CommonSearchType;
import lombok.AllArgsConstructor;

import java.util.Arrays;

@AllArgsConstructor
public enum AdminReviewSearchType implements CommonSearchType {
    USER("user"),
    PRODUCT("product");

    private final String value;

    public static AdminReviewSearchType from(String value) {
        return Arrays.stream(values())
                .filter(v -> v.value.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(
                        () -> new IllegalArgumentException("Invalid AdminReviewSearchType : " + value)
                );
    }

    @Override
    public String value() {
        return value;
    }
}
