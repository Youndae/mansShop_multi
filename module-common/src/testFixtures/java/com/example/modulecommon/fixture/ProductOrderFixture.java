package com.example.modulecommon.fixture;

import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.entity.ProductOption;
import com.example.modulecommon.model.entity.ProductOrder;
import com.example.modulecommon.model.entity.ProductOrderDetail;
import com.example.modulecommon.model.enumuration.OrderStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ProductOrderFixture {

    private static int randomInt() {
        Random ran = new Random();

        return ran.nextInt(4) + 1;
    }

    //저장하지 않은 상태의 ProductOrder, ProductOrderDetail 데이터 생성
    public static List<ProductOrder> createDefaultProductOrder(List<Member> members, List<ProductOption> options) {
        List<ProductOrder> result = new ArrayList<>();
        for(Member m : members) {
            int optionCount = randomInt();
            int productCount = 0;
            int totalPrice = 0;
            int phoneSuffix = 2345;
            List<ProductOrderDetail> details = new ArrayList<>();
            for(int i = 0; i < optionCount; i++) {
                details.add(
                        ProductOrderDetail.builder()
                                .productOption(options.get(i))
                                .product(options.get(i).getProduct())
                                .orderDetailCount(i + 1)
                                .orderDetailPrice((i + 1) * 10000)
                                .build()
                );
                productCount += i + 1;
                totalPrice += (i + 1) * 10000;
            }

            ProductOrder order = ProductOrder.builder()
                    .member(m)
                    .recipient(m.getUserName())
                    .orderPhone("010-1234-" + phoneSuffix++)
                    .orderAddress(m.getUserName() + " address")
                    .orderMemo(m.getUserName() + " memo")
                    .orderTotalPrice(totalPrice)
                    .deliveryFee(totalPrice < 100000 ? 3500 : 0)
                    .paymentType("card")
                    .orderStat(OrderStatus.ORDER.getStatusStr())
                    .productCount(productCount)
                    .createdAt(LocalDateTime.now().minusDays(1))
                    .build();

            details.forEach(order::addDetail);
            result.add(order);
        }

        return result;
    }
}
