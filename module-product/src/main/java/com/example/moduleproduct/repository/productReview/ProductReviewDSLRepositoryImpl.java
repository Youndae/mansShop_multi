package com.example.moduleproduct.repository.productReview;

import com.example.modulecommon.model.enumuration.AdminListType;
import com.example.moduleproduct.model.dto.admin.review.out.AdminReviewDTO;
import com.example.moduleproduct.model.dto.admin.review.out.AdminReviewDetailDTO;
import com.example.moduleproduct.model.dto.page.AdminReviewPageDTO;
import com.example.moduleproduct.model.dto.product.out.ProductDetailReviewDTO;
import com.example.moduleproduct.model.dto.productReview.out.MyPageReviewDTO;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

import static com.example.modulecommon.model.entity.QProductReview.productReview;
import static com.example.modulecommon.model.entity.QProductReviewReply.productReviewReply;
import static com.example.modulecommon.model.entity.QMember.member;
import static com.example.modulecommon.model.entity.QProduct.product;

@Repository
@RequiredArgsConstructor
public class ProductReviewDSLRepositoryImpl implements ProductReviewDSLRepository{

    private final JPAQueryFactory jpaQueryFactory;

    @PersistenceContext
    private final EntityManager em;

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

    @Override
    public Page<MyPageReviewDTO> findAllByUserId(String userId, Pageable pageable) {
        List<MyPageReviewDTO> list = jpaQueryFactory.select(
                                            Projections.constructor(
                                                    MyPageReviewDTO.class,
                                                    productReview.id.as("reviewId"),
                                                    product.thumbnail,
                                                    product.productName,
                                                    productReview.reviewContent.as("content"),
                                                    productReview.createdAt,
                                                    productReview.updatedAt,
                                                    productReviewReply.replyContent,
                                                    productReviewReply.updatedAt.as("replyUpdatedAt")
                                            )
                                    )
                                    .from(productReview)
                                    .innerJoin(productReview.product, product)
                                    .leftJoin(productReviewReply)
                                    .on(productReviewReply.productReview.id.eq(productReview.id))
                                    .where(productReview.member.userId.eq(userId))
                                    .offset(pageable.getOffset())
                                    .limit(pageable.getPageSize())
                                    .orderBy(productReview.id.desc())
                                    .fetch();

        JPAQuery<Long> count = jpaQueryFactory.select(productReview.countDistinct())
                .from(productReview)
                .where(productReview.member.userId.eq(userId));

        return PageableExecutionUtils.getPage(list, pageable, count::fetchOne);
    }

    @Override
    public List<AdminReviewDTO> findAllByAdminReviewList(AdminReviewPageDTO pageDTO, String listType) {
        StringBuilder queryBuilder = new StringBuilder();

        queryBuilder.append("SELECT r.id, ")
                .append(reviewDynamicFieldQuery(pageDTO))
                .append("r.updatedAt, ")
                .append("r.status ")
                .append("FROM ")
                .append(reviewDynamicSubQuery(pageDTO, listType))
                .append(reviewDynamicJoinQuery(pageDTO));

        Query query = em.createNativeQuery(queryBuilder.toString());

        query.setParameter("offset", pageDTO.offset());
        query.setParameter("amount", pageDTO.amount());

        if(pageDTO.keyword() != null)
            query.setParameter("keyword", "%" + pageDTO.keyword() + "%");

        List<Object[]> resultList = query.getResultList();

        return resultList.stream()
                .map(val -> new AdminReviewDTO(
                        ((Number) val[0]).longValue(),
                        (String) val[1],
                        (String) val[2],
                        ((Timestamp) val[3]).toLocalDateTime(),
                        (Boolean) val[4]
                ))
                .toList();
    }

    private String reviewDynamicFieldQuery(AdminReviewPageDTO pageDTO) {
        StringBuilder queryBuilder = new StringBuilder();

        if(pageDTO.searchType() == null) {
            queryBuilder.append("p.productName, ")
                    .append(memberCaseWhenQuery());
        }else if(pageDTO.searchType().equals("product")){
            queryBuilder.append("r.productName, ")
                    .append(memberCaseWhenQuery());
        }else if(pageDTO.searchType().equals("user")) {
            queryBuilder.append("p.productName, ")
                    .append("r.userId, ");
        }

        return queryBuilder.toString();
    }

    private String memberCaseWhenQuery() {
        return "CASE WHEN (m.nickname is null) THEN m.userName ELSE m.nickname END AS userId, ";
    }

    private String reviewDynamicJoinQuery(AdminReviewPageDTO pageDTO) {
        StringBuilder queryBuilder = new StringBuilder();
        String productJoin = "INNER JOIN product p ON p.id = r.productId ";
        String memberJoin = "INNER JOIN member m ON m.userId = r.userId ";

        if(pageDTO.searchType() == null){
            queryBuilder.append(productJoin)
                    .append(memberJoin);
        }else if(pageDTO.searchType().equals("product")){
            queryBuilder.append(memberJoin);
        }else if(pageDTO.searchType().equals("user")){
            queryBuilder.append(productJoin);
        }

        return queryBuilder.toString();
    }

    private String reviewDynamicSubQuery(AdminReviewPageDTO pageDTO, String listType) {
        StringBuilder queryBuilder = new StringBuilder();
        String listTypeCondition = listType.equals(AdminListType.NEW.getType()) ? "AND pr.status = 0 " : "";

        queryBuilder.append("( SELECT ")
                .append("pr.id, ")
                .append("pr.updatedAt, ")
                .append("pr.status, ");

        if(pageDTO.searchType() == null) {
            queryBuilder.append("pr.productId, ")
                    .append("pr.userId ")
                    .append("FROM productReview pr ")
                    .append("WHERE 1=1 ");
        }else if(pageDTO.searchType().equals("product")) {
            queryBuilder.append("p.productName, ")
                    .append("pr.userId ")
                    .append("FROM productReview pr ")
                    .append("INNER JOIN product p ")
                    .append("ON p.id = pr.productId ")
                    .append("WHERE p.productName LIKE :keyword ");
        }else if(pageDTO.searchType().equals("user")) {
            queryBuilder.append(memberCaseWhenQuery())
                    .append("pr.productId ")
                    .append("FROM productReview pr ")
                    .append("INNER JOIN member m ")
                    .append("ON m.userId = pr.userId ")
                    .append("WHERE (m.userName LIKE :keyword OR m.nickname LIKE :keyword) ");
        }

        queryBuilder.append(listTypeCondition)
                .append("ORDER BY pr.updatedAt DESC ")
                .append("LIMIT :offset, :amount) AS r ");

        return queryBuilder.toString();
    }

    @Override
    public Long countByAdminReviewList(AdminReviewPageDTO pageDTO, String listType) {
        return jpaQueryFactory.select(productReview.updatedAt.count())
                .from(productReview)
                .where(adminReviewListSearch(pageDTO, listType))
                .fetchOne();
    }

    public BooleanExpression adminReviewListSearch(AdminReviewPageDTO pageDTO, String listType) {
        BooleanExpression response = Expressions.asBoolean(true).isTrue();
        if(pageDTO.searchType() != null) {
            String keyword = String.format("%%%s%%", pageDTO.keyword());
            if(pageDTO.searchType().equals("user"))
                response = response.and(
                        productReview.member.userName.like(keyword)
                                .or(productReview.member.nickname.like(keyword))
                );
            else if(pageDTO.searchType().equals("product"))
                response = response.and(productReview.product.productName.like(keyword));
        }

        if(listType.equals(AdminListType.NEW.getType())) {
            response = response.and(productReview.status.eq(false));
        }

        return response.equals(Expressions.asBoolean(true).isTrue()) ? null : response;
    }

    @Override
    public AdminReviewDetailDTO findAdminReviewDetailById(long reviewId) {
        return jpaQueryFactory.select(
                        Projections.constructor(
                                AdminReviewDetailDTO.class,
                                productReview.id.as("reviewId"),
                                productReview.product.productName,
                                productReview.productOption.size,
                                productReview.productOption.color,
                                new CaseBuilder()
                                        .when(productReview.member.nickname.isNull())
                                        .then(productReview.member.userName)
                                        .otherwise(productReview.member.nickname)
                                        .as("writer"),
                                productReview.createdAt,
                                productReview.updatedAt,
                                productReview.reviewContent.as("content"),
                                productReviewReply.updatedAt.as("replyUpdatedAt"),
                                productReviewReply.replyContent
                        )
                )
                .from(productReview)
                .leftJoin(productReviewReply)
                .on(productReviewReply.productReview.id.eq(productReview.id))
                .where(productReview.id.eq(reviewId))
                .fetchOne();
    }
}
