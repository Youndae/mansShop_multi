package com.example.moduleadmin.repository;

import com.example.modulecommon.model.entity.PeriodSalesSummary;

import java.time.LocalDate;

public interface PeriodSalesSummaryDSLRepository {

    PeriodSalesSummary findByPeriod(LocalDate period);
}
