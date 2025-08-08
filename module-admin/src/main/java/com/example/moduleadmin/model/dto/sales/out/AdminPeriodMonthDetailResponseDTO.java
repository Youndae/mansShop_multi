package com.example.moduleadmin.model.dto.sales.out;

import com.example.moduleadmin.model.dto.sales.business.AdminBestSalesProductDTO;
import com.example.moduleadmin.model.dto.sales.business.AdminPeriodSalesStatisticsDTO;

import java.util.List;

public record AdminPeriodMonthDetailResponseDTO(
        long monthSales,
        long monthSalesQuantity,
        long monthOrderQuantity,
        long lastYearComparison,
        long lastYearSales,
        long lastYearSalesQuantity,
        long lastYearOrderQuantity,
        List<AdminBestSalesProductDTO> bestProduct,
        List<AdminPeriodClassificationDTO> classificationSales,
        List<AdminPeriodSalesListDTO> dailySales
) {

    public AdminPeriodMonthDetailResponseDTO(AdminPeriodSalesStatisticsDTO monthStatistics,
                                             AdminPeriodSalesStatisticsDTO lastYearStatistics,
                                             List<AdminBestSalesProductDTO> bestProduct,
                                             List<AdminPeriodClassificationDTO> classificationSales,
                                             List<AdminPeriodSalesListDTO> dailySales) {
        this(
                monthStatistics.monthSales(),
                monthStatistics.monthSalesQuantity(),
                monthStatistics.monthOrderQuantity(),
                monthStatistics.monthSales() - lastYearStatistics.monthSales(),
                lastYearStatistics.monthSales(),
                lastYearStatistics.monthSalesQuantity(),
                lastYearStatistics.monthOrderQuantity(),
                bestProduct,
                classificationSales,
                dailySales
        );
    }
}
