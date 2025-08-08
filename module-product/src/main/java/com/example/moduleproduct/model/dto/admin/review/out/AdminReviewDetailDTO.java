package com.example.moduleproduct.model.dto.admin.review.out;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AdminReviewDetailDTO(
        long reviewId,
        String productName,
        String size,
        String color,
        String writer,
        LocalDate createdAt,
        LocalDate updatedAt,
        String content,
        LocalDate replyUpdatedAt,
        String replyContent
) {

    public AdminReviewDetailDTO(long reviewId,
                                String productName,
                                String size,
                                String color,
                                String writer,
                                LocalDateTime createdAt,
                                LocalDateTime updatedAt,
                                String content,
                                LocalDateTime replyUpdatedAt,
                                String replyContent) {
        this(
                reviewId,
                productName,
                size,
                color,
                writer,
                createdAt.toLocalDate(),
                updatedAt.toLocalDate(),
                content,
                replyUpdatedAt == null ? null : replyUpdatedAt.toLocalDate(),
                replyContent
        );
    }
}
