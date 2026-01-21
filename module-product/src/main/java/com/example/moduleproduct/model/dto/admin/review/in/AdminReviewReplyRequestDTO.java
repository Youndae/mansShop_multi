package com.example.moduleproduct.model.dto.admin.review.in;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "관리자의 리뷰 답변 작성 요청 데이터")
public record AdminReviewReplyRequestDTO(
        @Schema(name = "reviewId", description = "리뷰 아이디")
        @Min(value = 1, message = "리뷰 아이디가 잘못 되었습니다.")
        long reviewId,

        @Schema(name = "content", description = "리뷰 답변 내용")
        @NotBlank(message = "답변 내용은 필수 입력사항입니다.")
        String content
) {
}
