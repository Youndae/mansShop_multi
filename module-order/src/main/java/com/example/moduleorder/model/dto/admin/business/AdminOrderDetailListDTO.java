package com.example.moduleorder.model.dto.admin.business;

public record AdminOrderDetailListDTO(
        Long orderId,
        String classification,
        String productName,
        String size,
        String color,
        int count,
        int price,
        boolean reviewStatus
) {
}
