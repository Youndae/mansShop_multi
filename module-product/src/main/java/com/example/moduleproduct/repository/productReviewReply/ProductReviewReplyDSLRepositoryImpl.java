package com.example.moduleproduct.repository.productReviewReply;

import com.example.modulecommon.model.entity.ProductReviewReply;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;


import static com.example.modulecommon.model.entity.QProductReviewReply.productReviewReply;

@Repository
@RequiredArgsConstructor
public class ProductReviewReplyDSLRepositoryImpl implements ProductReviewReplyDSLRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public ProductReviewReply findByReviewId(Long reviewId) {
        return jpaQueryFactory.selectFrom(productReviewReply)
                .where(productReviewReply.productReview.id.eq(reviewId))
                .fetchOne();
    }
}
