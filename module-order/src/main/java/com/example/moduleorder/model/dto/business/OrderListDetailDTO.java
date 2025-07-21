package com.example.moduleorder.model.dto.business;

public record OrderListDetailDTO(
        long orderId,
        String productId,
        long optionId,
        long detailId,
        String productName,
        String size,
        String color,
        int detailCount,
        int detailPrice,
        boolean reviewStatus,
        String thumbnail
) {
}
