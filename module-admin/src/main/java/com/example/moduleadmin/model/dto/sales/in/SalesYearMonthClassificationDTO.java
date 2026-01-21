package com.example.moduleadmin.model.dto.sales.in;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.YearMonth;

@Schema(name = "YYYY-MM 구조의 상품 분류별 매출 요청 데이터")
public record SalesYearMonthClassificationDTO(
        @Schema(name = "term", description = "매출 조회 날짜. YYYY-MM")
        @DateTimeFormat(pattern = "YYYY-MM")
        YearMonth term,

        @Schema(name = "classification", description = "상품 분류")
        @NotBlank()
        @Size(min = 2)
        String classification
) {
}
