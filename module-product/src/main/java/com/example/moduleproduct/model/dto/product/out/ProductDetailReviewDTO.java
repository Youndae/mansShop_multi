package com.example.moduleproduct.model.dto.product.out;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ProductDetailReviewDTO(
        String reviewWriter,
        String reviewContent,
        LocalDate reviewCreatedAt,
        String answerContent,
        LocalDate answerCreatedAt
) {

    public ProductDetailReviewDTO(String reviewWriter,
                            String reviewContent,
                            LocalDateTime reviewCreatedAt,
                            String answerContent,
                            LocalDateTime answerCreatedAt) {
        this(
                reviewWriter,
                reviewContent,
                reviewCreatedAt.toLocalDate(),
                answerContent,
                answerCreatedAt == null ? null : answerCreatedAt.toLocalDate()
        );
    }
}
