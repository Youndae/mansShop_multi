package com.example.moduleproduct.usecase.admin.productReview;

import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.entity.ProductReview;
import com.example.modulecommon.model.entity.ProductReviewReply;
import com.example.modulecommon.model.enumuration.Result;
import com.example.moduleproduct.model.dto.admin.review.in.AdminReviewReplyRequestDTO;
import com.example.moduleproduct.service.productReview.ProductReviewDataService;
import com.example.moduleproduct.service.productReview.ProductReviewDomainService;
import com.example.moduleproduct.service.productReview.ProductReviewExternalService;
import com.example.moduleuser.service.UserDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminProductReviewWriteUseCase {

    private final UserDataService userDataService;

    private final ProductReviewDataService productReviewDataService;

    private final ProductReviewDomainService productReviewDomainService;

    private final ProductReviewExternalService productReviewExternalService;

    @Transactional(rollbackFor = Exception.class)
    public String postReviewReply(AdminReviewReplyRequestDTO postDTO, String userId) {
        Member member = userDataService.getMemberByUserIdOrElseIllegal(userId);
        ProductReview productReview = productReviewDataService.findProductReviewByIdOrElseIllegal(postDTO.reviewId());
        ProductReviewReply productReviewReply = productReviewDataService.getProductReviewReplyByReviewId(postDTO.reviewId());

        if(productReviewReply != null)
            throw new IllegalArgumentException("review Reply already exist");

        ProductReviewReply entity = productReviewDomainService.buildProductReviewReplyEntity(member, productReview, postDTO.content());

        productReview.setStatus(true);

        productReviewDataService.saveProductReview(productReview);
        productReviewDataService.saveProductReviewReply(entity);

        productReviewExternalService.sendProductReviewNotification(productReview.getMember().getUserId());

        return Result.OK.getResultKey();
    }
}
