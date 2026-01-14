package com.example.moduleuser.model.enumuration;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Schema(description = "검색 타입")
@Getter
@RequiredArgsConstructor
public enum AdminMemberSearchType {
    USER_ID("userId"),
    USER_NAME("userName"),
    NICKNAME("nickname");

    private final String value;

    public static AdminMemberSearchType from (String value) {
        return Arrays.stream(values())
                .filter(v -> v.value.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(
                        () -> new IllegalArgumentException("Invalid AdminMemberSearchType : " + value)
                );
    }
}
