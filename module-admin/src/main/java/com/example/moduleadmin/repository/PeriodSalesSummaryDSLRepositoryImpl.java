package com.example.moduleadmin.repository;

import com.example.moduleadmin.model.dto.sales.business.AdminClassificationSalesDTO;
import com.example.moduleadmin.model.dto.sales.business.AdminPeriodSalesStatisticsDTO;
import com.example.moduleadmin.model.dto.sales.out.AdminPeriodSalesListDTO;
import com.example.modulecommon.model.entity.PeriodSalesSummary;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

import static com.example.modulecommon.model.entity.QPeriodSalesSummary.periodSalesSummary;

@Repository
@RequiredArgsConstructor
public class PeriodSalesSummaryDSLRepositoryImpl implements PeriodSalesSummaryDSLRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public PeriodSalesSummary findByPeriod(LocalDate period) {
        return jpaQueryFactory.selectFrom(periodSalesSummary)
                .where(periodSalesSummary.period.eq(period))
                .fetchOne();
    }

    @Override
    public List<AdminPeriodSalesListDTO> findPeriodListByYear(int year) {
        return jpaQueryFactory.select(
                        Projections.constructor(
                                AdminPeriodSalesListDTO.class,
                                periodSalesSummary.period.month().as("date"),
                                periodSalesSummary.sales.longValue().sum().as("sales"),
                                periodSalesSummary.salesQuantity.longValue().sum().as("salesQuantity"),
                                periodSalesSummary.orderQuantity.longValue().sum().as("orderQuantity")
                        )
                )
                .from(periodSalesSummary)
                .where(periodSalesSummary.period.year().eq(year))
                .groupBy(periodSalesSummary.period.month())
                .fetch();
    }

    @Override
    public AdminPeriodSalesStatisticsDTO findPeriodStatistics(LocalDate startDate, LocalDate endDate) {
        return jpaQueryFactory.select(
                        Projections.constructor(
                                AdminPeriodSalesStatisticsDTO.class,
                                periodSalesSummary.sales.longValue().sum().as("monthSales"),
                                periodSalesSummary.salesQuantity.longValue().sum().as("monthSalesQuantity"),
                                periodSalesSummary.orderQuantity.longValue().sum().as("monthOrderQuantity"),
                                periodSalesSummary.totalDeliveryFee.longValue().sum().as("deliveryFee"),
                                periodSalesSummary.cashTotal.longValue().sum().as("cashTotalPrice"),
                                periodSalesSummary.cardTotal.longValue().sum().as("cardTotalPrice")
                        )
                )
                .from(periodSalesSummary)
                .where(periodSalesSummary.period.goe(startDate).and(periodSalesSummary.period.lt(endDate)))
                .fetchOne();
    }

    @Override
    public List<AdminPeriodSalesListDTO> findPeriodDailySalesList(LocalDate startDate, LocalDate endDate) {
        return jpaQueryFactory.select(
                        Projections.constructor(
                                AdminPeriodSalesListDTO.class,
                                periodSalesSummary.period.dayOfMonth().as("date"),
                                periodSalesSummary.sales.as("sales"),
                                periodSalesSummary.salesQuantity.as("salesQuantity"),
                                periodSalesSummary.orderQuantity.as("orderQuantity")
                        )
                )
                .from(periodSalesSummary)
                .where(periodSalesSummary.period.goe(startDate).and(periodSalesSummary.period.lt(endDate)))
                .orderBy(periodSalesSummary.period.asc())
                .fetch();
    }

    @Override
    public AdminClassificationSalesDTO findDailySales(LocalDate period) {
        return jpaQueryFactory.select(
                        Projections.constructor(
                                AdminClassificationSalesDTO.class,
                                periodSalesSummary.sales,
                                periodSalesSummary.salesQuantity,
                                periodSalesSummary.orderQuantity
                        )
                )
                .from(periodSalesSummary)
                .where(periodSalesSummary.period.eq(period))
                .fetchOne();
    }
}
