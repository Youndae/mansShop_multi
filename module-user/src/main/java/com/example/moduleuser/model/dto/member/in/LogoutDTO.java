package com.example.moduleuser.model.dto.member.in;

import lombok.Builder;

@Builder
public record LogoutDTO(
        String authorizationToken,
        String inoValue,
        String userId
) {
}
