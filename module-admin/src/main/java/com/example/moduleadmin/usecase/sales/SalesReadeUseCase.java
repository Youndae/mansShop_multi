package com.example.moduleadmin.usecase.sales;

import com.example.moduleadmin.model.dto.page.AdminSalesPageDTO;
import com.example.moduleadmin.model.dto.sales.business.*;
import com.example.moduleadmin.model.dto.sales.out.*;
import com.example.moduleadmin.service.sales.period.PeriodSalesDataService;
import com.example.moduleadmin.service.sales.period.PeriodSalesDomainService;
import com.example.moduleadmin.service.sales.product.ProductSalesDataService;
import com.example.modulecommon.customException.CustomNotFoundException;
import com.example.modulecommon.model.enumuration.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SalesReadeUseCase {

    private final PeriodSalesDataService periodSalesDataService;

    private final PeriodSalesDomainService periodSalesDomainService;

    private final ProductSalesDataService productSalesDataService;


    public AdminPeriodSalesResponseDTO<AdminPeriodSalesListDTO> getPeriodSales(int term) {
        List<AdminPeriodSalesListDTO> selectList = periodSalesDataService.getPeriodListByYearTerm(term);

        return periodSalesDomainService.createAdminPeriodSalesResponseDTO(selectList);
    }

    public AdminPeriodMonthDetailResponseDTO getPeriodSalesDetailByYearMonth(String term) {
        LocalDate startDate = periodSalesDomainService.getStartDateByTermStr(term);
        LocalDate endDate = startDate.plusMonths(1);
        AdminPeriodSalesStatisticsDTO monthStatistics = periodSalesDataService.getPeriodStatistics(startDate, endDate);

        LocalDate lastYearStartDate = startDate.minusYears(1);
        LocalDate lastYearEndDate = endDate.minusYears(1);
        AdminPeriodSalesStatisticsDTO lastYearStatistics = periodSalesDataService.getPeriodStatistics(lastYearStartDate, lastYearEndDate);

        if(lastYearStatistics == null)
            lastYearStatistics = AdminPeriodSalesStatisticsDTO.emptyDTO();

        if(monthStatistics == null)
            return new AdminPeriodMonthDetailResponseDTO(
                    AdminPeriodSalesStatisticsDTO.emptyDTO(),
                    lastYearStatistics,
                    Collections.emptyList(),
                    Collections.emptyList(),
                    Collections.emptyList()
            );

        List<AdminBestSalesProductDTO> bestProductList = productSalesDataService.getPeriodBest5Product(startDate, endDate);
        List<AdminPeriodSalesListDTO> dailySalesResponseDTO = periodSalesDataService.getPeriodDailySalesList(startDate, endDate);
        dailySalesResponseDTO = periodSalesDomainService.completeDailySalesList(startDate, dailySalesResponseDTO);
        List<AdminPeriodClassificationDTO> classificationResponseDTO = productSalesDataService.getPeriodClassificationSalesList(startDate, endDate);

        return new AdminPeriodMonthDetailResponseDTO(
                monthStatistics,
                lastYearStatistics,
                bestProductList,
                classificationResponseDTO,
                dailySalesResponseDTO
        );
    }

    public AdminClassificationSalesResponseDTO getSalesByClassification(String term, String classificationId) {
        LocalDate startDate = periodSalesDomainService.getStartDateByTermStr(term);
        LocalDate endDate = startDate.plusMonths(1);

        AdminClassificationSalesDTO classificationSalesDTO = productSalesDataService.getPeriodClassificationSalesByClassificationId(startDate, endDate, classificationId);

        if(classificationSalesDTO == null)
            return new AdminClassificationSalesResponseDTO(
                    classificationId,
                    AdminClassificationSalesDTO.emptyDTO(),
                    Collections.emptyList()
            );

        List<AdminClassificationSalesProductListDTO> productList = productSalesDataService.getPeriodClassificationProductSalesByClassificationId(startDate, endDate, classificationId);

        return new AdminClassificationSalesResponseDTO(classificationId, classificationSalesDTO, productList);
    }

    public AdminPeriodSalesResponseDTO<AdminPeriodClassificationDTO> getSalesByDay(String term) {
        LocalDate startDate = periodSalesDomainService.getStartDateByTermStr(term);
        LocalDate endDate = startDate.plusDays(1);

        AdminClassificationSalesDTO salesDTO = periodSalesDataService.getDailySalesList(startDate);

        if(salesDTO == null)
            return new AdminPeriodSalesResponseDTO<>(
                    Collections.emptyList(),
                    0,
                    0,
                    0
            );

        List<AdminPeriodClassificationDTO> classificationList = productSalesDataService.getPeriodClassificationSalesList(startDate, endDate);

        return new AdminPeriodSalesResponseDTO<>(
                classificationList,
                salesDTO.sales(),
                salesDTO.salesQuantity(),
                salesDTO.orderQuantity()
        );
    }

    public LocalDate getTermDate(String term) {
        return periodSalesDomainService.getStartDateByTermStr(term);
    }

    public Page<AdminProductSalesListDTO> getProductSalesList(AdminSalesPageDTO pageDTO) {
        return productSalesDataService.getProductSalesList(pageDTO);
    }

    public AdminProductSalesDetailDTO getProductSalesDetail(String productId) {
        int year = LocalDate.now().getYear();

        AdminProductSalesDTO totalSalesDTO = productSalesDataService.getProductSales(productId);

        if(totalSalesDTO.productName() == null)
            throw new CustomNotFoundException(ErrorCode.NOT_FOUND, ErrorCode.NOT_FOUND.getMessage());

        AdminSalesDTO yearSalesDTO = productSalesDataService.getProductPeriodSales(year, productId);
        AdminSalesDTO lastYearSalesDTO = productSalesDataService.getProductPeriodSales(year - 1, productId);
        List<AdminPeriodSalesListDTO> monthSalesDTO = productSalesDataService.getProductMonthPeriodSales(year, productId);
        List<AdminProductSalesOptionDTO> optionTotalSalesList = productSalesDataService.getProductOptionSales(0, productId);
        List<AdminProductSalesOptionDTO> optionYearSalesList = productSalesDataService.getProductOptionSales(year, productId);
        List<AdminProductSalesOptionDTO> optionLastYearSalesList = productSalesDataService.getProductOptionSales(year - 1, productId);
        List<AdminPeriodSalesListDTO> monthSalesMappingDTO = periodSalesDomainService.createAdminPeriodSalesResponseDTO(monthSalesDTO).content();

        return new AdminProductSalesDetailDTO(
                totalSalesDTO,
                yearSalesDTO,
                lastYearSalesDTO,
                monthSalesMappingDTO,
                optionTotalSalesList,
                optionYearSalesList,
                optionLastYearSalesList
        );
    }
}
