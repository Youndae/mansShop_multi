package com.example.moduleproduct.usecase.productReview;

import com.example.modulecommon.customException.CustomAccessDeniedException;
import com.example.modulecommon.model.entity.*;
import com.example.modulecommon.model.enumuration.ErrorCode;
import com.example.modulecommon.model.enumuration.Result;
import com.example.moduleproduct.model.dto.productReview.in.MyPagePatchReviewDTO;
import com.example.moduleproduct.service.productReview.ProductReviewDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductReviewWriteUseCase {

    private final ProductReviewDataService productReviewDataService;


    public void patchReview(MyPagePatchReviewDTO reviewDTO, String userId) {
        ProductReview productReview = productReviewDataService.findProductReviewByIdOrElseIllegal(reviewDTO.reviewId());

        if(!productReview.getMember().getUserId().equals(userId)) {
            log.info("PatchProductReview writer not match. userId: {}, writer: {}", userId, productReview.getMember().getUserId());
            throw new CustomAccessDeniedException(ErrorCode.FORBIDDEN, ErrorCode.FORBIDDEN.getMessage());
        }

        productReview.setReviewContent(reviewDTO.content());
        productReviewDataService.saveProductReview(productReview);
    }

    public void deleteReview(long reviewId, String userId) {
        ProductReview productReview = productReviewDataService.findProductReviewByIdOrElseIllegal(reviewId);

        if(!productReview.getMember().getUserId().equals(userId)) {
            log.info("DeleteProductReview writer not match. userId: {}, writer: {}", userId, productReview.getMember().getUserId());
            throw new CustomAccessDeniedException(ErrorCode.FORBIDDEN, ErrorCode.FORBIDDEN.getMessage());
        }

        productReviewDataService.deleteProductReview(reviewId);
    }
}
