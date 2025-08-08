package com.example.moduleproduct.model.dto.admin.product.business;

public record AdminProductStockDataDTO(
        String productId,
        String classification,
        String productName,
        int totalStock,
        boolean isOpen
) {
}
