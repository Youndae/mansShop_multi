package com.example.moduleadmin.service.sales.product;

import com.example.moduleadmin.model.dto.page.AdminSalesPageDTO;
import com.example.moduleadmin.model.dto.sales.business.*;
import com.example.moduleadmin.model.dto.sales.out.AdminPeriodClassificationDTO;
import com.example.moduleadmin.model.dto.sales.out.AdminPeriodSalesListDTO;
import com.example.moduleadmin.model.dto.sales.out.AdminProductSalesListDTO;
import com.example.moduleadmin.repository.ProductSalesSummaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductSalesDataService {

    private final ProductSalesSummaryRepository productSalesSummaryRepository;


    public List<AdminBestSalesProductDTO> getPeriodBest5Product(LocalDate startDate, LocalDate endDate) {
        return productSalesSummaryRepository.findPeriodBest5Product(startDate, endDate);
    }

    public List<AdminPeriodClassificationDTO> getPeriodClassificationSalesList(LocalDate startDate, LocalDate endDate) {
        return productSalesSummaryRepository.findPeriodClassificationSalesList(startDate, endDate);
    }

    public AdminClassificationSalesDTO getPeriodClassificationSalesByClassificationId(LocalDate startDate, LocalDate endDate, String classificationId) {
        return productSalesSummaryRepository.findPeriodClassificationSalesByClassificationId(startDate, endDate, classificationId);
    }

    public List<AdminClassificationSalesProductListDTO> getPeriodClassificationProductSalesByClassificationId(LocalDate startDate, LocalDate endDate, String classificationId) {
        return productSalesSummaryRepository.findPeriodClassificationProductSalesByClassificationId(startDate, endDate, classificationId);
    }

    public Page<AdminProductSalesListDTO> getProductSalesList(AdminSalesPageDTO pageDTO) {
        Pageable pageable = PageRequest.of(pageDTO.page() - 1,
                pageDTO.amount(),
                Sort.by("classificationStep").ascending());

        return productSalesSummaryRepository.findProductSalesList(pageDTO, pageable);
    }

    public AdminProductSalesDTO getProductSales(String productId) {
        return productSalesSummaryRepository.getProductSales(productId);
    }

    public AdminSalesDTO getProductPeriodSales(int year, String productId) {
        return productSalesSummaryRepository.getProductPeriodSales(year, productId);
    }

    public List<AdminPeriodSalesListDTO> getProductMonthPeriodSales(int year, String productId) {
        return productSalesSummaryRepository.getProductMonthPeriodSales(year, productId);
    }

    public List<AdminProductSalesOptionDTO> getProductOptionSales(int year, String productId) {
        return productSalesSummaryRepository.getProductOptionSales(year, productId);
    }
}
