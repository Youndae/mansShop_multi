package com.example.moduleorder.model.dto.admin.out;

import com.example.modulecommon.model.entity.ProductOrder;
import com.example.moduleorder.model.dto.admin.business.AdminDailySalesDetailDTO;

import java.util.List;

public record AdminDailySalesResponseDTO(
        long totalPrice,
        long deliveryFee,
        String paymentType,
        List<AdminDailySalesDetailDTO> detailList
) {

    public AdminDailySalesResponseDTO(ProductOrder productOrder, List<AdminDailySalesDetailDTO> detailContent) {
        this(
                productOrder.getOrderTotalPrice(),
                productOrder.getDeliveryFee(),
                productOrder.getPaymentType(),
                detailContent
        );
    }
}