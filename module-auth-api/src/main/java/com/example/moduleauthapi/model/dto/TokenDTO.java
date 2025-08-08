package com.example.moduleauthapi.model.dto;

import lombok.Builder;

@Builder
public record TokenDTO(
        String accessTokenValue,
        String refreshTokenValue,
        String inoValue
) {
}