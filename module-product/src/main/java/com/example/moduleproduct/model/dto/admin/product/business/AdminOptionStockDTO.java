package com.example.moduleproduct.model.dto.admin.product.business;

public record AdminOptionStockDTO(
        String productId,
        String size,
        String color,
        int optionStock,
        boolean optionIsOpen
) {
}
