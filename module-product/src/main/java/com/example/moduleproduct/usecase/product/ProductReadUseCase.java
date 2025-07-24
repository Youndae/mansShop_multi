package com.example.moduleproduct.usecase.product;

import com.example.modulecommon.model.entity.Product;
import com.example.moduleproduct.model.dto.page.ProductDetailPageDTO;
import com.example.moduleproduct.model.dto.product.business.ProductOptionDTO;
import com.example.moduleproduct.model.dto.product.business.ProductQnADTO;
import com.example.moduleproduct.model.dto.product.business.ProductQnAResponseDTO;
import com.example.moduleproduct.model.dto.product.out.ProductDetailDTO;
import com.example.moduleproduct.model.dto.product.out.ProductDetailQnAReplyListDTO;
import com.example.moduleproduct.model.dto.product.out.ProductDetailReviewDTO;
import com.example.moduleproduct.model.dto.product.out.ProductPageableDTO;
import com.example.moduleproduct.service.product.ProductDataService;
import com.example.moduleproduct.service.product.ProductDomainService;
import com.example.moduleproduct.service.productLike.ProductLikeDataService;
import com.example.moduleproduct.service.productQnA.ProductQnADataService;
import com.example.moduleproduct.service.review.ProductReviewDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductReadUseCase {

    private final ProductDataService productDataService;

    private final ProductDomainService productDomainService;

    private final ProductQnADataService productQnADataService;

    private final ProductReviewDataService productReviewDataService;

    private final ProductLikeDataService productLikeDataService;

    public ProductDetailDTO getProductDetail(String productId, String userId) {
        Product product = productDataService.getProductById(productId);

        boolean likeStatus = false;
        if(userId != null)
            likeStatus = productLikeDataService.getProductLikeStatusByUser(userId, productId);

        List<ProductOptionDTO> optionDTOList = productDataService.getProductOptionDTOListByProductId(productId);
        List<String> thumbnailNameList = productDataService.getProductThumbnailImageNameList(productId);
        List<String> infoImageNameList = productDataService.getProductInfoImageNameList(productId);

        ProductDetailPageDTO pageDTO = new ProductDetailPageDTO();
        Page<ProductDetailReviewDTO> reviewDTOData = getProductDetailReview(pageDTO, productId);
        Page<ProductQnAResponseDTO> productQnAMappingData = getProductDetailQnA(pageDTO, productId);
        ProductPageableDTO<ProductDetailReviewDTO> productDetailReview = new ProductPageableDTO<>(reviewDTOData);
        ProductPageableDTO<ProductQnAResponseDTO> productDetailQnA = new ProductPageableDTO<>(productQnAMappingData);

        return new ProductDetailDTO(
                product,
                likeStatus,
                optionDTOList,
                thumbnailNameList,
                infoImageNameList,
                productDetailReview,
                productDetailQnA
        );
    }

    public Page<ProductDetailReviewDTO> getProductDetailReview(ProductDetailPageDTO pageDTO, String productId) {
        return productReviewDataService.getProductDetailReview(pageDTO, productId);
    }

    public Page<ProductQnAResponseDTO> getProductDetailQnA(ProductDetailPageDTO pageDTO, String productId) {
        Pageable qnaPageable = PageRequest.of(pageDTO.pageNum() - 1,
                                                    pageDTO.qnaAmount(),
                                                    Sort.by("createdAt").descending()
                                            );
        Page<ProductQnADTO> productQnADTOData = productQnADataService.getProductDetailQnA(qnaPageable, productId);
        List<ProductQnAResponseDTO> qnaContent = Collections.emptyList();

        if(!productQnADTOData.getContent().isEmpty()) {
            List<Long> qnaIds = productQnADTOData.getContent().stream().map(ProductQnADTO::qnaId).toList();
            List<ProductDetailQnAReplyListDTO> productQnAReplyList = productQnADataService.getProductDetailQnAReplyList(qnaIds);
            qnaContent = productDomainService.mapToProductQnAResponseDTO(productQnADTOData, productQnAReplyList);
        }
        return new PageImpl<>(qnaContent, qnaPageable, productQnADTOData.getTotalElements());
    }
}
