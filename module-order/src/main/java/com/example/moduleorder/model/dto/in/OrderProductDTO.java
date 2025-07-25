package com.example.moduleorder.model.dto.in;

import com.example.modulecommon.model.entity.Product;
import com.example.modulecommon.model.entity.ProductOption;
import com.example.modulecommon.model.entity.ProductOrderDetail;
import com.example.moduleproduct.model.dto.product.business.PatchOrderStockDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class OrderProductDTO{
    @Schema(name = "optionId", description = "상품 옵션 아이디")
    private long optionId;
    @Schema(name = "productName", description = "상품명")
    private String productName;
    @Schema(name = "productId", description = "상품 아이디")
    private String productId;
    @Schema(name = "detailCount", description = "상품 수량")
    private int detailCount;
    @Schema(name = "detailPrice", description = "상품 총 가격")
    private int detailPrice;

    public ProductOrderDetail toOrderDetailEntity() {
        return ProductOrderDetail.builder()
                .productOption(
                        ProductOption.builder()
                                .id(optionId)
                                .build()
                )
                .product(
                        Product.builder()
                                .id(productId)
                                .build()
                )
                .orderDetailCount(detailCount)
                .orderDetailPrice(detailPrice)
                .build();
    }

    public PatchOrderStockDTO toPatchOrderStockDTO() {
        return new PatchOrderStockDTO(optionId, detailCount);
    }
}
