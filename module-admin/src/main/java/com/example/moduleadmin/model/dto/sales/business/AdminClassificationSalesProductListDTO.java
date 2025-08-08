package com.example.moduleadmin.model.dto.sales.business;

public record AdminClassificationSalesProductListDTO(
        String productName,
        String size,
        String color,
        long productSales,
        long productSalesQuantity
) {
}
