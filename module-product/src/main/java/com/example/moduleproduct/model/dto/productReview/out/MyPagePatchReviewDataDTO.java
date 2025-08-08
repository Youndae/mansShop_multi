package com.example.moduleproduct.model.dto.productReview.out;

public record MyPagePatchReviewDataDTO(
        long reviewId,
        String content,
        String productName
) {
}
