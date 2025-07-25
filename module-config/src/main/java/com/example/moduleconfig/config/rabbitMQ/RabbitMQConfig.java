package com.example.moduleconfig.config.rabbitMQ;

import com.example.moduleconfig.properties.RabbitMQProperties;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import java.util.Map;

@Configuration
public class RabbitMQConfig {

    private final Map<String, RabbitMQProperties.Exchange> exchange;

    private final Map<String, RabbitMQProperties.Queue> queue;

    public RabbitMQConfig(RabbitMQProperties rabbitMQProperties) {
        this.exchange = rabbitMQProperties.getExchange();
        this.queue = rabbitMQProperties.getQueue();
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        ObjectMapper om = new ObjectMapper();
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 이 설정을 하지 않으면 LocalDate에 대해 [2025, 2, 1] 이런식으로 처리된다. 반면, 이 설정을 하면 "2025-02-01"로 되기 떄문에 정상적인 매핑이 가능.
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        om.registerModule(new JavaTimeModule());

        return new Jackson2JsonMessageConverter(om);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jackson2JsonMessageConverter());

        return rabbitTemplate;
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @DependsOn("orderDLQExchange")
    private Queue createQueueWithDLQExchange(String queueKey, String dlqExchangeKey) {
        return QueueBuilder.durable(queue.get(queueKey).getName())
                .withArgument("x-dead-letter-exchange", exchange.get(dlqExchangeKey).getDlq())
                .withArgument("x-dead-letter-routing-key", queue.get(queueKey).getDlqRouting())
                .build();
    }

    private Queue createQueue(String queueName) {
        return QueueBuilder.durable(queueName).build();
    }

    private DirectExchange createExchange(String exchangeName, boolean durable) {
        return ExchangeBuilder.directExchange(exchangeName)
                .durable(durable)
                .build();
    }

    private Binding setBinding(Queue bindingQueue, DirectExchange exchange, String routingKey) {
        return BindingBuilder.bind(bindingQueue)
                .to(exchange)
                .with(routingKey);
    }

    @Bean
    public Queue orderProductQueue() {
        return createQueueWithDLQExchange(RabbitMQPrefix.QUEUE_ORDER_PRODUCT.getKey(), RabbitMQPrefix.EXCHANGE_ORDER.getKey());
    }

    @Bean
    public Queue orderProductOptionQueue() {
        return createQueueWithDLQExchange(RabbitMQPrefix.QUEUE_ORDER_PRODUCT_OPTION.getKey(), RabbitMQPrefix.EXCHANGE_ORDER.getKey());
    }

    @Bean
    public Queue periodSummaryQueue() {
        return createQueueWithDLQExchange(RabbitMQPrefix.QUEUE_PERIOD_SUMMARY.getKey(), RabbitMQPrefix.EXCHANGE_ORDER.getKey());
    }

    @Bean
    public Queue productSummaryQueue() {
        return createQueueWithDLQExchange(RabbitMQPrefix.QUEUE_PRODUCT_SUMMARY.getKey(), RabbitMQPrefix.EXCHANGE_ORDER.getKey());
    }

    @Bean
    public Queue orderCartQueue() {
        return createQueueWithDLQExchange(RabbitMQPrefix.QUEUE_ORDER_CART.getKey(), RabbitMQPrefix.EXCHANGE_ORDER.getKey());
    }

    @Bean
    public Queue failedOrderQueue() {
        return createQueueWithDLQExchange(RabbitMQPrefix.QUEUE_FAILED_ORDER.getKey(), RabbitMQPrefix.EXCHANGE_ORDER.getKey());
    }

    @Bean
    public Queue notificationQueue() {
        return createQueueWithDLQExchange(RabbitMQPrefix.QUEUE_NOTIFICATION.getKey(), RabbitMQPrefix.EXCHANGE_NOTIFICATION.getKey());
    }

    @Bean
    public DirectExchange orderExchange() {
        return createExchange(exchange.get(RabbitMQPrefix.EXCHANGE_ORDER.getKey()).getName(), true);
    }

    @Bean
    public DirectExchange notificationExchange() {
        return createExchange(exchange.get(RabbitMQPrefix.EXCHANGE_NOTIFICATION.getKey()).getName(), true);
    }

    @Bean
    public Binding orderProductBinding() {

        return setBinding(orderProductQueue(),
                orderExchange(),
                queue.get(RabbitMQPrefix.QUEUE_ORDER_PRODUCT.getKey()).getRouting()
        );
    }

    @Bean
    public Binding orderProductOptionBinding() {

        return setBinding(orderProductOptionQueue(),
                orderExchange(),
                queue.get(RabbitMQPrefix.QUEUE_ORDER_PRODUCT_OPTION.getKey()).getRouting()
        );
    }

    @Bean
    public Binding periodSummaryBinding() {

        return setBinding(periodSummaryQueue(),
                orderExchange(),
                queue.get(RabbitMQPrefix.QUEUE_PERIOD_SUMMARY.getKey()).getRouting()
        );
    }

    @Bean
    public Binding productSummaryBinding() {

        return setBinding(productSummaryQueue(),
                orderExchange(),
                queue.get(RabbitMQPrefix.QUEUE_PRODUCT_SUMMARY.getKey()).getRouting()
        );
    }

    @Bean
    public Binding orderCartBinding() {

        return setBinding(orderCartQueue(),
                orderExchange(),
                queue.get(RabbitMQPrefix.QUEUE_ORDER_CART.getKey()).getRouting()
        );
    }

    @Bean
    public Binding failedOrderBinding() {
        return setBinding(failedOrderQueue(),
                orderExchange(),
                queue.get(RabbitMQPrefix.QUEUE_FAILED_ORDER.getKey()).getRouting()
        );
    }

    @Bean
    public Binding notificationBinding() {
        return setBinding(notificationQueue(),
                notificationExchange(),
                queue.get(RabbitMQPrefix.QUEUE_NOTIFICATION.getKey()).getRouting()
        );
    }

    @Bean
    public Queue orderProductDLQ() {
        return createQueue(queue.get(RabbitMQPrefix.QUEUE_ORDER_PRODUCT.getKey()).getDlq());
    }

    @Bean
    public Queue orderProductOptionDLQ() {
        return createQueue(queue.get(RabbitMQPrefix.QUEUE_ORDER_PRODUCT_OPTION.getKey()).getDlq());
    }

    @Bean
    public Queue periodSummaryDLQ() {
        return createQueue(queue.get(RabbitMQPrefix.QUEUE_PERIOD_SUMMARY.getKey()).getDlq());
    }

    @Bean
    public Queue productSummaryDLQ() {
        return createQueue(queue.get(RabbitMQPrefix.QUEUE_PRODUCT_SUMMARY.getKey()).getDlq());
    }

    @Bean
    public Queue orderCartDLQ() {
        return createQueue(queue.get(RabbitMQPrefix.QUEUE_ORDER_CART.getKey()).getDlq());
    }

    @Bean
    public Queue failedOrderDLQ() {
        return createQueue(queue.get(RabbitMQPrefix.QUEUE_FAILED_ORDER.getKey()).getDlq());
    }

    @Bean
    public Queue notificationDLQ() {
        return createQueue(queue.get(RabbitMQPrefix.QUEUE_NOTIFICATION.getKey()).getDlq());
    }

    @Bean
    public DirectExchange orderDLQExchange() {
        return createExchange(exchange.get(RabbitMQPrefix.EXCHANGE_ORDER.getKey()).getDlq(), true);
    }

    @Bean
    public DirectExchange notificationDLQExchange() {
        return createExchange(exchange.get(RabbitMQPrefix.EXCHANGE_NOTIFICATION.getKey()).getDlq(), true);
    }

    @Bean
    public Binding orderProductDLQBinding() {

        return setBinding(orderProductDLQ(),
                orderDLQExchange(),
                queue.get(RabbitMQPrefix.QUEUE_ORDER_PRODUCT.getKey()).getDlqRouting()
        );
    }

    @Bean
    public Binding orderProductOptionDLQBinding() {

        return setBinding(orderProductOptionDLQ(),
                orderDLQExchange(),
                queue.get(RabbitMQPrefix.QUEUE_ORDER_PRODUCT_OPTION.getKey()).getDlqRouting()
        );
    }

    @Bean
    public Binding periodSummaryDLQBinding() {

        return setBinding(periodSummaryDLQ(),
                orderDLQExchange(),
                queue.get(RabbitMQPrefix.QUEUE_PERIOD_SUMMARY.getKey()).getDlqRouting()
        );
    }

    @Bean
    public Binding productSummaryDLQBinding() {

        return setBinding(productSummaryDLQ(),
                orderDLQExchange(),
                queue.get(RabbitMQPrefix.QUEUE_PRODUCT_SUMMARY.getKey()).getDlqRouting()
        );
    }

    @Bean
    public Binding orderCartDLQBinding() {

        return setBinding(orderCartDLQ(),
                orderDLQExchange(),
                queue.get(RabbitMQPrefix.QUEUE_ORDER_CART.getKey()).getDlqRouting()
        );
    }

    @Bean
    public Binding failedOrderDLQBinding() {

        return setBinding(failedOrderDLQ(),
                orderDLQExchange(),
                queue.get(RabbitMQPrefix.QUEUE_FAILED_ORDER.getKey()).getDlqRouting()
        );
    }

    @Bean
    public Binding notificationDLQBinding() {
        return setBinding(notificationDLQ(),
                notificationDLQExchange(),
                queue.get(RabbitMQPrefix.QUEUE_NOTIFICATION.getKey()).getDlqRouting()
        );
    }
}
