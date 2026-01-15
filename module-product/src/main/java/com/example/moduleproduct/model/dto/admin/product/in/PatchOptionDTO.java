package com.example.moduleproduct.model.dto.admin.product.in;

import com.example.modulecommon.model.entity.Product;
import com.example.modulecommon.model.entity.ProductOption;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "상품 옵션 리스트 요청 데이터")
public class PatchOptionDTO {

    @Schema(description = "상품 옵션 아이디. 상품 추가시에는 전부 0으로 처리. 수정시에는 해당 optionId 필요.", example = "0")
    private long optionId;

    @Schema(description = "상품 사이즈 옵션. 따로 틀이 존재하지 않음.", example = "L")
    private String size;

    @Schema(description = "상품 색상 옵션. 따로 틀이 존재하지 않음.", example = "Black")
    private String color;

    @Schema(description = "상품 재고. 최소값 0", example = "0")
    private int optionStock;

    @Schema(description = "옵션 공개 여부. true = 공개, false = 비공개", example = "true")
    private boolean optionIsOpen;

    public ProductOption toEntity(Product product) {
        return ProductOption.builder()
                .id(optionId == 0 ? null : optionId)
                .product(product)
                .size(size)
                .color(color)
                .stock(optionStock)
                .isOpen(optionIsOpen)
                .build();
    }

    public ProductOption toEntity() {
        return ProductOption.builder()
                .id(optionId == 0 ? null : optionId)
                .size(size)
                .color(color)
                .stock(optionStock)
                .isOpen(optionIsOpen)
                .build();
    }
}
