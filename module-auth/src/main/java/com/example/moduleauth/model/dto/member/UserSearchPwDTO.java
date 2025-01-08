package com.example.moduleauth.model.dto.member;

public record UserSearchPwDTO(
        String userId,
        String userName,
        String userEmail
) {
}
