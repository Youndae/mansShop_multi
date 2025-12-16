package com.example.modulecommon.model.enumuration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public enum Result {

    TOKEN_STEALING("token Stealing"),
    TOKEN_EXPIRATION("token expiration"),
    WRONG_TOKEN("wrong token"),
    OK("OK"),
    FAIL("FAIL"),
    ERROR("error"),
    NOTFOUND("not found"),
    DUPLICATE("duplicated"),
    NO_DUPLICATE("No duplicates"),
    EMPTY("empty");

    private final String resultKey;

    private static final Map<String, Result> mapper = Arrays.stream(values())
                                                        .collect(
                                                                Collectors.toMap(Result::getResultKey, r -> r)
                                                        );

    public static Result fromKey(String key) {
        return mapper.get(key);
    }
}
