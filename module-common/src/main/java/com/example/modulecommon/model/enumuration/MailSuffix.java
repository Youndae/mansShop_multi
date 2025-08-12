package com.example.modulecommon.model.enumuration;

import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
public enum MailSuffix {

    NAVER("naver"),
    DAUM("daum"),
    GOOGLE("google"),
    NONE("none");

    private final String mailSuffixType;

    public static String findSuffixType(String suffix) {

        return Arrays.stream(MailSuffix.values())
                .filter(mailSuffix -> mailSuffix.mailSuffixType.equals(suffix))
                .findFirst()
                .orElse(NONE)
                .mailSuffixType;
    }
}
