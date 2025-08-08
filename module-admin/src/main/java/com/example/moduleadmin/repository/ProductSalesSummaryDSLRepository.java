package com.example.moduleadmin.repository;

import com.example.moduleadmin.model.dto.page.AdminSalesPageDTO;
import com.example.moduleadmin.model.dto.sales.business.*;
import com.example.moduleadmin.model.dto.sales.out.AdminPeriodClassificationDTO;
import com.example.moduleadmin.model.dto.sales.out.AdminPeriodSalesListDTO;
import com.example.moduleadmin.model.dto.sales.out.AdminProductSalesListDTO;
import com.example.modulecommon.model.entity.ProductSalesSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface ProductSalesSummaryDSLRepository {

    List<ProductSalesSummary> findAllByProductOptionIds(LocalDate periodMonth, List<Long> productOptionIds);

    List<AdminBestSalesProductDTO> findPeriodBest5Product(LocalDate startDate, LocalDate endDate);

    List<AdminPeriodClassificationDTO> findPeriodClassificationSalesList(LocalDate startDate, LocalDate endDate);

    AdminClassificationSalesDTO findPeriodClassificationSalesByClassificationId(LocalDate startDate, LocalDate endDate, String classificationId);

    List<AdminClassificationSalesProductListDTO> findPeriodClassificationProductSalesByClassificationId(LocalDate startDate, LocalDate endDate, String classificationId);

    Page<AdminProductSalesListDTO> findProductSalesList(AdminSalesPageDTO pageDTO, Pageable pageable);

    AdminProductSalesDTO getProductSales(String productId);

    AdminSalesDTO getProductPeriodSales(int year, String productId);

    List<AdminPeriodSalesListDTO> getProductMonthPeriodSales(int year, String productId);

    List<AdminProductSalesOptionDTO> getProductOptionSales(int year, String productId);
}
