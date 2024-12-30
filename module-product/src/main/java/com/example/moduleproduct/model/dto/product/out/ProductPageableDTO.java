package com.example.moduleproduct.model.dto.product.out;

import org.springframework.data.domain.Page;

import java.util.List;

public record ProductPageableDTO<T>(
        List<T> content,
        boolean empty,
        long number,
        long totalPages,
        long totalElements
) {

    public ProductPageableDTO(Page<T> pageableResponse) {
        this(
                pageableResponse.getContent(),
                pageableResponse.isEmpty(),
                pageableResponse.getNumber(),
                pageableResponse.getTotalPages(),
                pageableResponse.getTotalElements()
        );
    }
}
