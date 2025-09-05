package com.example.moduleproduct.usecase.productReview;

import com.example.modulecommon.customException.CustomAccessDeniedException;
import com.example.modulecommon.model.dto.page.MyPagePageDTO;
import com.example.modulecommon.model.entity.ProductReview;
import com.example.modulecommon.model.enumuration.ErrorCode;
import com.example.moduleproduct.model.dto.productReview.out.MyPagePatchReviewDataDTO;
import com.example.moduleproduct.model.dto.productReview.out.MyPageReviewDTO;
import com.example.moduleproduct.service.productReview.ProductReviewDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductReviewReadUseCase {

    private final ProductReviewDataService productReviewDataService;

    public Page<MyPageReviewDTO> getMyPageReviewList(MyPagePageDTO pageDTO, String userId) {
        return productReviewDataService.findAllReviewPaginationByUserId(pageDTO, userId);
    }

    public MyPagePatchReviewDataDTO getPatchReview(long reviewId, String userId) {

        ProductReview productReview = productReviewDataService.findProductReviewByIdOrElseIllegal(reviewId);

        if(!productReview.getMember().getUserId().equals(userId)) {
            log.info("patchReview writer not match. userId: {}, writer: {}", userId, productReview.getMember().getUserId());
            throw new CustomAccessDeniedException(ErrorCode.FORBIDDEN, ErrorCode.FORBIDDEN.getMessage());
        }

        return new MyPagePatchReviewDataDTO(reviewId, productReview.getReviewContent(), productReview.getProduct().getProductName());
    }
}
