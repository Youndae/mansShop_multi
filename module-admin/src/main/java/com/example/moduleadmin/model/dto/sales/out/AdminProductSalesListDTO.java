package com.example.moduleadmin.model.dto.sales.out;

public record AdminProductSalesListDTO(
        String classification,
        String productId,
        String productName,
        long sales,
        long salesQuantity
) {
}
