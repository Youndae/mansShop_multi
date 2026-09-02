package com.example.moduleauthapi.model.dto;

import com.example.modulecommon.model.enumuration.Role;

public record TokenVerifyResult(
        String userId,
        Role role
) {
    public TokenVerifyResult(String userId, String role) {
        this(
                userId,
                Role.fromKey(role)
        );
    }


}
