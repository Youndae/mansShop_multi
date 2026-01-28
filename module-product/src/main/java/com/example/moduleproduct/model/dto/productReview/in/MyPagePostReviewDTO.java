package com.example.moduleproduct.model.dto.productReview.in;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "회원 리뷰 작성 요청 데이터")
public record MyPagePostReviewDTO(
        @Schema(name = "productId", description = "상품 아이디")
        @NotBlank(message = "Integrity productId")
        String productId,

        @Schema(name = "content", description = "리뷰 내용")
        @NotBlank(message = "리뷰 내용은 필수 입력 사항입니다.")
        @Size(min = 2, message = "리뷰 내용은 2글자 이상이어야 합니다.")
        String content,

        @Schema(name = "optionId", description = "상품 옵션 아이디")
        @Min(value = 1, message = "Integrity optionId")
        long optionId,

        @Schema(name = "detailId", description = "주문 상세 아이디")
        @Min(value = 1, message = "Integrity detailId")
        long detailId
) {
}
