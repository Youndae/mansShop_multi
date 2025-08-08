package com.example.moduleproduct.usecase.productReview;

import com.example.modulecommon.customException.CustomAccessDeniedException;
import com.example.modulecommon.model.entity.*;
import com.example.moduleproduct.model.dto.productReview.in.MyPagePatchReviewDTO;
import com.example.moduleproduct.service.productReview.ProductReviewDataService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductReviewWriteUseCaseUnitTest {

    @InjectMocks
    private ProductReviewWriteUseCase productReviewWriteUseCase;

    @Mock
    private ProductReviewDataService productReviewDataService;

    private static final String USER_ID = "tester";

    private MyPagePatchReviewDTO getMyPagePatchReviewDTO() {
        return new MyPagePatchReviewDTO(
                1L, "testContent"
        );
    }

    @Test
    @DisplayName(value = "리뷰 수정. 리뷰 아이디가 잘못된 경우")
    void patchReviewWrongReviewId() {
        MyPagePatchReviewDTO reviewDTO = getMyPagePatchReviewDTO();
        when(productReviewDataService.findProductReviewByIdOrElseIllegal(anyLong()))
                .thenThrow(IllegalArgumentException.class);

        assertThrows(
                IllegalArgumentException.class,
                () -> productReviewWriteUseCase.patchReview(reviewDTO, USER_ID)
        );

        verify(productReviewDataService, never())
                .saveProductReview(any(ProductReview.class));
    }

    @Test
    @DisplayName(value = "리뷰 수정. 작성자가 일치하지 않는 경우")
    void patchReviewWriterNotEquals() {
        MyPagePatchReviewDTO reviewDTO = getMyPagePatchReviewDTO();
        ProductReview reviewFixture = ProductReview.builder()
                                .id(1L)
                                .member(Member.builder()
                                        .userId(USER_ID + "1")
                                        .build()
                                )
                                .build();
        when(productReviewDataService.findProductReviewByIdOrElseIllegal(anyLong()))
                .thenReturn(reviewFixture);

        assertThrows(
                CustomAccessDeniedException.class,
                () -> productReviewWriteUseCase.patchReview(reviewDTO, USER_ID)
        );

        verify(productReviewDataService, never())
                .saveProductReview(any(ProductReview.class));
    }

    @Test
    @DisplayName(value = "리뷰 삭제. 리뷰 아이디가 잘못된 경우")
    void deleteReviewWrongReviewId() {
        when(productReviewDataService.findProductReviewByIdOrElseIllegal(anyLong()))
                .thenThrow(IllegalArgumentException.class);

        assertThrows(
                IllegalArgumentException.class,
                () -> productReviewWriteUseCase.deleteReview(1L, USER_ID)
        );

        verify(productReviewDataService, never())
                .deleteProductReview(anyLong());
    }

    @Test
    @DisplayName(value = "리뷰 삭제. 작성자가 일치하지 않는 경우")
    void deleteReviewWriterNotEquals() {
        ProductReview reviewFixture = ProductReview.builder()
                .id(1L)
                .member(Member.builder()
                        .userId(USER_ID + "1")
                        .build()
                )
                .build();
        when(productReviewDataService.findProductReviewByIdOrElseIllegal(anyLong()))
                .thenReturn(reviewFixture);

        assertThrows(
                CustomAccessDeniedException.class,
                () -> productReviewWriteUseCase.deleteReview(1L, USER_ID)
        );

        verify(productReviewDataService, never())
                .deleteProductReview(anyLong());
    }
}
