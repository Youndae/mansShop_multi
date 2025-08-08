package com.example.moduleproduct.model.dto.productReview.in;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "회원 리뷰 작성 요청 데이터")
public record MyPagePostReviewDTO(
        @Schema(name = "productId", description = "상품 아이디")
        String productId,
        @Schema(name = "content", description = "리뷰 내용")
        String content,
        @Schema(name = "optionId", description = "상품 옵션 아이디")
        long optionId,
        @Schema(name = "detailId", description = "주문 상세 아이디")
        long detailId
) {
}
