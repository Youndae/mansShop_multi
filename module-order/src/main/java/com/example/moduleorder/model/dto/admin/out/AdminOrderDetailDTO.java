package com.example.moduleorder.model.dto.admin.out;

import com.example.moduleorder.model.dto.admin.business.AdminOrderDetailListDTO;

public record AdminOrderDetailDTO(
        String classification,
        String productName,
        String size,
        String color,
        int count,
        int price,
        boolean reviewStatus
) {

    public AdminOrderDetailDTO(AdminOrderDetailListDTO detail) {
        this(
                detail.classification(),
                detail.productName(),
                detail.size(),
                detail.color(),
                detail.count(),
                detail.price(),
                detail.reviewStatus()
        );
    }
}
