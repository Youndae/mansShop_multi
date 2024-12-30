package com.example.moduleproduct.model.dto.product.business;

public record ProductOptionDTO(
        Long optionId,
        String size,
        String color,
        int stock
) {
}
