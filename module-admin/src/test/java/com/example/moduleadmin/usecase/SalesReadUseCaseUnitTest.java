package com.example.moduleadmin.usecase;

import com.example.moduleadmin.model.dto.sales.business.AdminProductSalesDTO;
import com.example.moduleadmin.model.dto.sales.in.SalesYearMonthClassificationDTO;
import com.example.moduleadmin.model.dto.sales.out.AdminClassificationSalesResponseDTO;
import com.example.moduleadmin.model.dto.sales.out.AdminPeriodClassificationDTO;
import com.example.moduleadmin.model.dto.sales.out.AdminPeriodMonthDetailResponseDTO;
import com.example.moduleadmin.model.dto.sales.out.AdminPeriodSalesResponseDTO;
import com.example.moduleadmin.service.sales.period.PeriodSalesDataService;
import com.example.moduleadmin.service.sales.period.PeriodSalesDomainService;
import com.example.moduleadmin.service.sales.product.ProductSalesDataService;
import com.example.moduleadmin.usecase.sales.SalesReadeUseCase;
import com.example.modulecommon.customException.CustomNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SalesReadUseCaseUnitTest {

    @InjectMocks
    private SalesReadeUseCase salesReadUseCase;

    @Mock
    private PeriodSalesDataService periodSalesDataService;

    @Mock
    private PeriodSalesDomainService periodSalesDomainService;

    @Mock
    private ProductSalesDataService productSalesDataService;

    @Test
    @DisplayName(value = "월 매출 상세 조회. 월 매출이 없는 경우")
    void getPeriodSalesDetailByYearMonth() {
        YearMonth term = YearMonth.of(2025, 1);
        when(periodSalesDataService.getPeriodStatistics(any(LocalDate.class), any(LocalDate.class))).thenReturn(null);

        AdminPeriodMonthDetailResponseDTO result = assertDoesNotThrow(
                () -> salesReadUseCase.getPeriodSalesDetailByYearMonth(term)
        );

        verify(productSalesDataService, never()).getPeriodBest5Product(any(LocalDate.class), any(LocalDate.class));
        verify(periodSalesDataService, never()).getPeriodDailySalesList(any(LocalDate.class), any(LocalDate.class));
        verify(periodSalesDomainService, never()).completeDailySalesList(any(LocalDate.class), anyList());
        verify(productSalesDataService, never()).getPeriodClassificationSalesList(any(LocalDate.class), any(LocalDate.class));

        assertNotNull(result);
        assertEquals(0, result.monthSales());
        assertEquals(0, result.monthSalesQuantity());
        assertEquals(0, result.monthOrderQuantity());
        assertEquals(0, result.lastYearComparison());
        assertEquals(0, result.lastYearSales());
        assertEquals(0, result.lastYearSalesQuantity());
        assertEquals(0, result.lastYearOrderQuantity());
        assertTrue(result.bestProduct().isEmpty());
        assertTrue(result.classificationSales().isEmpty());
        assertTrue(result.dailySales().isEmpty());
    }

    @Test
    @DisplayName(value = "선택한 상품 분류의 월 매출 조회. 월 매출이 없는 경우")
    void getSalesByClassification() {
        YearMonth term = YearMonth.of(2025, 1);
        String classificationId = "OUTER";
        SalesYearMonthClassificationDTO fixture = new SalesYearMonthClassificationDTO(term, classificationId);
        when(productSalesDataService.getPeriodClassificationSalesByClassificationId(any(LocalDate.class), any(LocalDate.class), any()))
                .thenReturn(null);

        AdminClassificationSalesResponseDTO result = assertDoesNotThrow(
                () -> salesReadUseCase.getSalesByClassification(fixture)
        );

        verify(productSalesDataService, never())
                .getPeriodClassificationProductSalesByClassificationId(any(LocalDate.class), any(LocalDate.class), any());

        assertNotNull(result);
        assertEquals("OUTER", result.classification());
        assertEquals(0, result.totalSales());
        assertEquals(0, result.totalSalesQuantity());
        assertTrue(result.productList().isEmpty());
    }

    @Test
    @DisplayName(value = "선택 일자의 매출 조회. 매출이 없는 경우")
    void getSalesByDay() {
        LocalDate term = LocalDate.of(2025, 1, 1);
        when(periodSalesDataService.getDailySalesList(any(LocalDate.class))).thenReturn(null);

        AdminPeriodSalesResponseDTO<AdminPeriodClassificationDTO> result = assertDoesNotThrow(
                () -> salesReadUseCase.getSalesByDay(term)
        );

        verify(productSalesDataService, never())
                .getPeriodClassificationSalesList(any(LocalDate.class), any(LocalDate.class));

        assertNotNull(result);
        assertTrue(result.content().isEmpty());
        assertEquals(0, result.sales());
        assertEquals(0, result.salesQuantity());
        assertEquals(0, result.orderQuantity());
    }

    @Test
    @DisplayName(value = "선택 상품 매출 상세 조회. 상품 아이디가 잘못된 경우")
    void getProductSalesDetail() {
        AdminProductSalesDTO fixture = new AdminProductSalesDTO(
                null,
                0L,
                0L
        );
        when(productSalesDataService.getProductSales(any())).thenReturn(fixture);

        assertThrows(
                CustomNotFoundException.class,
                () -> salesReadUseCase.getProductSalesDetail("WrongProductId")
        );

        verify(productSalesDataService, never())
                .getProductPeriodSales(anyInt(), any());
        verify(productSalesDataService, never())
                .getProductMonthPeriodSales(anyInt(), any());
        verify(productSalesDataService, never())
                .getProductOptionSales(anyInt(), any());
        verify(periodSalesDomainService, never())
                .createAdminPeriodSalesResponseDTO(anyList());
    }
}
