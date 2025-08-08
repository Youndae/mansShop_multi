package com.example.moduleadmin.model.dto.sales.business;

public record AdminBestSalesProductDTO(
        String productName,
        long productPeriodSalesQuantity,
        long productPeriodSales
) {

    public AdminBestSalesProductDTO (String productName) {
        this(productName, 0, 0);
    }
}
