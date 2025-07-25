package com.example.moduleorder.model.dto.in;

import io.swagger.v3.oas.annotations.media.Schema;

public record OrderProductRequestDTO(
        @Schema(name = "optionId", description = "상품 옵션 아이디")
        long optionId,
        @Schema(name = "count", description = "상품 수량")
        int count
) {
}
