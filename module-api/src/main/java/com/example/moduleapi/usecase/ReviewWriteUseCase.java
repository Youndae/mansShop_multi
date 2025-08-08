package com.example.moduleapi.usecase;

import com.example.modulecommon.model.entity.*;
import com.example.modulecommon.model.enumuration.OrderStatus;
import com.example.modulecommon.model.enumuration.Result;
import com.example.moduleorder.service.OrderDataService;
import com.example.moduleproduct.model.dto.productReview.in.MyPagePostReviewDTO;
import com.example.moduleproduct.service.product.ProductDataService;
import com.example.moduleproduct.service.productReview.ProductReviewDataService;
import com.example.moduleproduct.service.productReview.ProductReviewDomainService;
import com.example.moduleuser.service.UserDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewWriteUseCase {

    private final UserDataService userDataService;

    private final ProductDataService productDataService;

    private final OrderDataService orderDataService;

    private final ProductReviewDomainService productReviewDomainService;

    private final ProductReviewDataService productReviewDataService;

    /**
     *
     * @param reviewDTO
     * @param userId
     * @return
     *
     * order -> product 모듈로 의존관계가 형성되어 있지만,
     * 이 처리를 같은 트랜잭션 내에서 처리해야 하며
     * product에서 orderRepository 접근을 할 수 없기 때문에
     * 불가피하게 api 모듈에서 UseCase를 작성
     */
    @Transactional(rollbackFor = Exception.class)
    public String postReview(MyPagePostReviewDTO reviewDTO, String userId) {
        Member member = userDataService.getMemberByUserIdOrElseIllegal(userId);
        Product product = productDataService.getProductByIdOrElseIllegal(reviewDTO.productId());
        ProductOption productOption = productDataService.getProductOptionByIdOrElseIllegal(reviewDTO.optionId());
        ProductOrderDetail productOrderDetail = orderDataService.getProductOrderDetailByIdOrElseIllegal(reviewDTO.detailId());

        if(!productOrderDetail.getProductOrder().getOrderStat().equals(OrderStatus.COMPLETE.getStatusStr())
                || productOrderDetail.isOrderReviewStatus())
            throw new IllegalArgumentException("OrderStatus Not complete or reviewStatus is true");

        ProductReview productReview = productReviewDomainService.buildProductReview(member, product, reviewDTO.content(), productOption);
        productReviewDataService.saveProductReview(productReview);
        productOrderDetail.setOrderReviewStatus(true);
        orderDataService.saveProductOrderDetail(productOrderDetail);

        return Result.OK.getResultKey();
    }
}
