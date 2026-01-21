package com.example.moduleadmin.service.sales.period;

import com.example.moduleadmin.model.dto.sales.out.AdminPeriodSalesListDTO;
import com.example.moduleadmin.model.dto.sales.out.AdminPeriodSalesResponseDTO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class PeriodSalesDomainService {

    public AdminPeriodSalesResponseDTO<AdminPeriodSalesListDTO> createAdminPeriodSalesResponseDTO(List<AdminPeriodSalesListDTO> selectList) {
        Map<Integer, AdminPeriodSalesListDTO> monthlySalesMap = selectList.stream()
                .collect(
                        Collectors.toMap(
                                AdminPeriodSalesListDTO::date,
                                dto -> dto
                        )
                );

        List<AdminPeriodSalesListDTO> contentList = new ArrayList<>();
        long yearSales = 0;
        long yearSalesQuantity = 0;
        long yearOrderQuantity = 0;

        for(int i = 1; i <= 12; i++) {
            AdminPeriodSalesListDTO content = monthlySalesMap.getOrDefault(i, new AdminPeriodSalesListDTO(i));
            yearSales += content.sales();
            yearSalesQuantity += content.salesQuantity();
            yearOrderQuantity += content.orderQuantity();

            contentList.add(content);
        }

        return new AdminPeriodSalesResponseDTO<>(
                contentList,
                yearSales,
                yearSalesQuantity,
                yearOrderQuantity
        );
    }

    public List<AdminPeriodSalesListDTO> completeDailySalesList(LocalDate startDate, List<AdminPeriodSalesListDTO> dailySalesList) {
        int lastDay = YearMonth.from(startDate).lengthOfMonth();

        Map<Integer, AdminPeriodSalesListDTO> dailyMap = dailySalesList.stream()
                .collect(
                        Collectors.toMap(AdminPeriodSalesListDTO::date, dto -> dto)
                );

        return IntStream.rangeClosed(1, lastDay)
                .mapToObj(v ->
                        dailyMap.getOrDefault(v, new AdminPeriodSalesListDTO(v))
                ).toList();
    }
}
