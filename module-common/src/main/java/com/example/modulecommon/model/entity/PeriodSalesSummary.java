package com.example.modulecommon.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Table(name = "periodSalesSummary")
public class PeriodSalesSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "DATE")
    private LocalDate period;

    @Column(nullable = false)
    private long sales;

    @Column(nullable = false)
    private long salesQuantity;

    @Column(nullable = false)
    private long orderQuantity;

    @Column(columnDefinition = "BIGINT DEFAULT 0",
            nullable = false)
    private long totalDeliveryFee;

    @Column(columnDefinition = "BIGINT DEFAULT 0",
            nullable = false)
    private long cashTotal;

    @Column(columnDefinition = "BIGINT DEFAULT 0",
            nullable = false)
    private long cardTotal;

    public void setPatchData(PeriodSalesSummary dummyEntity) {
        this.period = dummyEntity.getPeriod();
        this.sales += dummyEntity.getSales();
        this.salesQuantity += dummyEntity.getSalesQuantity();
        this.orderQuantity += 1;
        this.totalDeliveryFee += dummyEntity.getTotalDeliveryFee();
        this.cashTotal += dummyEntity.getCashTotal();
        this.cardTotal += dummyEntity.getCardTotal();
    }
}
