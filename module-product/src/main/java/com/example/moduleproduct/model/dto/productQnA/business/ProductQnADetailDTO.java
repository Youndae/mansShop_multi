package com.example.moduleproduct.model.dto.productQnA.business;

import com.example.modulecommon.model.entity.ProductQnA;

import java.time.LocalDateTime;

public record ProductQnADetailDTO(
        long productQnAId,
        String productName,
        String writer,
        String qnaContent,
        LocalDateTime createdAt,
        boolean productQnAStat
) {
    public ProductQnADetailDTO(ProductQnA productQnA, String writer) {
        this(
                productQnA.getId(),
                productQnA.getProduct().getProductName(),
                writer,
                productQnA.getQnaContent(),
                productQnA.getCreatedAt(),
                productQnA.isProductQnAStat()
        );
    }
}
