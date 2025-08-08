package com.example.modulemypage.model.dto.memberQnA.in;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "회원 문의 수정 요청 데이터")
public record MemberQnAModifyDTO(
        @Schema(name = "qnaId", description = "문의 아이디")
        long qnaId,
        @Schema(name = "title", description = "문의 제목")
        String title,
        @Schema(name = "content", description = "문의 내용")
        String content,
        @Schema(name = "classificationId", description = "문의 분류 아이디")
        long classificationId
) {
}
