package com.example.moduleadmin.model.dto.sales.out;

import java.util.List;

public record AdminPeriodSalesResponseDTO <T>(
        List<T> content,
        long sales,
        long salesQuantity,
        long orderQuantity
) {
}
