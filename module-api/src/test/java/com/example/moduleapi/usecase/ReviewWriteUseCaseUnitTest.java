package com.example.moduleapi.usecase;

import com.example.modulecommon.model.entity.*;
import com.example.modulecommon.model.enumuration.OrderStatus;
import com.example.moduleorder.service.OrderDataService;
import com.example.moduleproduct.model.dto.productReview.in.MyPagePostReviewDTO;
import com.example.moduleproduct.service.product.ProductDataService;
import com.example.moduleproduct.service.productReview.ProductReviewDataService;
import com.example.moduleproduct.service.productReview.ProductReviewDomainService;
import com.example.moduleuser.service.UserDataService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
public class ReviewWriteUseCaseUnitTest {

    @InjectMocks
    private ReviewWriteUseCase reviewWriteUseCase;

    @Mock
    private ProductReviewDataService productReviewDataService;

    @Mock
    private ProductReviewDomainService productReviewDomainService;

    @Mock
    private UserDataService userDataService;

    @Mock
    private ProductDataService productDataService;

    @Mock
    private OrderDataService orderDataService;

    private static final String USER_ID = "tester";

    private MyPagePostReviewDTO getMyPagePostReviewDTO() {
        return new MyPagePostReviewDTO(
                "testProductId",
                "testContent",
                1L,
                1L
        );
    }

    @Test
    @DisplayName(value = "리뷰 작성. 사용자 아이디가 잘못된 경우")
    void postReviewWrongUserId() {
        MyPagePostReviewDTO myPagePostReviewDTO = getMyPagePostReviewDTO();
        when(userDataService.getMemberByUserIdOrElseIllegal(any()))
                .thenThrow(IllegalArgumentException.class);

        assertThrows(
                IllegalArgumentException.class,
                () -> reviewWriteUseCase.postReview(myPagePostReviewDTO, USER_ID)
        );

        verify(productDataService, never())
                .getProductByIdOrElseIllegal(any());
        verify(productDataService, never())
                .getProductOptionByIdOrElseIllegal(anyLong());
        verify(orderDataService, never())
                .getProductOrderDetailByIdOrElseIllegal(anyLong());
        verify(productReviewDomainService, never())
                .buildProductReview(any(Member.class), any(Product.class), any(), any(ProductOption.class));
        verify(productReviewDataService, never())
                .saveProductReview(any(ProductReview.class));
        verify(orderDataService, never())
                .saveProductOrderDetail(any(ProductOrderDetail.class));
    }

    @Test
    @DisplayName(value = "리뷰 작성. 상품 아이디가 잘못된 경우")
    void postReviewWrongProductId() {
        MyPagePostReviewDTO myPagePostReviewDTO = getMyPagePostReviewDTO();
        when(userDataService.getMemberByUserIdOrElseIllegal(any()))
                .thenReturn(Member.builder().userId(USER_ID).build());
        when(productDataService.getProductByIdOrElseIllegal(any()))
                .thenThrow(IllegalArgumentException.class);

        assertThrows(
                IllegalArgumentException.class,
                () -> reviewWriteUseCase.postReview(myPagePostReviewDTO, USER_ID)
        );

        verify(productDataService, never())
                .getProductOptionByIdOrElseIllegal(anyLong());
        verify(orderDataService, never())
                .getProductOrderDetailByIdOrElseIllegal(anyLong());
        verify(productReviewDomainService, never())
                .buildProductReview(any(Member.class), any(Product.class), any(), any(ProductOption.class));
        verify(productReviewDataService, never())
                .saveProductReview(any(ProductReview.class));
        verify(orderDataService, never())
                .saveProductOrderDetail(any(ProductOrderDetail.class));
    }

    @Test
    @DisplayName(value = "리뷰 작성. 상품 옵션 아이디가 잘못된 경우")
    void postReviewWrongProductOptionId() {
        MyPagePostReviewDTO myPagePostReviewDTO = getMyPagePostReviewDTO();
        when(userDataService.getMemberByUserIdOrElseIllegal(any()))
                .thenReturn(Member.builder().userId(USER_ID).build());
        when(productDataService.getProductByIdOrElseIllegal(any()))
                .thenReturn(Product.builder().id("testProductId").build());
        when(productDataService.getProductOptionByIdOrElseIllegal(anyLong()))
                .thenThrow(IllegalArgumentException.class);

        assertThrows(
                IllegalArgumentException.class,
                () -> reviewWriteUseCase.postReview(myPagePostReviewDTO, USER_ID)
        );

        verify(orderDataService, never())
                .getProductOrderDetailByIdOrElseIllegal(anyLong());
        verify(productReviewDomainService, never())
                .buildProductReview(any(Member.class), any(Product.class), any(), any(ProductOption.class));
        verify(productReviewDataService, never())
                .saveProductReview(any(ProductReview.class));
        verify(orderDataService, never())
                .saveProductOrderDetail(any(ProductOrderDetail.class));
    }

    @Test
    @DisplayName(value = "리뷰 작성. 주문 상세 아이디가 잘못된 경우")
    void postReviewWrongOrderDetailId() {
        MyPagePostReviewDTO myPagePostReviewDTO = getMyPagePostReviewDTO();
        when(userDataService.getMemberByUserIdOrElseIllegal(any()))
                .thenReturn(Member.builder().userId(USER_ID).build());
        when(productDataService.getProductByIdOrElseIllegal(any()))
                .thenReturn(Product.builder().id("testProductId").build());
        when(productDataService.getProductOptionByIdOrElseIllegal(anyLong()))
                .thenReturn(ProductOption.builder().id(1L).build());
        when(orderDataService.getProductOrderDetailByIdOrElseIllegal(anyLong()))
                .thenThrow(IllegalArgumentException.class);

        assertThrows(
                IllegalArgumentException.class,
                () -> reviewWriteUseCase.postReview(myPagePostReviewDTO, USER_ID)
        );


        verify(productReviewDomainService, never())
                .buildProductReview(any(Member.class), any(Product.class), any(), any(ProductOption.class));
        verify(productReviewDataService, never())
                .saveProductReview(any(ProductReview.class));
        verify(orderDataService, never())
                .saveProductOrderDetail(any(ProductOrderDetail.class));
    }

    @Test
    @DisplayName(value = "리뷰 작성. 주문이 아직 완료되지 않은 경우")
    void postReviewNotCompleteOrder() {
        MyPagePostReviewDTO myPagePostReviewDTO = getMyPagePostReviewDTO();
        ProductOrderDetail orderDetail = ProductOrderDetail.builder()
                .id(1L)
                .productOrder(ProductOrder.builder()
                        .id(1L)
                        .orderStat(OrderStatus.SHIPPING.getStatusStr())
                        .build()
                )
                .orderReviewStatus(false)
                .build();
        when(userDataService.getMemberByUserIdOrElseIllegal(any()))
                .thenReturn(Member.builder().userId(USER_ID).build());
        when(productDataService.getProductByIdOrElseIllegal(any()))
                .thenReturn(Product.builder().id("testProductId").build());
        when(productDataService.getProductOptionByIdOrElseIllegal(anyLong()))
                .thenReturn(ProductOption.builder().id(1L).build());
        when(orderDataService.getProductOrderDetailByIdOrElseIllegal(anyLong()))
                .thenReturn(orderDetail);

        assertThrows(
                IllegalArgumentException.class,
                () -> reviewWriteUseCase.postReview(myPagePostReviewDTO, USER_ID)
        );


        verify(productReviewDomainService, never())
                .buildProductReview(any(Member.class), any(Product.class), any(), any(ProductOption.class));
        verify(productReviewDataService, never())
                .saveProductReview(any(ProductReview.class));
        verify(orderDataService, never())
                .saveProductOrderDetail(any(ProductOrderDetail.class));
    }

    @Test
    @DisplayName(value = "리뷰 작성. 이미 리뷰를 작성한 주문인 경우")
    void postReviewAlreadyReviewWrite() {
        MyPagePostReviewDTO myPagePostReviewDTO = getMyPagePostReviewDTO();
        ProductOrderDetail orderDetail = ProductOrderDetail.builder()
                .id(1L)
                .productOrder(ProductOrder.builder()
                        .id(1L)
                        .orderStat(OrderStatus.COMPLETE.getStatusStr())
                        .build()
                )
                .orderReviewStatus(true)
                .build();
        when(userDataService.getMemberByUserIdOrElseIllegal(any()))
                .thenReturn(Member.builder().userId(USER_ID).build());
        when(productDataService.getProductByIdOrElseIllegal(any()))
                .thenReturn(Product.builder().id("testProductId").build());
        when(productDataService.getProductOptionByIdOrElseIllegal(anyLong()))
                .thenReturn(ProductOption.builder().id(1L).build());
        when(orderDataService.getProductOrderDetailByIdOrElseIllegal(anyLong()))
                .thenReturn(orderDetail);

        assertThrows(
                IllegalArgumentException.class,
                () -> reviewWriteUseCase.postReview(myPagePostReviewDTO, USER_ID)
        );


        verify(productReviewDomainService, never())
                .buildProductReview(any(Member.class), any(Product.class), any(), any(ProductOption.class));
        verify(productReviewDataService, never())
                .saveProductReview(any(ProductReview.class));
        verify(orderDataService, never())
                .saveProductOrderDetail(any(ProductOrderDetail.class));
    }
}
