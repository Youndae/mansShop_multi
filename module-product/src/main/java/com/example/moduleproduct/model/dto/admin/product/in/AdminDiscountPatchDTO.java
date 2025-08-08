package com.example.moduleproduct.model.dto.admin.product.in;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "상품 할인 요청 데이터")
public record AdminDiscountPatchDTO(
        @Schema(name = "productIdList", description = "상품 아이디 리스트", type = "array")
        List<String> productIdList,
        @Schema(name = "discount", description = "할인율")
        int discount
) {
}
