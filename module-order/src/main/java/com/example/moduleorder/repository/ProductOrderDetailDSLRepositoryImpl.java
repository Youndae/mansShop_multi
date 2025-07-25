package com.example.moduleorder.repository;

import com.example.moduleorder.model.dto.business.OrderListDetailDTO;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.example.modulecommon.model.entity.QProductOrder.productOrder;
import static com.example.modulecommon.model.entity.QProductOrderDetail.productOrderDetail;
import static com.example.modulecommon.model.entity.QProduct.product;
import static com.example.modulecommon.model.entity.QProductOption.productOption;

@Repository
@RequiredArgsConstructor
public class ProductOrderDetailDSLRepositoryImpl implements ProductOrderDetailDSLRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<OrderListDetailDTO> findAllOrderDetailByOrderIds(List<Long> orderIds) {
        return jpaQueryFactory.select(
                        Projections.constructor(
                                OrderListDetailDTO.class,
                                productOrder.id.as("orderId"),
                                product.id.as("productId"),
                                productOrderDetail.productOption.id.as("optionId"),
                                productOrderDetail.id.as("detailId"),
                                product.productName,
                                productOption.size,
                                productOption.color,
                                productOrderDetail.orderDetailCount.as("detailCount"),
                                productOrderDetail.orderDetailPrice.as("detailPrice"),
                                productOrderDetail.orderReviewStatus.as("reviewStatus"),
                                product.thumbnail
                        )
                )
                .from(productOrderDetail)
                .innerJoin(productOrderDetail.productOrder, productOrder)
                .innerJoin(productOrderDetail.productOption, productOption)
                .innerJoin(productOrderDetail.product, product)
                .where(productOrderDetail.productOrder.id.in(orderIds))
                .orderBy(productOrderDetail.product.id.asc())
                .fetch();
    }
}
