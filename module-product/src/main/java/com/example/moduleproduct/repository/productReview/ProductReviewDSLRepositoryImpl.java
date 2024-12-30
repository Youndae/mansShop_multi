package com.example.moduleproduct.repository.productReview;

import com.example.moduleproduct.model.dto.product.business.ProductReviewResponseDTO;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.example.modulecommon.model.entity.QProductReview.productReview;
import static com.example.modulecommon.model.entity.QProductReviewReply.productReviewReply;

@Repository
@RequiredArgsConstructor
public class ProductReviewDSLRepositoryImpl implements ProductReviewDSLRepository{

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<ProductReviewResponseDTO> findByProductId(String productId, Pageable pageable) {
        List<ProductReviewResponseDTO> list = jpaQueryFactory
                .select(
                        Projections.constructor(
                                ProductReviewResponseDTO.class,
                                new CaseBuilder()
                                        .when(productReview.member.nickname.isNull())
                                        .then(productReview.member.userName)
                                        .otherwise(productReview.member.nickname)
                                        .as("reviewWriter"),
                                productReview.reviewContent,
                                productReview.createdAt.as("reviewCreatedAt"),
                                productReviewReply.replyContent.as("answerContent"),
                                productReviewReply.createdAt.as("answerCreatedAt")
                        )
                )
                .from(productReview)
                .leftJoin(productReviewReply)
                .on(productReview.id.eq(productReviewReply.productReview.id))
                .where(productReview.product.id.eq(productId))
                .orderBy(productReview.createdAt.desc())
                .orderBy(productReview.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> count = jpaQueryFactory.select(productReview.count())
                .from(productReview)
                .where(productReview.product.id.eq(productId));

        return PageableExecutionUtils.getPage(list, pageable, count::fetchOne);
    }
}
