package com.example.moduleorder.model.dto.in;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;

public record OrderProductRequestDTO(
        @Schema(name = "optionId", description = "상품 옵션 아이디")
        @Min(value = 1, message = "Integrity option id")
        long optionId,
        @Schema(name = "count", description = "상품 수량")
        @Min(value = 1, message = "Integrity count")
        int count
) {
}
