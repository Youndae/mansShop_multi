package com.example.moduleorder.model.dto.rabbitMQ;

import com.example.modulecommon.model.entity.PeriodSalesSummary;
import com.example.modulecommon.model.entity.ProductOrder;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PeriodSummaryQueueDTO {

    private LocalDate period;
    private long sales;
    private long salesQuantity;
    private long totalDeliveryFee;
    private long cashSales;
    private long cardSales;

    public PeriodSummaryQueueDTO(ProductOrder productOrder) {
        this.period = LocalDate.now();
        this.sales = productOrder.getOrderTotalPrice();
        this.salesQuantity = productOrder.getProductCount();
        this.totalDeliveryFee = productOrder.getDeliveryFee();
        this.cashSales = productOrder.getPaymentType().equals("cash") ? productOrder.getOrderTotalPrice() : 0L;
        this.cardSales = productOrder.getPaymentType().equals("card") ? productOrder.getOrderTotalPrice() : 0L;
    }

    public PeriodSalesSummary toEntity() {
        return PeriodSalesSummary.builder()
                .period(this.period)
                .sales(this.sales)
                .salesQuantity(this.salesQuantity)
                .orderQuantity(1L)
                .totalDeliveryFee(this.totalDeliveryFee)
                .cashTotal(cashSales)
                .cardTotal(cardSales)
                .build();
    }
}
