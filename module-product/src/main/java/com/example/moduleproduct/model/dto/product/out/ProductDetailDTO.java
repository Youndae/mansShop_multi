package com.example.moduleproduct.model.dto.product.out;

import com.example.modulecommon.model.entity.Product;
import com.example.modulecommon.utils.ProductDiscountUtils;
import com.example.moduleproduct.model.dto.product.business.ProductOptionDTO;
import com.example.moduleproduct.model.dto.product.business.ProductQnAResponseDTO;

import java.util.List;

public record ProductDetailDTO(
        String productId,
        String productName,
        int productPrice,
        String productImageName,
        boolean likeStat,
        int discount,
        int discountPrice,
        List<ProductOptionDTO> productOptionList,
        List<String> productThumbnailList,
        List<String> productInfoImageList,
        ProductPageableDTO<ProductDetailReviewDTO> productReviewList,
        ProductPageableDTO<ProductQnAResponseDTO> productQnAList
) {
    public ProductDetailDTO(Product product,
                            boolean likeStat,
                            List<ProductOptionDTO> productOptionList,
                            List<String> productThumbnailList,
                            List<String> productInfoImageList,
                            ProductPageableDTO<ProductDetailReviewDTO> productReviewList,
                            ProductPageableDTO<ProductQnAResponseDTO> productQnAList) {
        this(
                product.getId(),
                product.getProductName(),
                product.getProductPrice(),
                product.getThumbnail(),
                likeStat,
                product.getProductDiscount(),
                ProductDiscountUtils.calcDiscountPrice(product.getProductPrice(), product.getProductDiscount()),
                productOptionList,
                productThumbnailList,
                productInfoImageList,
                productReviewList,
                productQnAList
        );
    }
}
