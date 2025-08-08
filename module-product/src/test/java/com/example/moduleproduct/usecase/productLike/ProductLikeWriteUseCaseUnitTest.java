package com.example.moduleproduct.usecase.productLike;

import com.example.modulecommon.customException.CustomAccessDeniedException;
import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.entity.Product;
import com.example.modulecommon.model.entity.ProductLike;
import com.example.moduleproduct.service.product.ProductDataService;
import com.example.moduleproduct.service.productLike.ProductLikeDataService;
import com.example.moduleproduct.service.productLike.ProductLikeDomainService;
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
public class ProductLikeWriteUseCaseUnitTest {

    @InjectMocks
    private ProductLikeWriteUseCase productLikeWriteUseCase;

    @Mock
    private ProductLikeDataService productLikeDataService;

    @Mock
    private ProductLikeDomainService productLikeDomainService;

    @Mock
    private UserDataService userDataService;

    @Mock
    private ProductDataService productDataService;

    @Test
    @DisplayName(value = "관심 상품 등록. 사용자 아이디가 null인 경우")
    void likeProductUserIdIsNull() {
        assertThrows(
                CustomAccessDeniedException.class,
                () -> productLikeWriteUseCase.likeProduct("testProductId", null)
        );

        verify(userDataService, never()).getMemberByUserIdOrElseAccessDenied(any());
        verify(productDataService, never()).getProductByIdOrElseIllegal(any());
        verify(productLikeDomainService, never()).buildLikeProduct(any(Member.class), any(Product.class));
        verify(productLikeDataService, never()).saveProductLike(any(ProductLike.class));
    }

    @Test
    @DisplayName(value = "관심 상품 등록. 사용자 아이디가 존재하지 않는 아이디인 경우")
    void likeProductWrongUserId() {
        when(userDataService.getMemberByUserIdOrElseAccessDenied(any())).thenThrow(CustomAccessDeniedException.class);

        assertThrows(
                CustomAccessDeniedException.class,
                () -> productLikeWriteUseCase.likeProduct("testProductId", "tester")
        );

        verify(productDataService, never()).getProductByIdOrElseIllegal(any());
        verify(productLikeDomainService, never()).buildLikeProduct(any(Member.class), any(Product.class));
        verify(productLikeDataService, never()).saveProductLike(any(ProductLike.class));
    }

    @Test
    @DisplayName(value = "관심 상품 등록. 사용자 아이디가 null인 경우")
    void deleteLikeProductUserIdIsNull() {
        assertThrows(
                CustomAccessDeniedException.class,
                () -> productLikeWriteUseCase.deleteProductLike("testProductId", null)
        );

        verify(userDataService, never()).getMemberByUserIdOrElseAccessDenied(any());
        verify(productDataService, never()).getProductByIdOrElseIllegal(any());
        verify(productLikeDomainService, never()).buildLikeProduct(any(Member.class), any(Product.class));
        verify(productLikeDataService, never()).deleteProductLike(any(ProductLike.class));
    }

    @Test
    @DisplayName(value = "관심 상품 등록. 사용자 아이디가 존재하지 않는 아이디인 경우")
    void deleteLikeProductWrongUserId() {
        when(userDataService.getMemberByUserIdOrElseAccessDenied(any())).thenThrow(CustomAccessDeniedException.class);

        assertThrows(
                CustomAccessDeniedException.class,
                () -> productLikeWriteUseCase.deleteProductLike("testProductId", "tester")
        );

        verify(productDataService, never()).getProductByIdOrElseIllegal(any());
        verify(productLikeDomainService, never()).buildLikeProduct(any(Member.class), any(Product.class));
        verify(productLikeDataService, never()).deleteProductLike(any(ProductLike.class));
    }
}
