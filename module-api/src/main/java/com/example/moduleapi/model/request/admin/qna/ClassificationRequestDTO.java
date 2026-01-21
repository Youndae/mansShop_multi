package com.example.moduleapi.model.request.admin.qna;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "회원 문의 분류 추가 요청 데이터")
public record ClassificationRequestDTO(
        @Schema(name = "classification", description = "상품 분류명")
        @NotBlank(message = "상품 분류명은 필수 입력값입니다.")
        String classification
) {
}
