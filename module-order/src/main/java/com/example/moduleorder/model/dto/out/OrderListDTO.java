package com.example.moduleorder.model.dto.out;

import com.example.modulecommon.model.entity.ProductOrder;
import com.example.moduleorder.model.dto.business.OrderListDetailDTO;

import java.time.LocalDateTime;
import java.util.List;

public record OrderListDTO(
        long orderId,
        int orderTotalPrice,
        LocalDateTime orderDate,
        String orderStat,
        List<OrderListDetailDTO> detail
) {

    public OrderListDTO(ProductOrder data, List<OrderListDetailDTO> detail) {
        this(
                data.getId(),
                data.getOrderTotalPrice(),
                data.getCreatedAt(),
                data.getOrderStat(),
                detail
        );
    }
}
