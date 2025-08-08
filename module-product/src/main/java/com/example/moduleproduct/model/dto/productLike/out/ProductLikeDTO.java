package com.example.moduleproduct.model.dto.productLike.out;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ProductLikeDTO(
        Long likeId,
        String productId,
        String productName,
        int productPrice,
        String thumbnail,
        int stock,
        LocalDate createdAt
) {
    public ProductLikeDTO(Long likeId,
                          String productId,
                          String productName,
                          int productPrice,
                          String thumbnail,
                          int stock,
                          LocalDateTime createdAt) {
        this(
                likeId,
                productId,
                productName,
                productPrice,
                thumbnail,
                stock,
                createdAt.toLocalDate()
        );
    }
}
