package com.example.moduleproduct.model.dto.product.business;

import java.time.LocalDate;

public record ProductReviewResponseDTO(
        String reviewWriter,
        String reviewContent,
        LocalDate reviewCreatedAt,
        String answerContent,
        LocalDate answerCreatedAt
) {
}
