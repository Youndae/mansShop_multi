package com.example.moduleuser.model.dto.member.in;

public record UserResetPwDTO(
        String userId,
        String certification,
        String userPw
) {
}
