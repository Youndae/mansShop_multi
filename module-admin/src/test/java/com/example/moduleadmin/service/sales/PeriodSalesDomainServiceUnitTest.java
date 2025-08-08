package com.example.moduleadmin.service.sales;

import com.example.moduleadmin.model.dto.sales.out.AdminPeriodSalesListDTO;
import com.example.moduleadmin.model.dto.sales.out.AdminPeriodSalesResponseDTO;
import com.example.moduleadmin.service.sales.period.PeriodSalesDomainService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class PeriodSalesDomainServiceUnitTest {

    @InjectMocks
    private PeriodSalesDomainService periodSalesDomainService;

    @Test
    @DisplayName(value = "월별 매출 응답 DTO 데이터 매핑")
    void createAdminPeriodSalesResponseDTO() {
        List<AdminPeriodSalesListDTO> selectList = new ArrayList<>();
        long yearSalesFixture = 0L;
        long yearSalesQuantityFixture = 0L;
        long yearOrderQuantityFixture = 0L;
        for(int i = 1; i <= 12; i++) {
            long sales = 1000 * i;
            long salesQuantity = 100 * i;
            long orderQuantity = 10 * i;

            yearSalesFixture += sales;
            yearSalesQuantityFixture += salesQuantity;
            yearOrderQuantityFixture += orderQuantity;

            selectList.add(
                    new AdminPeriodSalesListDTO(
                            i,
                            sales,
                            salesQuantity,
                            orderQuantity
                    )
            );
        }

        AdminPeriodSalesResponseDTO<AdminPeriodSalesListDTO> result = assertDoesNotThrow(() ->
                periodSalesDomainService.createAdminPeriodSalesResponseDTO(selectList)
        );

        assertNotNull(result);
        assertEquals(yearSalesFixture, result.sales());
        assertEquals(yearSalesQuantityFixture, result.salesQuantity());
        assertEquals(yearOrderQuantityFixture, result.orderQuantity());
        assertEquals(selectList, result.content());
    }

    @Test
    @DisplayName(value = "월별 매출 응답 DTO 데이터 매핑. 데이터가 없는 월이 존재하는 경우")
    void createAdminPeriodSalesResponseDTOExistsEmptyData() {
        List<AdminPeriodSalesListDTO> selectList = new ArrayList<>();
        long yearSalesFixture = 0L;
        long yearSalesQuantityFixture = 0L;
        long yearOrderQuantityFixture = 0L;
        for(int i = 1; i <= 12; i++) {
            if(i % 2 == 0){
                long sales = 1000 * i;
                long salesQuantity = 100 * i;
                long orderQuantity = 10 * i;

                yearSalesFixture += sales;
                yearSalesQuantityFixture += salesQuantity;
                yearOrderQuantityFixture += orderQuantity;

                selectList.add(
                        new AdminPeriodSalesListDTO(
                                i,
                                sales,
                                salesQuantity,
                                orderQuantity
                        )
                );
            }
        }

        AdminPeriodSalesResponseDTO<AdminPeriodSalesListDTO> result = assertDoesNotThrow(() ->
                periodSalesDomainService.createAdminPeriodSalesResponseDTO(selectList)
        );

        assertNotNull(result);
        assertEquals(12, result.content().size());
        assertEquals(yearSalesFixture, result.sales());
        assertEquals(yearSalesQuantityFixture, result.salesQuantity());
        assertEquals(yearOrderQuantityFixture, result.orderQuantity());

        for(AdminPeriodSalesListDTO dto : result.content()){
            int date = dto.date();

            if(date % 2 == 0) {
                AdminPeriodSalesListDTO fixture = selectList.stream().filter(v -> v.date() == date).findFirst().get();
                assertEquals(fixture, dto);
            }else {
                assertEquals(0, dto.sales());
                assertEquals(0, dto.salesQuantity());
                assertEquals(0, dto.orderQuantity());
            }
        }
    }

    @Test
    @DisplayName(value = "일별 매출 응답 DTO 데이터 매핑")
    void completeDailySalesList() {
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        int lastDay = YearMonth.from(startDate).lengthOfMonth();
        List<AdminPeriodSalesListDTO> dailySalesList = new ArrayList<>();
        for (int i = 1; i <= lastDay; i++) {
            dailySalesList.add(
                    new AdminPeriodSalesListDTO(
                            i,
                            (long) 1000 * i,
                            (long) 100 * i,
                            (long) 10 * i
                    )
            );
        }

        List<AdminPeriodSalesListDTO> result = assertDoesNotThrow(() ->
                periodSalesDomainService.completeDailySalesList(startDate, dailySalesList)
        );

        assertNotNull(result);
        assertEquals(dailySalesList.size(), result.size());
        assertEquals(dailySalesList, result);
    }

    @Test
    @DisplayName(value = "일별 매출 응답 DTO 데이터 매핑. 데이터가 없는 일이 존재하는 경우")
    void completeDailySalesListExistsEmptyData() {
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        int lastDay = YearMonth.from(startDate).lengthOfMonth();
        List<AdminPeriodSalesListDTO> dailySalesList = new ArrayList<>();
        for (int i = 1; i <= lastDay; i++) {
            if(i % 2 == 0){
                dailySalesList.add(
                        new AdminPeriodSalesListDTO(
                                i,
                                (long) 1000 * i,
                                (long) 100 * i,
                                (long) 10 * i
                        )
                );
            }
        }

        List<AdminPeriodSalesListDTO> result = assertDoesNotThrow(() ->
                periodSalesDomainService.completeDailySalesList(startDate, dailySalesList)
        );

        assertNotNull(result);
        assertEquals(lastDay, result.size());
        for(AdminPeriodSalesListDTO dto : result){
            int date = dto.date();

            if(date % 2 == 0) {
                AdminPeriodSalesListDTO fixture = dailySalesList.stream().filter(v -> v.date() == date).findFirst().get();
                assertEquals(fixture, dto);
            }else {
                assertEquals(0, dto.sales());
                assertEquals(0, dto.salesQuantity());
                assertEquals(0, dto.orderQuantity());
            }
        }
    }
}
