package com.example.moduleuser.model.dto.member.in;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "로그인 요청 데이터")
public record LoginDTO(
        @Schema(name = "userId", description = "사용자 아이디")
        String userId,
        @Schema(name = "password", description = "사용자 비밀번호")
        String userPw
) { }
