package com.example.moduleproduct.model.dto.admin.product.in;

import com.example.modulecommon.model.entity.Classification;
import com.example.modulecommon.model.entity.Product;
import com.example.modulecommon.model.entity.ProductOption;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Random;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "관리자의 상품 추가 요청시 상품 데이터")
public class AdminProductPostDTO {

    @Schema(description = "상품명", example = "testProduct")
    @NotBlank(message = "상품명은 공백일 수 없습니다.")
    @Length(min = 2, message = "상품명은 최소 2글자 이상입니다.")
    private String productName;

    @Schema(description = "상품 분류명", example = "OUTER")
    @NotBlank(message = "상품 분류는 공백일 수 없습니다.")
    @Length(min = 2, message = "상품 분류는 최소 2글자 이상입니다.")
    private String classification;

    @Schema(description = "상품 가격", example = "10000")
    @Min(value = 100, message = "가격은 최소 100원 이상입니다.")
    private int price;

    @Schema(description = "상품 공개 여부. true = 공개, false = 비공개", example = "true")
    @NotNull(message = "상품 공개 여부는 필수 사항입니다.")
    private Boolean isOpen;

    @Schema(description = "상품 할인율", example = "10")
    @Min(value = 0, message = "할인 최소값은 0 입니다.")
    private int discount;

    // optionList 필드 유효성 검증은 비즈니스 로직에서 수행
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
