package com.example.moduleproduct.repository.productLike;

import com.example.modulecommon.model.entity.ProductLike;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import static com.example.modulecommon.model.entity.QProductLike.productLike;

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
                .fetch()
                .get(0)
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
    public Page<?> findByUserId() {
        return null;
    }
}
