package com.example.moduleproduct.model.dto.admin.product.in;

import com.example.modulecommon.model.entity.Classification;
import com.example.modulecommon.model.entity.Product;
import com.example.modulecommon.model.entity.ProductOption;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Random;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "관리자의 상품 추가 및 수정 요청시 상품 데이터")
public class AdminProductPatchDTO {

    @Schema(description = "상품명", example = "testProduct")
    private String productName;

    @Schema(description = "상품 분류명", example = "OUTER")
    private String classification;

    @Schema(description = "상품 가격", example = "10000")
    private int price;

    @Schema(description = "상품 공개 여부. true = 공개, false = 비공개", example = "true")
    private Boolean isOpen;

    @Schema(description = "상품 할인율", example = "10")
    private int discount;

    @Schema(description = "상품 옵션 리스트. 여러 옵션 추가 가능.")
    private List<PatchOptionDTO> optionList;

    public Product toPostEntity() {
        StringBuffer sb = new StringBuffer();
        return Product.builder()
                .id(
                        sb.append(classification)
                                .append(
                                        new SimpleDateFormat("yyyyMMddHHmmssSSS").format(System.currentTimeMillis())
                                )
                                .append(String.format("%06d", new Random().nextInt(100000)))
                                .toString()
                )
                .classification(Classification.builder().id(classification).build())
                .productName(productName)
                .productPrice(price)
                .isOpen(isOpen)
                .productSalesQuantity(0L)
                .productDiscount(discount)
                .build();
    }

    public List<ProductOption> getProductOptionList(Product product) {

        return this.getOptionList().stream().map(option -> option.toEntity(product)).toList();
    }
}
