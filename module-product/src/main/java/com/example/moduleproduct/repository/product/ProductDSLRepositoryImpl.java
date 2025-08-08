package com.example.moduleproduct.repository.product;


import com.example.modulecommon.model.entity.Product;
import com.example.moduleproduct.model.dto.admin.product.business.AdminProductStockDataDTO;
import com.example.moduleproduct.model.dto.admin.product.in.AdminDiscountPatchDTO;
import com.example.moduleproduct.model.dto.admin.product.out.AdminDiscountProductDTO;
import com.example.moduleproduct.model.dto.admin.product.out.AdminProductListDTO;
import com.example.moduleproduct.model.dto.main.business.MainListDTO;
import com.example.moduleproduct.model.dto.page.AdminProductPageDTO;
import com.example.moduleproduct.model.dto.page.MainPageDTO;
import com.example.moduleproduct.model.dto.product.business.ProductIdClassificationDTO;
import com.querydsl.core.types.*;
import com.querydsl.core.types.dsl.*;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    @Override
    @Transactional
    public void patchProductSalesQuantity(Map<String, Integer> productMap) {
        CaseBuilder caseBuilder = new CaseBuilder();
        CaseBuilder.Cases<Integer, NumberExpression<Integer>> caseExpression = null;
        List<String> productIds = new ArrayList<>();

        for(Map.Entry<String, Integer> entry : productMap.entrySet()) {
            productIds.add(entry.getKey());

            if(caseExpression == null)
                caseExpression = caseBuilder.when(product.id.eq(entry.getKey()))
                        .then(entry.getValue());
            else
                caseExpression = caseExpression.when(product.id.eq(entry.getKey()))
                        .then(entry.getValue());
        }

        NumberExpression<Integer> salesExpression = caseExpression.otherwise(0);

        jpaQueryFactory.update(product)
                .set(product.productSalesQuantity, product.productSalesQuantity.add(salesExpression))
                .where(product.id.in(productIds))
                .execute();
    }

    @Override
    public List<ProductIdClassificationDTO> findClassificationAllByProductIds(List<String> productIds) {
        return jpaQueryFactory.select(
                        Projections.constructor(
                                ProductIdClassificationDTO.class,
                                product.id.as("productId"),
                                product.classification.id.as("classificationId")
                        )
                )
                .from(product)
                .where(product.id.in(productIds))
                .fetch();
    }

    private BooleanExpression adminProductSearch(AdminProductPageDTO pageDTO) {
        if(pageDTO.keyword() != null){
            return product.productName.like(pageDTO.keyword());
        }else {
            return null;
        }
    }

    @Override
    public List<AdminProductListDTO> findAdminProductPageList(AdminProductPageDTO pageDTO) {
        return jpaQueryFactory.select(
                        Projections.constructor(
                                AdminProductListDTO.class,
                                product.id.as("productId"),
                                product.classification.id.as("classification"),
                                product.productName,
                                ExpressionUtils.as(
                                        JPAExpressions.select(
                                                        productOption.stock.sum()
                                                )
                                                .from(productOption)
                                                .where(productOption.product.id.eq(product.id))
                                                .groupBy(productOption.product.id), "stock"
                                ),
                                ExpressionUtils.as(
                                        JPAExpressions.select(
                                                        productOption.id.count()
                                                )
                                                .from(productOption)
                                                .where(productOption.product.id.eq(product.id))
                                                .groupBy(productOption.product.id), "optionCount"
                                ),
                                product.productPrice.as("price")
                        )
                )
                .from(product)
                .where(adminProductSearch(pageDTO))
                .offset(pageDTO.offset())
                .limit(pageDTO.amount())
                .orderBy(product.createdAt.desc())
                .fetch();
    }

    @Override
    public Long findAdminProductListCount(AdminProductPageDTO pageDTO) {
        return jpaQueryFactory.select(product.id.countDistinct())
                .from(product)
                .where(adminProductSearch(pageDTO))
                .fetchOne();
    }

    @Override
    public List<AdminProductStockDataDTO> findAllProductStockData(AdminProductPageDTO pageDTO) {
        NumberPath<Integer> aliasSum = Expressions.numberPath(Integer.class, "totalStock");

        return jpaQueryFactory.select(
                        Projections.constructor(
                                AdminProductStockDataDTO.class
                                , product.id.as("productId")
                                , product.classification.id.as("classification")
                                , product.productName
                                , ExpressionUtils.as(
                                        JPAExpressions.select(productOption.stock.sum())
                                                .from(productOption)
                                                .where(productOption.product.id.eq(product.id))
                                                .groupBy(product.id), aliasSum
                                )
                                , product.isOpen.as("isOpen")
                        )
                )
                .from(product)
                .where(adminProductSearch(pageDTO))
                .orderBy(aliasSum.asc())
                .orderBy(product.id.asc())
                .offset(pageDTO.offset())
                .limit(pageDTO.amount())
                .fetch();
    }

    @Override
    public Long findAllProductStockDataCount(AdminProductPageDTO pageDTO) {
        return jpaQueryFactory.select(product.countDistinct())
                .from(product)
                .where(adminProductSearch(pageDTO))
                .fetchOne();
    }

    @Override
    public Page<Product> getDiscountProductList(AdminProductPageDTO pageDTO, Pageable pageable) {
        List<Product> list = jpaQueryFactory.select(product)
                .from(product)
                .where(product.productDiscount.ne(0).and(adminProductSearch(pageDTO)))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(product.updatedAt.desc())
                .fetch();

        JPAQuery<Long> count = jpaQueryFactory.select(product.countDistinct())
                .from(product)
                .where(product.productDiscount.ne(0).and(adminProductSearch(pageDTO)));

        return PageableExecutionUtils.getPage(list, pageable, count::fetchOne);
    }

    @Override
    public List<AdminDiscountProductDTO> findAllProductByClassificationId(String classificationId) {
        return jpaQueryFactory.select(
                        Projections.constructor(
                                AdminDiscountProductDTO.class
                                , product.id.as("productId")
                                , product.productName.as("productName")
                                , product.productPrice.as("productPrice")
                        )
                )
                .from(product)
                .where(product.classification.id.eq(classificationId))
                .orderBy(product.productName.asc())
                .fetch();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void patchProductDiscount(AdminDiscountPatchDTO patchDTO) {
        jpaQueryFactory.update(product)
                .set(product.productDiscount, patchDTO.discount())
                .where(product.id.in(patchDTO.productIdList()))
                .execute();
    }
}
