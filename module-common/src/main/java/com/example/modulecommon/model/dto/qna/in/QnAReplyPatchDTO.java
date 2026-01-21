package com.example.modulecommon.model.dto.qna.in;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "문의 답변 수정 요청 데이터")
public record QnAReplyPatchDTO(
        @Schema(name = "replyId", description = "답변 아이디")
        @Min(value = 1, message = "답변 아이디가 잘못되었습니다.")
        long replyId,
        @Schema(name = "content", description = "수정 내용")
        @NotBlank(message = "답변 내용은 필수 입력 사항입니다.")
        String content
) {
}
