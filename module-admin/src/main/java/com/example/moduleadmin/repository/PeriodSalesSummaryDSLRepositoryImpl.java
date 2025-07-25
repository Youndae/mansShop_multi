package com.example.moduleadmin.repository;

import com.example.modulecommon.model.entity.PeriodSalesSummary;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

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
}
