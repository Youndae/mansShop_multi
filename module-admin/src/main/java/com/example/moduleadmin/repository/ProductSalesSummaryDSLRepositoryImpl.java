package com.example.moduleadmin.repository;

import com.example.moduleadmin.model.dto.page.AdminSalesPageDTO;
import com.example.moduleadmin.model.dto.sales.business.*;
import com.example.moduleadmin.model.dto.sales.out.AdminPeriodClassificationDTO;
import com.example.moduleadmin.model.dto.sales.out.AdminPeriodSalesListDTO;
import com.example.moduleadmin.model.dto.sales.out.AdminProductSalesListDTO;
import com.example.modulecommon.model.entity.ProductSalesSummary;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
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

import java.time.LocalDate;
import java.util.List;

import static com.example.modulecommon.model.entity.QProductSalesSummary.productSalesSummary;
import static com.example.modulecommon.model.entity.QProductOption.productOption;
import static com.example.modulecommon.model.entity.QClassification.classification;
import static com.example.modulecommon.model.entity.QProduct.product;

@Repository
@RequiredArgsConstructor
public class ProductSalesSummaryDSLRepositoryImpl implements ProductSalesSummaryDSLRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<ProductSalesSummary> findAllByProductOptionIds(LocalDate periodMonth, List<Long> productOptionIds) {
        return jpaQueryFactory.select(productSalesSummary)
                .from(productSalesSummary)
                .innerJoin(productSalesSummary.productOption, productOption).fetchJoin()
                .innerJoin(productSalesSummary.classification, classification).fetchJoin()
                .innerJoin(productSalesSummary.product, product).fetchJoin()
                .where(
                        productSalesSummary.periodMonth.eq(periodMonth)
                                .and(productSalesSummary.productOption.id.in(productOptionIds))
                )
                .fetch();
    }

    @Override
    public List<AdminBestSalesProductDTO> findPeriodBest5Product(LocalDate startDate, LocalDate endDate) {
        StringBuilder queryBuilder = new StringBuilder();

        queryBuilder.append("SELECT p.productName, ")
                .append("s.salesQuantity, ")
                .append("s.sales ")
                .append("FROM (")
                    .append("SELECT productId, ")
                    .append("sum(sales) as sales, ")
                    .append("sum(salesQuantity) as salesQuantity ")
                    .append("FROM productSalesSummary ")
                    .append("WHERE periodMonth >= :startDate ")
                    .append("AND periodMonth < :endDate ")
                    .append("GROUP BY productId ")
                    .append("ORDER BY salesQuantity DESC ")
                    .append("LIMIT 5")
                .append(") as s ")
                .append("INNER JOIN product p ")
                .append("ON s.productId = p.id");

        Query query = em.createNativeQuery(queryBuilder.toString());

        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);

        List<Object[]> resultList = query.getResultList();

        return resultList.stream()
                .map(val -> new AdminBestSalesProductDTO(
                        (String) val[0],
                        ((Number) val[1]).longValue(),
                        ((Number) val[2]).longValue()
                ))
                .toList();
    }

    @Override
    public List<AdminPeriodClassificationDTO> findPeriodClassificationSalesList(LocalDate startDate, LocalDate endDate) {
        return jpaQueryFactory.select(
                        Projections.constructor(
                                AdminPeriodClassificationDTO.class,
                                classification.id.as("classification"),
                                productSalesSummary.sales.longValue().sum().coalesce(0L).as("classificationSales"),
                                productSalesSummary.salesQuantity.longValue().sum().coalesce(0L).as("classificationSalesQuantity")
                        )
                )
                .from(productSalesSummary)
                .rightJoin(classification)
                .on(
                        classification.id.eq(productSalesSummary.classification.id)
                                .and(productSalesSummary.periodMonth.goe(startDate))
                                .and(productSalesSummary.periodMonth.lt(endDate))
                )
                .groupBy(classification.id)
                .orderBy(classification.classificationStep.asc())
                .fetch();
    }

    @Override
    public AdminClassificationSalesDTO findPeriodClassificationSalesByClassificationId(LocalDate startDate, LocalDate endDate, String classificationId) {
        return jpaQueryFactory.select(
                        Projections.constructor(
                                AdminClassificationSalesDTO.class,
                                productSalesSummary.sales.longValue().sum().as("sales"),
                                productSalesSummary.salesQuantity.longValue().sum().as("salesQuantity"),
                                productSalesSummary.orderQuantity.longValue().sum().as("orderQuantity")
                        )
                )
                .from(productSalesSummary)
                .where(productSalesSummary.classification.id.eq(classificationId)
                        .and(productSalesSummary.periodMonth.goe(startDate))
                        .and(productSalesSummary.periodMonth.lt(endDate))
                )
                .fetchOne();
    }

    @Override
    public List<AdminClassificationSalesProductListDTO> findPeriodClassificationProductSalesByClassificationId(LocalDate startDate, LocalDate endDate, String classificationId) {
        return jpaQueryFactory.select(
                        Projections.constructor(
                                AdminClassificationSalesProductListDTO.class,
                                product.productName.as("productName"),
                                productOption.size.as("size"),
                                productOption.color.as("color"),
                                productSalesSummary.sales.coalesce(0L).as("productSales"),
                                productSalesSummary.salesQuantity.coalesce(0L).as("productSalesQuantity")
                        )
                )
                .from(productOption)
                .innerJoin(productOption.product, product)
                .leftJoin(productSalesSummary)
                .on(productSalesSummary.productOption.id.eq(productOption.id)
                        .and(productSalesSummary.periodMonth.goe(startDate))
                        .and(productSalesSummary.periodMonth.lt(endDate))
                )
                .where(product.classification.id.eq(classificationId))
                .orderBy(product.createdAt.desc())
                .fetch();
    }

    @Override
    public Page<AdminProductSalesListDTO> findProductSalesList(AdminSalesPageDTO pageDTO, Pageable pageable) {
        StringBuilder queryBuilder = new StringBuilder();
        String searchCondition = pageDTO.keyword() != null ? "WHERE pr.productName LIKE :keyword " : "";

        queryBuilder.append("SELECT p.classificationId, ")
                .append("p.id, ")
                .append("p.productName, ")
                .append("coalesce(sum(s.sales), 0), ")
                .append("p.productSalesQuantity ")
                .append("FROM productSalesSummary s ")
                .append("RIGHT JOIN")
                .append("(")
                    .append("SELECT pr.id, ")
                    .append("pr.classificationId, ")
                    .append("pr.productName, ")
                    .append("pr.productSalesQuantity ")
                    .append("FROM product pr ")
                    .append("INNER JOIN classification c ")
                    .append("ON pr.classificationId = c.id ")
                    .append(searchCondition)
                    .append("ORDER BY c.classificationStep ASC, ")
                    .append("pr.createdAt DESC ")
                    .append("LIMIT :offset, :amount")
                .append(") as p ")
                .append("ON p.id = s.productId ")
                .append("GROUP BY p.classificationId, p.id, p.productName");

        Query query = em.createNativeQuery(queryBuilder.toString());

        query.setParameter("offset", pageDTO.offset());
        query.setParameter("amount", pageDTO.amount());

        if(pageDTO.keyword() != null)
            query.setParameter("keyword", "%" + pageDTO.keyword() + "%");

        List<Object[]> resultList = query.getResultList();

        List<AdminProductSalesListDTO> list = resultList.stream()
                .map(val -> new AdminProductSalesListDTO(
                        (String) val[0],
                        (String) val[1],
                        (String) val[2],
                        ((Number) val[3]).longValue(),
                        ((Number) val[4]).longValue()
                ))
                .toList();

        JPAQuery<Long> count = jpaQueryFactory.select(product.createdAt.count())
                .from(product)
                .where(productSalesDynamicSearch(pageDTO));


        return PageableExecutionUtils.getPage(list, pageable, count::fetchOne);
    }

    private BooleanExpression productSalesDynamicSearch(AdminSalesPageDTO pageDTO) {
        if(pageDTO.keyword() != null)
            return product.productName.like(pageDTO.keyword());
        else
            return null;
    }

    @Override
    public AdminProductSalesDTO getProductSales(String productId) {
        return jpaQueryFactory.select(
                        Projections.constructor(
                                AdminProductSalesDTO.class,
                                product.productName.as("productName"),
                                productSalesSummary.sales.longValue().sum().as("totalSales"),
                                product.productSalesQuantity.as("totalSalesQuantity")
                        )
                )
                .from(productSalesSummary)
                .innerJoin(productSalesSummary.product, product)
                .where(product.id.eq(productId))
                .fetchOne();
    }

    @Override
    public AdminSalesDTO getProductPeriodSales(int year, String productId) {
        return jpaQueryFactory.select(
                        Projections.constructor(
                                AdminSalesDTO.class,
                                productSalesSummary.sales.longValue().sum().as("sales"),
                                productSalesSummary.salesQuantity.longValue().sum().as("salesQuantity")
                        )
                )
                .from(productSalesSummary)
                .innerJoin(productSalesSummary.product, product)
                .where(
                        productSalesSummary.product.id.eq(productId)
                                .and(productSalesSummary.periodMonth.year().eq(year))
                )
                .fetchOne();
    }

    @Override
    public List<AdminPeriodSalesListDTO> getProductMonthPeriodSales(int year, String productId) {
        return jpaQueryFactory.select(
                        Projections.constructor(
                                AdminPeriodSalesListDTO.class,
                                productSalesSummary.periodMonth.month().as("date"),
                                productSalesSummary.sales.longValue().sum().as("sales"),
                                productSalesSummary.salesQuantity.longValue().sum().as("salesQuantity"),
                                productSalesSummary.orderQuantity.longValue().sum().as("orderQuantity")
                        )
                )
                .from(productSalesSummary)
                .innerJoin(productSalesSummary.product, product)
                .where(productSalesSummary.product.id.eq(productId).and(productSalesSummary.periodMonth.year().eq(year)))
                .groupBy(productSalesSummary.periodMonth.month())
                .orderBy(productSalesSummary.periodMonth.month().asc())
                .fetch();
    }

    @Override
    public List<AdminProductSalesOptionDTO> getProductOptionSales(int year, String productId) {
        return jpaQueryFactory.select(
                        Projections.constructor(
                                AdminProductSalesOptionDTO.class,
                                productOption.id.as("optionId"),
                                productOption.size.as("size"),
                                productOption.color.as("color"),
                                productSalesSummary.sales.longValue().sum().coalesce(0L).as("optionSales"),
                                productSalesSummary.salesQuantity.longValue().sum().coalesce(0L).as("optionSalesQuantity")
                        )
                )
                .from(productOption)
                .leftJoin(productSalesSummary)
                .on(
                        productOption.product.id.eq(productSalesSummary.product.id)
                                .and(productOption.id.eq(productSalesSummary.productOption.id))
                                .and(productOptionSalesDynamicSearch(year))
                )
                .where(productOption.product.id.eq(productId))
                .groupBy(productOption.id, productOption.size, productOption.color)
                .orderBy(productOption.id.asc())
                .fetch();
    }

    private BooleanExpression productOptionSalesDynamicSearch(int year) {
        return year == 0 ? null : productSalesSummary.periodMonth.year().eq(year);
    }
}
