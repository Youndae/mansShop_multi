package com.example.moduletest.fixture;

import com.example.modulecommon.model.entity.Product;
import com.example.modulecommon.model.entity.ProductOption;
import com.example.moduleorder.model.dto.in.OrderProductDTO;

import java.util.List;
import java.util.Map;

public record MemberPaymentMapDTO(
        int totalPrice,
        int totalCount,
        List<OrderProductDTO> orderProductFixtureList,
        Map<String, Product> paymentProductMap,
        Map<String, Long> paymentProductSalesQuantityMap,
        Map<Long, ProductOption> paymentProductOptionMap,
        Map<Long, Long> paymentProductOptionStockMap
) {
}
