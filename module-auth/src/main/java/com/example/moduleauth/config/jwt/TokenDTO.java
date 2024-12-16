package com.example.moduleauth.config.jwt;

import lombok.Builder;

@Builder
public record TokenDTO(
        String accessTokenValue,
        String refreshTokenValue,
        String inoValue
) {
}
