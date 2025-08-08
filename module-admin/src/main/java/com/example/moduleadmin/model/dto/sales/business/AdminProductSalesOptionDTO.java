package com.example.moduleadmin.model.dto.sales.business;

import com.example.modulecommon.model.entity.ProductOption;

public record AdminProductSalesOptionDTO(
        long optionId,
        String size,
        String color,
        long optionSales,
        long optionSalesQuantity
) {

    public AdminProductSalesOptionDTO(ProductOption option, long optionSales, long optionSalesQuantity) {
        this(
                option.getId(),
                option.getSize(),
                option.getColor(),
                optionSales,
                optionSalesQuantity
        );
    }

    public AdminProductSalesOptionDTO(ProductOption option) {
        this(
                option.getId(),
                option.getSize(),
                option.getColor(),
                0,
                0
        );
    }

    public AdminProductSalesOptionDTO(Long optionId, String size, String color) {
        this(
                optionId,
                size,
                color,
                0,
                0
        );
    }
}
