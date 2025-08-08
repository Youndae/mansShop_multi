package com.example.moduleadmin.model.dto.sales.out;

import com.example.moduleadmin.model.dto.sales.business.AdminProductSalesDTO;
import com.example.moduleadmin.model.dto.sales.business.AdminProductSalesOptionDTO;
import com.example.moduleadmin.model.dto.sales.business.AdminSalesDTO;

import java.util.List;

public record AdminProductSalesDetailDTO(
        String productName,
        long totalSales,
        long totalSalesQuantity,
        long yearSales,
        long yearSalesQuantity,
        long lastYearComparison,
        long lastYearSales,
        long lastYearSalesQuantity,
        List<AdminPeriodSalesListDTO> monthSales,
        List<AdminProductSalesOptionDTO> optionTotalSales,
        List<AdminProductSalesOptionDTO> optionYearSales,
        List<AdminProductSalesOptionDTO> optionLastYearSales
) {

    public AdminProductSalesDetailDTO(AdminProductSalesDTO totalSalesDTO,
                                    AdminSalesDTO yearSalesDTO,
                                    AdminSalesDTO lastYearSalesDTO,
                                    List<AdminPeriodSalesListDTO> monthSales,
                                    List<AdminProductSalesOptionDTO> optionTotalSales,
                                    List<AdminProductSalesOptionDTO> optionYearSales,
                                    List<AdminProductSalesOptionDTO> optionLastYearSales){
        this(
                totalSalesDTO.productName(),
                totalSalesDTO.totalSales(),
                totalSalesDTO.totalSalesQuantity(),
                yearSalesDTO.sales(),
                yearSalesDTO.salesQuantity(),
                yearSalesDTO.sales() - lastYearSalesDTO.sales(),
                lastYearSalesDTO.sales(),
                lastYearSalesDTO.salesQuantity(),
                monthSales,
                optionTotalSales,
                optionYearSales,
                optionLastYearSales
        );
    }
}