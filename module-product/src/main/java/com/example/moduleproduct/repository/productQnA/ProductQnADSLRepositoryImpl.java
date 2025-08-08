package com.example.moduleproduct.repository.productQnA;

import com.example.modulecommon.model.dto.admin.qna.out.AdminQnAListResponseDTO;
import com.example.modulecommon.model.dto.page.AdminQnAPageDTO;
import com.example.modulecommon.model.enumuration.AdminListType;
import com.example.moduleproduct.model.dto.product.business.ProductQnADTO;
import com.example.moduleproduct.model.dto.productQnA.business.ProductQnADetailDTO;
import com.example.moduleproduct.model.dto.productQnA.out.ProductQnAListDTO;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
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

import static com.example.modulecommon.model.entity.QProductQnA.productQnA;
import static com.example.modulecommon.model.entity.QProduct.product;

@Repository
@RequiredArgsConstructor
public class ProductQnADSLRepositoryImpl implements ProductQnADSLRepository{

    private final JPAQueryFactory jpaQueryFactory;

    @PersistenceContext
    private EntityManager em;

    @Override
    public Page<ProductQnADTO> findByProductId(String productId, Pageable pageable) {
        List<ProductQnADTO> list = jpaQueryFactory
                .select(
                        Projections.constructor(
                                ProductQnADTO.class,
                                productQnA.id.as("qnaId"),
                                new CaseBuilder()
                                        .when(productQnA.member.nickname.isNull())
                                        .then(productQnA.member.userName)
                                        .otherwise(productQnA.member.nickname)
                                        .as("writer"),
                                productQnA.qnaContent,
                                productQnA.createdAt,
                                productQnA.productQnAStat
                        )
                )
                .from(productQnA)
                .where(productQnA.product.id.eq(productId))
                .orderBy(productQnA.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> count = jpaQueryFactory.select(productQnA.count())
                .from(productQnA)
                .where(productQnA.product.id.eq(productId));

        return PageableExecutionUtils.getPage(list, pageable, count::fetchOne);
    }

    @Override
    public Page<ProductQnAListDTO> findListByUserId(String userId, Pageable pageable) {
        List<ProductQnAListDTO> list = jpaQueryFactory.select(
                        Projections.constructor(
                                ProductQnAListDTO.class
                                , productQnA.id.as("productQnAId")
                                , product.productName
                                , productQnA.productQnAStat
                                , productQnA.createdAt
                        )
                )
                .from(productQnA)
                .innerJoin(productQnA.product, product)
                .where(productQnA.member.userId.eq(userId))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(productQnA.id.desc())
                .fetch();

        JPAQuery<Long> count = jpaQueryFactory.select(productQnA.countDistinct())
                .from(productQnA)
                .where(productQnA.member.userId.eq(userId));

        return PageableExecutionUtils.getPage(list, pageable, count::fetchOne);
    }

    @Override
    public ProductQnADetailDTO findByQnAId(long qnaId) {
        return jpaQueryFactory.select(
                Projections.constructor(
                        ProductQnADetailDTO.class,
                        productQnA.id.as("productQnAId"),
                        product.productName,
                        new CaseBuilder()
                                .when(productQnA.member.nickname.isNull())
                                .then(productQnA.member.userName)
                                .otherwise(productQnA.member.nickname)
                                .as("writer"),
                        productQnA.qnaContent,
                        productQnA.createdAt,
                        productQnA.productQnAStat
                )
        )
                .from(productQnA)
                .where(productQnA.id.eq(qnaId))
                .fetchOne();
    }

    @Override
    public List<AdminQnAListResponseDTO> findAllByAdminProductQnA(AdminQnAPageDTO pageDTO) {
        StringBuilder queryBuilder = new StringBuilder();

        queryBuilder.append("SELECT q.id, ")
                .append("p.classificationId, ")
                .append("p.productName, ");

        if(pageDTO.keyword() == null)
            queryBuilder.append("CASE WHEN (m.nickname IS NULL) THEN m.userName ELSE m.nickname END, ");
        else
            queryBuilder.append("q.userId, ");

        queryBuilder.append("q.createdAt, ")
                .append("q.productQnAStat ")
                .append("FROM (")
                .append(adminProductQnADynamicSubQuery(pageDTO))
                .append(") as q ")
                .append("INNER JOIN product p ")
                .append("ON p.id = q.productId ");

        if(pageDTO.keyword() == null)
            queryBuilder.append("LEFT JOIN member m ")
                    .append("ON m.userId = q.userId");

        Query query = em.createNativeQuery(queryBuilder.toString());

        query.setParameter("offset", pageDTO.offset());
        query.setParameter("amount", pageDTO.amount());

        if(pageDTO.keyword() != null)
            query.setParameter("keyword", pageDTO.keyword());

        List<Object[]> resultList = query.getResultList();

        return resultList.stream()
                .map(val -> new AdminQnAListResponseDTO(
                        ((Number) val[0]).longValue(),
                        (String) val[1],
                        (String) val[2],
                        (String) val[3],
                        ((Timestamp) val[4]).toLocalDateTime(),
                        (Boolean) val[5]
                ))
                .toList();
    }

    private String adminProductQnADynamicSubQuery(AdminQnAPageDTO pageDTO) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT pq.id, ")
                .append("pq.productId, ")
                .append("pq.createdAt, ")
                .append("pq.productQnAStat, ");

        if(pageDTO.keyword() == null)
            queryBuilder.append("pq.userId ")
                    .append("FROM productQnA pq ")
                    .append("WHERE 1=1 ");
        else
            queryBuilder.append("CASE WHEN (m.nickname is null) THEN m.userName ELSE m.nickname END as userId ")
                    .append("FROM productQnA pq ")
                    .append("INNER JOIN member m ")
                    .append("ON pq.userId = m.userId ")
                    .append("WHERE (m.nickname = :keyword OR m.userId = :keyword) ");

        queryBuilder.append(adminProductQnASubQuerySearch(pageDTO))
                .append("ORDER BY pq.createdAt DESC LIMIT :offset, :amount");

        return queryBuilder.toString();
    }

    private String adminProductQnASubQuerySearch(AdminQnAPageDTO pageDTO) {
        String query = "";

        if(pageDTO.listType().equals(AdminListType.NEW.getType()))
            query = "AND pq.productQnAStat = 0 ";

        return query;
    }

    @Override
    public Long countAdminProductQnA(AdminQnAPageDTO pageDTO) {
        return jpaQueryFactory.select(productQnA.createdAt.count())
                .from(productQnA)
                .where(adminProductQnASearch(pageDTO))
                .fetchOne();
    }

    public BooleanExpression adminProductQnASearch(AdminQnAPageDTO pageDTO) {

        if(pageDTO.listType().equals(AdminListType.NEW.getType())){
            if(pageDTO.keyword() != null)
                return (productQnA.member.nickname.eq(pageDTO.keyword()).or(productQnA.member.userId.eq(pageDTO.keyword())))
                        .and(productQnA.productQnAStat.isFalse());
            else
                return productQnA.productQnAStat.isFalse();
        }else if(pageDTO.listType().equals(AdminListType.ALL.getType())){
            if(pageDTO.keyword() != null)
                return productQnA.member.nickname.eq(pageDTO.keyword()).or(productQnA.member.userId.eq(pageDTO.keyword()));
        }

        return null;
    }
}
