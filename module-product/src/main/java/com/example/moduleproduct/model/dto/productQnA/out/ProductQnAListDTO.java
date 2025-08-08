package com.example.moduleproduct.model.dto.productQnA.out;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ProductQnAListDTO(
        Long productQnAId,
        String productName,
        boolean productQnAStat,
        LocalDate createdAt
) {

    public ProductQnAListDTO(Long productQnAId,
                             String productName,
                             boolean productQnAStat,
                             LocalDateTime createdAt) {
        this(
                productQnAId,
                productName,
                productQnAStat,
                createdAt.toLocalDate()
        );
    }
}
