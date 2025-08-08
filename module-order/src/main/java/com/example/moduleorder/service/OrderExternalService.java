package com.example.moduleorder.service;

import com.example.modulecart.model.dto.business.CartMemberDTO;
import com.example.modulecommon.model.dto.notification.NotificationSendDTO;
import com.example.modulecommon.model.entity.ProductOrder;
import com.example.modulecommon.model.enumuration.FallbackMapKey;
import com.example.modulecommon.model.enumuration.NotificationType;
import com.example.moduleconfig.config.rabbitMQ.RabbitMQPrefix;
import com.example.moduleconfig.properties.RabbitMQProperties;
import com.example.moduleorder.model.dto.business.FailedOrderDTO;
import com.example.moduleorder.model.dto.rabbitMQ.OrderCartDTO;
import com.example.moduleorder.model.dto.business.ProductOrderDataDTO;
import com.example.moduleorder.model.dto.in.PaymentDTO;
import com.example.moduleorder.model.dto.rabbitMQ.OrderProductMessageDTO;
import com.example.moduleorder.model.dto.rabbitMQ.OrderProductSummaryDTO;
import com.example.moduleorder.model.dto.rabbitMQ.PeriodSummaryQueueDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderExternalService {

    private final RabbitMQProperties rabbitMQProperties;

    private final RabbitTemplate rabbitTemplate;

    public void sendOrderMessageQueue(PaymentDTO paymentDTO, CartMemberDTO cartMemberDTO, ProductOrderDataDTO productOrderDataDTO, ProductOrder order) {
        String orderExchange = getOrderExchange();

        if(paymentDTO.orderType().equals("cart"))
            sendMessage(orderExchange, RabbitMQPrefix.QUEUE_ORDER_CART, new OrderCartDTO(cartMemberDTO, productOrderDataDTO.orderOptionIds()));

        sendMessage(orderExchange, RabbitMQPrefix.QUEUE_ORDER_PRODUCT_OPTION, new OrderProductMessageDTO(productOrderDataDTO));
        sendMessage(orderExchange, RabbitMQPrefix.QUEUE_ORDER_PRODUCT, new OrderProductMessageDTO(productOrderDataDTO));
        sendMessage(orderExchange, RabbitMQPrefix.QUEUE_PERIOD_SUMMARY, new PeriodSummaryQueueDTO(order));
        sendMessage(orderExchange, RabbitMQPrefix.QUEUE_PRODUCT_SUMMARY, new OrderProductSummaryDTO(productOrderDataDTO));
    }

    private String getOrderExchange() {
        return rabbitMQProperties.getExchange()
                .get(RabbitMQPrefix.EXCHANGE_ORDER.getKey())
                .getName();
    }

    private String getQueueRoutingKey(RabbitMQPrefix rabbitMQPrefix) {
        return rabbitMQProperties.getQueue()
                .get(rabbitMQPrefix.getKey())
                .getRouting();
    }

    private <T> void sendMessage(String exchange, RabbitMQPrefix rabbitMQPrefix, T data) {
        rabbitTemplate.convertAndSend(
                exchange,
                getQueueRoutingKey(rabbitMQPrefix),
                data
        );
    }

    public void retryFailedOrder(FailedOrderDTO failedOrderDTO,
                                 ProductOrderDataDTO productOrderDataDTO,
                                 ProductOrder order,
                                 FallbackMapKey fallbackMapKey) {
        if(fallbackMapKey == FallbackMapKey.ORDER)
            sendMessage(getOrderExchange(), RabbitMQPrefix.QUEUE_FAILED_ORDER, productOrderDataDTO);

        sendOrderMessageQueue(failedOrderDTO.paymentDTO(), failedOrderDTO.cartMemberDTO(), productOrderDataDTO, order);

    }

    public void sendOrderNotification(String userId) {
        rabbitTemplate.convertAndSend(
                rabbitMQProperties.getExchange().get(RabbitMQPrefix.EXCHANGE_NOTIFICATION.getKey()).getName(),
                rabbitMQProperties.getQueue().get(RabbitMQPrefix.QUEUE_NOTIFICATION.getKey()).getRouting(),
                new NotificationSendDTO(
                        userId,
                        NotificationType.ORDER_STATUS,
                        NotificationType.ORDER_STATUS.getTitle(),
                        null
                )
        );
    }
}
