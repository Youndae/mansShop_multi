package com.example.modulecommon.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductOrderDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "productOptionId")
    private ProductOption productOption;

    @ManyToOne
    @JoinColumn(name = "productId")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "orderId")
    private ProductOrder productOrder;

    private int orderDetailCount;

    private int orderDetailPrice;

    private boolean orderReviewStatus;

    public void setProductOrder(ProductOrder productOrder) {
        this.productOrder = productOrder;
    }

    public void setOrderReviewStatus(boolean orderReviewStatus) {
        this.orderReviewStatus = orderReviewStatus;
    }
}
