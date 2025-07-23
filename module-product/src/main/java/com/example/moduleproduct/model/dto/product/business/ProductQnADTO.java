package com.example.moduleproduct.model.dto.product.business;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ProductQnADTO(
        Long qnaId,
        String writer,
        String qnaContent,
        LocalDate createdAt,
        boolean productQnAStat
) {
    //ProductQnADSLRepository.findByProductId Constructor
    public ProductQnADTO(Long qnaId, String writer, String qnaContent, LocalDateTime createdAt, boolean productQnAStat) {
        this(
                qnaId,
                writer,
                qnaContent,
                createdAt.toLocalDate(),
                productQnAStat
        );
    }
}
