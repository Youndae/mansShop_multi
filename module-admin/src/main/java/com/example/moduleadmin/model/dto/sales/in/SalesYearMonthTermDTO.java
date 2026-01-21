package com.example.moduleadmin.model.dto.sales.in;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.YearMonth;

@Schema(name = "YYYY-MM 구조의 매출 날짜 요청 데이터")
public record SalesYearMonthTermDTO(
        @Schema(name = "term", description = "매출 조회 날짜. YYYY-MM")
        @DateTimeFormat(pattern = "YYYY-MM")
        YearMonth term
) {
}
