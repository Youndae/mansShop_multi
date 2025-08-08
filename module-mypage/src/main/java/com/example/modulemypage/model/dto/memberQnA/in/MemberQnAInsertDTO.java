package com.example.modulemypage.model.dto.memberQnA.in;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "회원문의 작성 요청 데이터")
public record MemberQnAInsertDTO(
        @Schema(name = "title", description = "문의 제목")
        String title,
        @Schema(name = "content", description = "문의 내용")
        String content,
        @Schema(name = "classificationId", description = "문의 분류 아이디")
        long classificationId
) {
}
