package com.example.moduleadmin.model.dto.sales.out;

public record AdminPeriodClassificationDTO(
        String classification,
        long classificationSales,
        long classificationSalesQuantity
) {
    public AdminPeriodClassificationDTO(String classification) {
        this(classification, 0, 0);
    }
}
