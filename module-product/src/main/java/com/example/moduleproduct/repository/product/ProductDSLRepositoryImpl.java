package com.example.moduleproduct.repository.product;

import com.example.moduleproduct.model.dto.main.business.MainListDTO;
import com.example.moduleproduct.model.dto.page.MainPageDTO;
import com.querydsl.core.types.*;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
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
public class ProductDSLRepositoryImpl implements ProductDSLRepository {

    private final JPAQueryFactory jpaQueryFactory;

    /**
     *
     * @param pageDTO
     * @return
     *
     * 메인 페이지 중 BEST, NEW에 대한 조회
     * 상품 12개 정보만 필요하기 때문에 페이징 사용하지 않을 것이므로 Page 타입이 아닌 List 타입으로.
     */
    @Override
    public List<MainListDTO> findListDefault(MainPageDTO pageDTO) {

        return jpaQueryFactory.select(
                        Projections.constructor(
                                MainListDTO.class,
                                product.id.as("productId"),
                                product.productName,
                                product.thumbnail,
                                product.productPrice.as("price"),
                                product.productDiscount.as("discount"),
                                ExpressionUtils.as(
                                        JPAExpressions.select(
                                                        productOption.stock
                                                                .longValue()
                                                                .sum()
                                                )
                                                .from(productOption)
                                                .where(productOption.product.id.eq(product.id))
                                                .groupBy(productOption.product.id)
                                        , "stock"
                                )
                        )
                )
                .from(product)
                .where(product.isOpen.eq(true))
                .orderBy(defaultListOrderBy(pageDTO.classification()), product.id.desc())
                .limit(pageDTO.amount())
                .fetch();
    }


    @Override
    public Page<MainListDTO> findListPageable(MainPageDTO pageDTO, Pageable pageable) {

        List<MainListDTO> list = jpaQueryFactory.select(
                        Projections.constructor(
                                MainListDTO.class
                                , product.id.as("productId")
                                , product.productName
                                , product.thumbnail
                                , product.productPrice.as("price")
                                , product.productDiscount.as("discount")
                                , ExpressionUtils.as(
                                        JPAExpressions.select(
                                                        productOption.stock
                                                                .longValue()
                                                                .sum()
                                                )
                                                .from(productOption)
                                                .where(productOption.product.id.eq(product.id))
                                                .groupBy(productOption.product.id)
                                        , "stock"
                                )
                        )
                )
                .from(product)
                .where(
                        product.isOpen.eq(true)
                                .and(searchType(pageDTO.classification(), pageDTO.keyword()))
                )
                .orderBy(defaultListOrderBy(pageDTO.classification()), product.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> count = jpaQueryFactory.select(product.countDistinct())
                .from(product)
                .where(
                        product.isOpen.eq(true)
                                .and(searchType(pageDTO.classification(), pageDTO.keyword()))
                );



        return PageableExecutionUtils.getPage(list, pageable, count::fetchOne);
    }

    private OrderSpecifier<?> defaultListOrderBy(String classification){

        if(classification != null && classification.equals("BEST"))
            return new OrderSpecifier<>(Order.DESC, product.productSalesQuantity);
        else
            return new OrderSpecifier<>(Order.DESC, product.createdAt);

    }

    /**
     *
     * @param classification
     * @param keyword
     * @return
     *
     * classification이 존재하면 keyword는 존재하지 않고
     * Keyword가 존재한다면 classification은 존재하지 않는다.
     */
    private BooleanExpression searchType(String classification, String keyword) {

        if(classification != null)
            return product.classification.id.eq(classification);
        else if(keyword != null) {
            return product.productName.like(keyword);
        }else
            return null;
    }
}
