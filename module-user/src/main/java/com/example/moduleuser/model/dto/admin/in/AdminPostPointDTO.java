package com.example.moduleuser.model.dto.admin.in;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "회원 포인트 지급 요청 데이터")
public record AdminPostPointDTO(
        @Schema(name = "userId", description = "사용자 아이디")
        String userId,
        @Schema(name = "point", description = "지급될 포인트")
        long point
) {
}