package com.example.moduleauthapi.model.dto;

public record TokenReissueInfo(
        String refreshTokenValue,
        String inoValue
) {
}
