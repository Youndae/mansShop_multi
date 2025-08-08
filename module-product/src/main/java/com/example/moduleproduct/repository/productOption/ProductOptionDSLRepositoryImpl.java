package com.example.moduleproduct.repository.productOption;

import com.example.moduleproduct.model.dto.admin.product.business.AdminOptionStockDTO;
import com.example.moduleproduct.model.dto.admin.product.out.AdminProductOptionDTO;
import com.example.moduleproduct.model.dto.product.business.OrderProductInfoDTO;
import com.example.moduleproduct.model.dto.product.business.PatchOrderStockDTO;
import com.example.moduleproduct.model.dto.product.business.ProductOptionDTO;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.modulecommon.model.entity.QProductOption.productOption;
import static com.example.modulecommon.model.entity.QProduct.product;

@Repository
@RequiredArgsConstructor
public class ProductOptionDSLRepositoryImpl implements ProductOptionDSLRepository{

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<ProductOptionDTO> findByDetailOption(String productId) {
        return jpaQueryFactory
                .select(
                        Projections.constructor(
                                ProductOptionDTO.class,
                                productOption.id.as("optionId"),
                                productOption.size,
                                productOption.color,
                                productOption.stock
                        )
                )
                .from(productOption)
                .where(productOption.product.id.eq(productId)
                        .and(productOption.isOpen.isTrue())
                )
                .orderBy(productOption.id.asc())
                .fetch();
    }

    @Override
    @Transactional
    public void patchOrderStock(List<PatchOrderStockDTO> patchDTO) {
        CaseBuilder caseBuilder = new CaseBuilder();
        CaseBuilder.Cases<Integer, NumberExpression<Integer>> caseExpression = null;

        for(PatchOrderStockDTO dto : patchDTO) {
            if(caseExpression == null)
                caseExpression = caseBuilder.when(productOption.id.eq(dto.getOptionId()))
                        .then(dto.getProductCount());
            else
                caseExpression = caseExpression.when(productOption.id.eq(dto.getOptionId()))
                        .then(dto.getProductCount());
        }

        NumberExpression<Integer> stockExpression = caseExpression.otherwise(0);
        List<Long> ids = patchDTO.stream().map(PatchOrderStockDTO::getOptionId).toList();

        jpaQueryFactory.update(productOption)
                .set(
                        productOption.stock,
                        new CaseBuilder()
                                .when(productOption.stock.lt(stockExpression))
                                .then(0)
                                .otherwise(productOption.stock.subtract(stockExpression))
                )
                .where(productOption.id.in(ids))
                .execute();
    }

    @Override
    public List<OrderProductInfoDTO> findOrderData(List<Long> optionIds) {
        return jpaQueryFactory.select(
                Projections.constructor(
                        OrderProductInfoDTO.class,
                        product.id.as("productId"),
                        productOption.id.as("optionId"),
                        product.productName,
                        productOption.size,
                        productOption.color,
                        product.productPrice.as("price"),
                        product.productDiscount.as("discount")
                )
                )
                .from(productOption)
                .innerJoin(product)
                .on(productOption.product.id.eq(product.id))
                .where(productOption.id.in(optionIds))
                .fetch();
    }

    @Override
    public List<AdminProductOptionDTO> findAllAdminOptionDTOByProductId(String productId) {
        return jpaQueryFactory.select(
                        Projections.constructor(
                                AdminProductOptionDTO.class
                                , productOption.id.as("optionId")
                                , productOption.size
                                , productOption.color
                                , productOption.stock.as("optionStock")
                                , productOption.isOpen.as("optionIsOpen")
                        )
                )
                .from(productOption)
                .where(productOption.product.id.eq(productId))
                .fetch();
    }

    @Override
    public List<AdminOptionStockDTO> findAllProductOptionStockByProductIds(List<String> productIds) {
        return jpaQueryFactory.select(
                        Projections.constructor(
                                AdminOptionStockDTO.class,
                                product.id.as("productId"),
                                productOption.size,
                                productOption.color,
                                productOption.stock.as("optionStock"),
                                productOption.isOpen.as("optionIsOpen")
                        )
                )
                .from(productOption)
                .innerJoin(product)
                .on(productOption.product.id.eq(product.id))
                .where(product.id.in(productIds))
                .fetch();
    }
}
