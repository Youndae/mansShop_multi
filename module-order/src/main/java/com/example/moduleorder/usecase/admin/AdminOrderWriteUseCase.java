package com.example.moduleorder.usecase.admin;

import com.example.modulecommon.model.entity.ProductOrder;
import com.example.modulecommon.model.enumuration.OrderStatus;
import com.example.modulecommon.model.enumuration.Result;
import com.example.moduleorder.service.OrderDataService;
import com.example.moduleorder.service.OrderExternalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminOrderWriteUseCase {

    private final OrderDataService orderDataService;

    private final OrderExternalService orderExternalService;

    public String orderPreparation(long orderId) {
        ProductOrder productOrder = orderDataService.findProductOrderByIdOrElseIllegal(orderId);
        productOrder.setOrderStat(OrderStatus.PREPARATION.getStatusStr());

        orderDataService.saveProductOrder(productOrder);

        orderExternalService.sendOrderNotification(productOrder.getMember().getUserId());

        return Result.OK.getResultKey();
    }
}
