package com.example.modulecommon.model.dto.qna.in;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "QnA 답변 작성 요청 데이터")
public record QnAReplyInsertDTO(
        @Schema(name = "qnaId", description = "문의 아이디")
        long qnaId,
        @Schema(name = "content", description = "답변 내용")
        String content
) {
}
