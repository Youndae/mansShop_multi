package com.example.moduleorder.model.dto.business;

import com.example.modulecommon.model.entity.ProductOrder;
import com.example.moduleorder.model.dto.in.OrderProductDTO;

import java.util.List;

public record ProductOrderDataDTO(
        ProductOrder productOrder,
        List<OrderProductDTO> orderProductList,
        List<String> orderProductIds,
        List<Long> orderOptionIds
){
}
