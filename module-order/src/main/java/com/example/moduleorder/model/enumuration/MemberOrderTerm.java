package com.example.moduleorder.model.enumuration;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum MemberOrderTerm {
    QUARTER_YEAR("3"),
    HALF_YEAR("6"),
    ONE_YEAR("12"),
    ALL("all");

    private final String term;

    public static void validate(String term) {
        boolean exists = Arrays.stream(values())
                .anyMatch(v -> v.term.equalsIgnoreCase(term));

        if(!exists)
            throw new IllegalArgumentException("Invalid MemberOrderTerm : " + term);
    }
}
