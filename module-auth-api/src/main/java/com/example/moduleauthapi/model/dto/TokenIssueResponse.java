package com.example.moduleauthapi.model.dto;

public record TokenIssueResponse(
        String accessToken,
        String userId,
        String role
) {
}
