package com.example.modulecommon.model.dto.qna.in;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "QnA 답변 작성 요청 데이터")
public record QnAReplyInsertDTO(
        @Schema(name = "qnaId", description = "문의 아이디")
        @Min(value = 1, message = "문의 아이디가 잘못 되었습니다.")
        long qnaId,

        @Schema(name = "content", description = "답변 내용")
        @NotBlank(message = "답변 내용은 필수 입력사항입니다.")
        String content
) {
}
