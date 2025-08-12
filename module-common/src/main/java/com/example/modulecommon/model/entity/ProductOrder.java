package com.example.modulecommon.model.entity;

import com.example.modulecommon.utils.PhoneNumberUtils;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "productOrder")
public class ProductOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "userId", nullable = false)
    private Member member;

    @Column(length = 50,
            nullable = false
    )
    private String recipient;

    @Column(length = 100,
            nullable = false
    )
    private String orderPhone;

    @Column(length = 200,
            nullable = false
    )
    private String orderAddress;

    @Column(length = 200)
    private String orderMemo;

    @Column(nullable = false)
    private int orderTotalPrice;

    private int deliveryFee;

    @Column(nullable = false, columnDefinition = "DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3)")
    private LocalDateTime createdAt;

    @Column(length = 10,
            nullable = false
    )
    private String paymentType;

    @Column(length = 20,
            nullable = false
    )
    private String orderStat;

    @Column(nullable = false)
    private int productCount;

    @OneToMany(mappedBy = "productOrder", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private final List<ProductOrderDetail> productOrderDetailList = new ArrayList<>();

    public void addDetail(ProductOrderDetail productOrderDetail) {
        productOrderDetailList.add(productOrderDetail);
        productOrderDetail.setProductOrder(this);
    }

    public void setOrderStat(String orderStat) {
        this.orderStat = orderStat;
    }

    public void setProductCount(int productCount) {
        this.productCount = productCount;
    }
}
