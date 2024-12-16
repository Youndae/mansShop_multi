package com.example.modulecommon.model.enumuration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OAuthProvider {

    LOCAL("local"),
    GOOGLE("google"),
    NAVER("naver"),
    KAKAO("kakao");

    private final String key;
}
