package com.example.moduleadmin.repository;

import com.example.modulecommon.model.entity.ProductSalesSummary;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

import static com.example.modulecommon.model.entity.QProductSalesSummary.productSalesSummary;
import static com.example.modulecommon.model.entity.QProductOption.productOption;
import static com.example.modulecommon.model.entity.QClassification.classification;
import static com.example.modulecommon.model.entity.QProduct.product;

@Repository
@RequiredArgsConstructor
public class ProductSalesSummaryDSLRepositoryImpl implements ProductSalesSummaryDSLRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<ProductSalesSummary> findAllByProductOptionIds(LocalDate periodMonth, List<Long> productOptionIds) {
        return jpaQueryFactory.select(productSalesSummary)
                .from(productSalesSummary)
                .innerJoin(productSalesSummary.productOption, productOption).fetchJoin()
                .innerJoin(productSalesSummary.classification, classification).fetchJoin()
                .innerJoin(productSalesSummary.product, product).fetchJoin()
                .where(
                        productSalesSummary.periodMonth.eq(periodMonth)
                                .and(productSalesSummary.productOption.id.in(productOptionIds))
                )
                .fetch();
    }
}
