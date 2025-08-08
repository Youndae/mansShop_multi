package com.example.moduleadmin.model.dto.sales.business;

public record AdminPeriodSalesStatisticsDTO(
        long monthSales,
        long monthSalesQuantity,
        long monthOrderQuantity,
        long monthDeliveryFee,
        long cashTotalPrice,
        long cardTotalPrice
) {
    public static AdminPeriodSalesStatisticsDTO emptyDTO() {
        return new AdminPeriodSalesStatisticsDTO(0, 0, 0, 0, 0, 0);
    }
}
