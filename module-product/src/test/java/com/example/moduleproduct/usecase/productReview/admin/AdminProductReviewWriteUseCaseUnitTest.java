package com.example.moduleproduct.usecase.productReview.admin;

import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.entity.ProductReview;
import com.example.modulecommon.model.entity.ProductReviewReply;
import com.example.moduleproduct.model.dto.admin.review.in.AdminReviewReplyRequestDTO;
import com.example.moduleproduct.service.productReview.ProductReviewDataService;
import com.example.moduleproduct.service.productReview.ProductReviewDomainService;
import com.example.moduleproduct.service.productReview.ProductReviewExternalService;
import com.example.moduleproduct.usecase.admin.productReview.AdminProductReviewWriteUseCase;
import com.example.moduleuser.service.UserDataService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminProductReviewWriteUseCaseUnitTest {

    @InjectMocks
    private AdminProductReviewWriteUseCase adminProductReviewWriteUseCase;

    @Mock
    private UserDataService userDataService;

    @Mock
    private ProductReviewDataService productReviewDataService;

    @Mock
    private ProductReviewDomainService productReviewDomainService;

    @Mock
    private ProductReviewExternalService productReviewExternalService;

    private AdminReviewReplyRequestDTO getReviewPostDTO() {
        return new AdminReviewReplyRequestDTO(1L, "testContent");
    }

    @Test
    @DisplayName(value = "리뷰 답변 작성. 사용자 정보가 잘못된 경우")
    void postReviewReplyWrongUserId() {
        AdminReviewReplyRequestDTO postDTO = getReviewPostDTO();
        when(userDataService.getMemberByUserIdOrElseIllegal(any())).thenThrow(IllegalArgumentException.class);

        assertThrows(
                IllegalArgumentException.class,
                () -> adminProductReviewWriteUseCase.postReviewReply(postDTO, "tester")
        );

        verify(productReviewDataService, never()).findProductReviewByIdOrElseIllegal(anyLong());
        verify(productReviewDataService, never()).getProductReviewReplyByReviewId(anyLong());
        verify(productReviewDomainService, never())
                .buildProductReviewReplyEntity(any(Member.class), any(ProductReview.class), any());
        verify(productReviewDataService, never()).saveProductReview(any(ProductReview.class));
        verify(productReviewDataService, never()).saveProductReviewReply(any(ProductReviewReply.class));
        verify(productReviewExternalService, never()).sendProductReviewNotification(any());
    }

    @Test
    @DisplayName(value = "리뷰 답변 작성. 리뷰 아이디가 잘못된 경우")
    void postReviewReplyWrongReviewId() {
        AdminReviewReplyRequestDTO postDTO = getReviewPostDTO();
        when(userDataService.getMemberByUserIdOrElseIllegal(any()))
                .thenReturn(Member.builder().userId("tester").build());
        when(productReviewDataService.findProductReviewByIdOrElseIllegal(anyLong()))
                .thenThrow(IllegalArgumentException.class);

        assertThrows(
                IllegalArgumentException.class,
                () -> adminProductReviewWriteUseCase.postReviewReply(postDTO, "tester")
        );

        verify(productReviewDataService, never()).getProductReviewReplyByReviewId(anyLong());
        verify(productReviewDomainService, never())
                .buildProductReviewReplyEntity(any(Member.class), any(ProductReview.class), any());
        verify(productReviewDataService, never()).saveProductReview(any(ProductReview.class));
        verify(productReviewDataService, never()).saveProductReviewReply(any(ProductReviewReply.class));
        verify(productReviewExternalService, never()).sendProductReviewNotification(any());
    }

    @Test
    @DisplayName(value = "리뷰 답변 작성. 리뷰 답변이 이미 존재하는 경우")
    void postReviewReplyExistsReply() {
        AdminReviewReplyRequestDTO postDTO = getReviewPostDTO();
        when(userDataService.getMemberByUserIdOrElseIllegal(any()))
                .thenReturn(Member.builder().userId("tester").build());
        when(productReviewDataService.findProductReviewByIdOrElseIllegal(anyLong()))
                .thenReturn(ProductReview.builder().id(1L).build());
        when(productReviewDataService.getProductReviewReplyByReviewId(anyLong()))
                .thenReturn(ProductReviewReply.builder().id(1L).build());

        assertThrows(
                IllegalArgumentException.class,
                () -> adminProductReviewWriteUseCase.postReviewReply(postDTO, "tester")
        );

        verify(productReviewDomainService, never())
                .buildProductReviewReplyEntity(any(Member.class), any(ProductReview.class), any());
        verify(productReviewDataService, never()).saveProductReview(any(ProductReview.class));
        verify(productReviewDataService, never()).saveProductReviewReply(any(ProductReviewReply.class));
        verify(productReviewExternalService, never()).sendProductReviewNotification(any());
    }
}
