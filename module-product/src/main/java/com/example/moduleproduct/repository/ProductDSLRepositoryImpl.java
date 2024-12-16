package com.example.moduleproduct.repository;

import com.example.moduleproduct.model.dto.main.business.MainListDTO;
import com.example.moduleproduct.model.dto.page.ProductPageDTO;
import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.example.modulecommon.model.entity.QProduct.product;
import static com.example.modulecommon.model.entity.QProductOption.productOption;

@Repository
@RequiredArgsConstructor
public class ProductDSLRepositoryImpl implements ProductDSLRepository{

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<MainListDTO> getProductDefaultList(ProductPageDTO pageDTO) {
        return jpaQueryFactory.select(getProductProjections())
                .from(product)
                .innerJoin(productOption)
                .on(product.id.eq(productOption.product.id))
                .where(product.isOpen.isTrue())
                .orderBy(productListOrderBy(pageDTO))
                .groupBy(product.id)
                .limit(pageDTO.mainProductAmount())
                .fetch();
    }

    @Override
    public Page<MainListDTO> getProductClassificationAndSearchList(ProductPageDTO pageDTO, Pageable pageable) {
        List<MainListDTO> list = jpaQueryFactory.select(getProductProjections())
                                                .from(product)
                                                .innerJoin(productOption)
                                                .on(product.id.eq(productOption.product.id))
                                                .where(productSearchType(pageDTO))
                                                .orderBy(productListOrderBy(pageDTO))
                                                .groupBy(product.id)
                                                .offset(pageable.getOffset())
                                                .limit(pageable.getPageSize())
                                                .fetch();

        JPAQuery<Long> count = jpaQueryFactory.select(product.countDistinct())
                                .from(product)
                                .where(
                                        product.isOpen.eq(true)
                                                .and(productSearchType(pageDTO))
                                );

        return PageableExecutionUtils.getPage(list, pageable, count::fetchOne);
    }

    private ConstructorExpression<MainListDTO> getProductProjections() {
        return Projections.constructor(
                        MainListDTO.class,
                        product.id.as("productId"),
                        product.productName,
                        product.thumbnail,
                        product.productPrice.as("price"),
                        product.productDiscount.as("discount"),
                        productOption.stock.longValue().sum().as("stock")
                );
    }

    private OrderSpecifier[] productListOrderBy(ProductPageDTO pageDTO) {
        if(pageDTO.classification() != null && pageDTO.classification().equals("BEST"))
            return new OrderSpecifier[]{new OrderSpecifier<>(Order.DESC, product.productSales)};
        else{
            return new OrderSpecifier[]{
                    new OrderSpecifier<>(Order.DESC, product.createdAt),
                    new OrderSpecifier<>(Order.DESC, product.id)
            };
        }
    }

    private BooleanExpression productSearchType(ProductPageDTO pageDTO) {
        if(pageDTO.classification().equals("BEST") || pageDTO.classification().equals("NEW"))
            return product.isOpen.isTrue();
        else if(pageDTO.keyword() == null)
            return product.isOpen.isTrue().and(product.classification.id.eq(pageDTO.classification()));
        else
            return product.isOpen.isTrue().and(product.productName.like("%" + pageDTO.keyword() + "%"));
    }
}
