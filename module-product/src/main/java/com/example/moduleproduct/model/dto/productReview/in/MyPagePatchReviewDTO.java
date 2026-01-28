package com.example.moduleproduct.model.dto.productReview.in;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "회원 리뷰 수정 요청 데이터")
public record MyPagePatchReviewDTO(
        @Schema(name = "reviewId", description = "리뷰 아이디")
        @Min(value = 1, message = "Integrity reviewId")
        long reviewId,

        @Schema(name = "content", description = "리뷰 내용")
        @NotBlank(message = "리뷰 내용은 필수 입력값입니다.")
        @Size(min = 2, message = "리뷰 내용은 최소 2글자 이상이어야 합니다.")
        String content
) {
}
