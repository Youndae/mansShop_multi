package com.example.moduleuser.model.dto.member.in;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "회원 정보 수정 요청 데이터")
public record MyPageInfoPatchDTO(
        @Schema(name = "nickname", description = "닉네임")
        String nickname,
        @Schema(name = "phone", description = "연락처", example = "01012345678")
        String phone,
        @Schema(name = "mail", description = "이메일", example = "tester1@tester1.com")
        String mail
) {
}