package com.example.modulemypage.model.dto.memberQnA.in;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "회원 문의 수정 요청 데이터")
public record MemberQnAModifyDTO(
        @Schema(name = "qnaId", description = "문의 아이디")
        @Min(value = 1, message = "Integrity qnaId")
        long qnaId,

        @Schema(name = "title", description = "문의 제목")
        @NotBlank(message = "제목은 필수 입력값입니다.")
        @Size(min = 2, message = "제목은 최소 2글자 이상이어야 합니다.")
        String title,

        @Schema(name = "content", description = "문의 내용")
        @NotBlank(message = "내용은 필수 입력값입니다.")
        @Size(min = 2, message = "내용은 최소 2글자 이상이어야 합니다.")
        String content,

        @Schema(name = "classificationId", description = "문의 분류 아이디")
        @Min(value = 1, message = "IntegrityClassificationId")
        long classificationId
) {
}
