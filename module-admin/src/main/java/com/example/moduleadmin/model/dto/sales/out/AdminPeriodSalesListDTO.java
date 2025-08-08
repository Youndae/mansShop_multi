package com.example.moduleadmin.model.dto.sales.out;

import lombok.Builder;

@Builder
public record AdminPeriodSalesListDTO(
        int date,
        long sales,
        long salesQuantity,
        long orderQuantity
) {
    public AdminPeriodSalesListDTO(int date) {
        this(date, 0, 0, 0);
    }
}
