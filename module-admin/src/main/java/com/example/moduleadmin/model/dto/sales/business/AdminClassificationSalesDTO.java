package com.example.moduleadmin.model.dto.sales.business;

public record AdminClassificationSalesDTO(
        long sales,
        long salesQuantity,
        long orderQuantity
) {

    public static AdminClassificationSalesDTO emptyDTO() {
        return new AdminClassificationSalesDTO(0, 0, 0);
    }
}
