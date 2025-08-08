package com.example.moduleadmin.repository;

import com.example.moduleadmin.model.dto.sales.business.AdminClassificationSalesDTO;
import com.example.moduleadmin.model.dto.sales.business.AdminPeriodSalesStatisticsDTO;
import com.example.moduleadmin.model.dto.sales.out.AdminPeriodSalesListDTO;
import com.example.modulecommon.model.entity.PeriodSalesSummary;

import java.time.LocalDate;
import java.util.List;

public interface PeriodSalesSummaryDSLRepository {

    PeriodSalesSummary findByPeriod(LocalDate period);

    List<AdminPeriodSalesListDTO> findPeriodListByYear(int year);

    AdminPeriodSalesStatisticsDTO findPeriodStatistics(LocalDate startDate, LocalDate endDate);

    List<AdminPeriodSalesListDTO> findPeriodDailySalesList(LocalDate startDate, LocalDate endDate);

    AdminClassificationSalesDTO findDailySales(LocalDate period);
}
