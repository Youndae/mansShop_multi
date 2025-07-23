package com.example.moduleproduct.repository.productReview;

import com.example.moduleproduct.model.dto.product.out.ProductDetailReviewDTO;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.JPAExpressions;
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
import static com.example.modulecommon.model.entity.QMember.member;

@Repository
@RequiredArgsConstructor
public class ProductReviewDSLRepositoryImpl implements ProductReviewDSLRepository{

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<ProductDetailReviewDTO> findByProductId(String productId, Pageable pageable) {
        List<ProductDetailReviewDTO> list = jpaQueryFactory
                .select(
                        Projections.constructor(
                                ProductDetailReviewDTO.class,
                                ExpressionUtils.as(
                                        JPAExpressions.select(new CaseBuilder()
                                                        .when(productReview.member.nickname.isNull())
                                                        .then(productReview.member.userName)
                                                        .otherwise(productReview.member.nickname))
                                                .from(member)
                                                .where(member.userId.eq(productReview.member.userId)), "reviewWriter"
                                ),
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

        JPAQuery<Long> count = jpaQueryFactory.select(productReview.countDistinct())
                .from(productReview)
                .where(productReview.product.id.eq(productId));

        return PageableExecutionUtils.getPage(list, pageable, count::fetchOne);
    }
}
