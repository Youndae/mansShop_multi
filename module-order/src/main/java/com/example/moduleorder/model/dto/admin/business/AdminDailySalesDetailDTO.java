package com.example.moduleorder.model.dto.admin.business;

public record AdminDailySalesDetailDTO(
        String productName,
        String size,
        String color,
        int count,
        int price
) {

    public AdminDailySalesDetailDTO(AdminOrderDetailListDTO orderDetail) {
        this(
                orderDetail.productName(),
                orderDetail.size(),
                orderDetail.color(),
                orderDetail.count(),
                orderDetail.price()
        );
    }
}
