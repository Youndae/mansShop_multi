package com.example.moduleproduct.model.dto.admin.product.out;

import com.example.modulecommon.model.entity.Product;

import java.util.List;

public record AdminProductDetailDTO(
        String productId,
        String classification,
        String productName,
        String firstThumbnail,
        List<String> thumbnailList,
        List<String> infoImageList,
        List<AdminProductOptionDTO> optionList,
        int price,
        boolean isOpen,
        long sales,
        int discount
) {

    public AdminProductDetailDTO (String productId,
                                Product product,
                                List<String> thumbnailList,
                                List<String> infoImageList,
                                List<AdminProductOptionDTO> productOptionList) {
        this(
                productId,
                product.getClassification().getId(),
                product.getProductName(),
                product.getThumbnail(),
                thumbnailList,
                infoImageList,
                productOptionList,
                product.getProductPrice(),
                product.isOpen(),
                product.getProductSalesQuantity(),
                product.getProductDiscount()
        );
    }
}
