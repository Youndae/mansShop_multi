package com.example.modulecart.model.dto.in;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "장바구니 담기 요청 데이터", type = "array")
public record AddCartDTO(
        @Schema(name = "optionId", description = "상품 옵션 아이디")
        Long optionId,
        @Schema(name = "count", description = "상품 개수")
        int count
) {
}
