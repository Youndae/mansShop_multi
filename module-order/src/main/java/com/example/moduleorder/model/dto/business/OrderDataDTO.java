package com.example.moduleorder.model.dto.business;

import com.example.moduleorder.model.vo.OrderItemVO;
import com.example.moduleproduct.model.dto.product.business.OrderProductInfoDTO;

public record OrderDataDTO(
        String productId,
        long optionId,
        String productName,
        String size,
        String color,
        int count,
        int price
) {

    public OrderDataDTO(OrderProductInfoDTO infoDTO, int count) {
        this(
                infoDTO.productId(),
                infoDTO.optionId(),
                infoDTO.productName(),
                infoDTO.size(),
                infoDTO.color(),
                count,
                infoDTO.price() * count
        );
    }

    public OrderItemVO toOrderItemVO() {
        return new OrderItemVO(productId, optionId, count, price);
    }
}
