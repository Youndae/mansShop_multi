package com.example.moduleproduct.model.dto.admin.product.out;

import java.util.List;

public record AdminProductPatchDataDTO(
        String productId,
        String productName,
        String classificationId,
        String firstThumbnail,
        int price,
        boolean isOpen,
        int discount,
        List<AdminProductOptionDTO> optionList,
        List<String> thumbnailList,
        List<String> infoImageList,
        List<String> classificationList
) {


    public AdminProductPatchDataDTO(AdminProductDetailDTO productDetailDTO
                                    , List<String> classificationList) {
        this(
                productDetailDTO.productId(),
                productDetailDTO.productName(),
                productDetailDTO.classification(),
                productDetailDTO.firstThumbnail(),
                productDetailDTO.price(),
                productDetailDTO.isOpen(),
                productDetailDTO.discount(),
                productDetailDTO.optionList(),
                productDetailDTO.thumbnailList(),
                productDetailDTO.infoImageList(),
                classificationList
        );
    }
}
