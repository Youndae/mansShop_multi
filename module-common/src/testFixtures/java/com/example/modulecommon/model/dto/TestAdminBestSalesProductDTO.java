package com.example.modulecommon.model.dto;

public record TestAdminBestSalesProductDTO(
        String productName,
        long productPeriodSalesQuantity,
        long productPeriodSales
) {
    public TestAdminBestSalesProductDTO (String productName) {
        this(productName, 0, 0);
    }
}
