package com.example.moduleproduct.model.dto.product.business;

import java.time.LocalDate;

public record ProductQnADTO(
        Long qnaId,
        String writer,
        String qnaContent,
        LocalDate createdAt,
        boolean productQnAStat
) {
}
