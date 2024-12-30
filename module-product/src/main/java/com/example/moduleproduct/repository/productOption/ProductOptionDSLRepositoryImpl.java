package com.example.moduleproduct.repository.productOption;

import com.example.moduleproduct.model.dto.product.business.ProductOptionDTO;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.example.modulecommon.model.entity.QProductOption.productOption;

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
}
