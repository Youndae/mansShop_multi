package com.example.moduleproduct.model.dto.admin.review.out;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AdminReviewDTO(
        long reviewId,
        String productName,
        String writer,
        LocalDate updatedAt,
        boolean status
) {

    public AdminReviewDTO(long reviewId,
                          String productName,
                          String writer,
                          LocalDateTime updatedAt,
                          boolean status) {
        this(
                reviewId,
                productName,
                writer,
                updatedAt.toLocalDate(),
                status
        );
    }
}
