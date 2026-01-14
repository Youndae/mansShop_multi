package com.example.moduleuser.model.dto.admin.in;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

@Schema(name = "회원 포인트 지급 요청 데이터")
public record AdminPostPointDTO(
        @Schema(name = "userId", description = "사용자 아이디")
        @Size(min = 4, message = "아이디는 최소 4자 이상입니다.")
        String userId,
        @Schema(name = "point", description = "지급될 포인트")
        @Min(value = 1, message = "최소 지급 포인트는 1 이상입니다.")
        long point
) {
}