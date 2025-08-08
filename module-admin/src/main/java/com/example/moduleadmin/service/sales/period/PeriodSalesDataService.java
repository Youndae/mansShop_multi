package com.example.moduleadmin.service.sales.period;

import com.example.moduleadmin.model.dto.sales.business.AdminClassificationSalesDTO;
import com.example.moduleadmin.model.dto.sales.business.AdminPeriodSalesStatisticsDTO;
import com.example.moduleadmin.model.dto.sales.out.AdminPeriodSalesListDTO;
import com.example.moduleadmin.repository.PeriodSalesSummaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PeriodSalesDataService {

    private final PeriodSalesSummaryRepository periodSalesSummaryRepository;

    public List<AdminPeriodSalesListDTO> getPeriodListByYearTerm(int term) {
        return periodSalesSummaryRepository.findPeriodListByYear(term);
    }

    public AdminPeriodSalesStatisticsDTO getPeriodStatistics(LocalDate startDate, LocalDate endDate) {
        return periodSalesSummaryRepository.findPeriodStatistics(startDate, endDate);
    }

    public List<AdminPeriodSalesListDTO> getPeriodDailySalesList(LocalDate startDate, LocalDate endDate) {
        return periodSalesSummaryRepository.findPeriodDailySalesList(startDate, endDate);
    }

    public AdminClassificationSalesDTO getDailySalesList(LocalDate startDate) {
        return periodSalesSummaryRepository.findDailySales(startDate);
    }
}
