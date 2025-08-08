package com.example.moduleproduct.usecase.productReview;

import com.example.modulecommon.customException.CustomAccessDeniedException;
import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.entity.Product;
import com.example.modulecommon.model.entity.ProductReview;
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
public class ProductReviewReadUseCaseUnitTest {

    @InjectMocks
    private ProductReviewReadUseCase productReviewReadUseCase;

    @Mock
    private ProductReviewDataService productReviewDataService;

    @Test
    @DisplayName(value = "리뷰 수정을 위한 데이터 조회. 리뷰 아이디가 잘못된 경우")
    void getPatchReviewWrongReviewId() {

        when(productReviewDataService.findProductReviewByIdOrElseIllegal(anyLong()))
                .thenThrow(IllegalArgumentException.class);

        assertThrows(
                IllegalArgumentException.class,
                () -> productReviewReadUseCase.getPatchReview(0L, "tester")
        );
    }

    @Test
    @DisplayName(value = "리뷰 수정을 위한 데이터 조회. 작성자가 일치하지 않는 경우")
    void getPatchReviewWriterNotEquals() {
        ProductReview fixture = ProductReview.builder()
                .id(1L)
                .member(Member.builder().userId("tester").build())
                .product(Product.builder().id("testId").productName("testProduct").build())
                .build();

        when(productReviewDataService.findProductReviewByIdOrElseIllegal(anyLong()))
                .thenReturn(fixture);

        assertThrows(
                CustomAccessDeniedException.class,
                () -> productReviewReadUseCase.getPatchReview(0L, "writer")
        );
    }
}
