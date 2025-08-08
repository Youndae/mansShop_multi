package com.example.moduleproduct.model.dto.productReview.out;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record MyPageReviewDTO(
        long reviewId,
        String thumbnail,
        String productName,
        String content,
        LocalDate createdAt,
        LocalDate updatedAt,
        String replyContent,
        LocalDate replyUpdatedAt
) {

    public MyPageReviewDTO(long reviewId,
                           String thumbnail,
                           String productName,
                           String content,
                           LocalDateTime createdAt,
                           LocalDateTime updatedAt,
                           String replyContent,
                           LocalDateTime replyUpdatedAt) {
        this(
                reviewId,
                thumbnail,
                productName,
                content,
                createdAt.toLocalDate(),
                updatedAt.toLocalDate(),
                replyContent,
                replyUpdatedAt == null ? null : replyUpdatedAt.toLocalDate()
        );
    }
}
