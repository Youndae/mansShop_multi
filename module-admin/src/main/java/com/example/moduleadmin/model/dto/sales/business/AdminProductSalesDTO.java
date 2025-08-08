package com.example.moduleadmin.model.dto.sales.business;

public record AdminProductSalesDTO(
        String productName,
        long totalSales,
        long totalSalesQuantity
) {
}
