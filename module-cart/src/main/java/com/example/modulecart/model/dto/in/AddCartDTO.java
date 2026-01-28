package com.example.modulecart.model.dto.in;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(name = "장바구니 담기 요청 데이터", type = "array")
public record AddCartDTO(
        @Schema(name = "optionId", description = "상품 옵션 아이디")
        @NotNull
        @Min(value = 1, message = "Integrity optionId")
        Long optionId,
        @Schema(name = "count", description = "상품 개수")
        @Min(value = 1, message = "Integrity count")
        int count
) {
}
