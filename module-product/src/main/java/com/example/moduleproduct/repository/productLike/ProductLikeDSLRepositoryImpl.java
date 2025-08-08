package com.example.moduleproduct.repository.productLike;

import com.example.modulecommon.model.entity.ProductLike;
import com.example.moduleproduct.model.dto.productLike.out.ProductLikeDTO;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.modulecommon.model.entity.QProductLike.productLike;
import static com.example.modulecommon.model.entity.QProduct.product;
import static com.example.modulecommon.model.entity.QProductOption.productOption;

@Repository
@RequiredArgsConstructor
public class ProductLikeDSLRepositoryImpl implements ProductLikeDSLRepository{

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public int countByUserIdAndProductId(String productId, String userId) {
        return jpaQueryFactory
                .select(productLike.count())
                .from(productLike)
                .where(productLike.member.userId.eq(userId)
                        .and(productLike.product.id.eq(productId))
                )
                .fetchOne()
                .intValue();
    }

    @Override
    @Transactional
    public void deleteByUserIdAndProductId(ProductLike entity) {
        jpaQueryFactory.delete(productLike)
                .where(productLike.member.eq(entity.getMember())
                        .and(productLike.product.eq(entity.getProduct()))
                )
                .execute();
    }

    @Override
    public Page<ProductLikeDTO> findListByUserId(String userId, Pageable pageable) {
        List<ProductLikeDTO> list = jpaQueryFactory.select(
                Projections.constructor(
                        ProductLikeDTO.class,
                        productLike.id.as("likeId"),
                        product.id.as("productId"),
                        product.productName,
                        product.productPrice,
                        product.thumbnail,
                        productOption.stock.sum().as("stock"),
                        productLike.createdAt
                )
        )
                .from(productLike)
                .innerJoin(productLike.product, product)
                .innerJoin(productOption)
                .on(productOption.product.id.eq(product.id))
                .groupBy(productLike.id)
                .where(productLike.member.userId.eq(userId))
                .orderBy(productLike.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> count = jpaQueryFactory.select(productLike.countDistinct())
                .from(productLike)
                .where(productLike.member.userId.eq(userId));

        return PageableExecutionUtils.getPage(list, pageable, count::fetchOne);
    }
}
