package com.example.moduleproduct.usecase.productQnA;

import com.example.modulecommon.customException.CustomAccessDeniedException;
import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.entity.ProductQnA;
import com.example.moduleproduct.model.dto.product.in.ProductQnAPostDTO;
import com.example.moduleproduct.service.product.ProductDataService;
import com.example.moduleproduct.service.productQnA.ProductQnADataService;
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
public class ProductQnAWriteUseCaseUnitTest {

    @InjectMocks
    private ProductQnAWriteUseCase productQnAWriteUseCase;

    @Mock
    private ProductQnADataService productQnADataService;

    @Mock
    private UserDataService userDataService;

    @Mock
    private ProductDataService productDataService;

    private ProductQnAPostDTO getProductQnAPostDTO() {
        return new ProductQnAPostDTO("testProductId", "testContent");
    }

    @Test
    @DisplayName(value = "상품 문의 등록. 사용자 아이디가 잘못된 경우")
    void postProductQnAWrongUserId() {
        ProductQnAPostDTO postDTO = getProductQnAPostDTO();

        when(userDataService.getMemberByUserIdOrElseAccessDenied(any())).thenThrow(CustomAccessDeniedException.class);

        assertThrows(
                CustomAccessDeniedException.class,
                () -> productQnAWriteUseCase.postProductQnA(postDTO, "writer")
        );

        verify(productDataService, never()).getProductByIdOrElseIllegal(any());
        verify(productQnADataService, never()).saveProductQnA(any(ProductQnA.class));
    }

    @Test
    @DisplayName(value = "상품 문의 등록. 상품 아이디가 잘못된 경우")
    void postProductQnAWrongProductId() {
        ProductQnAPostDTO postDTO = getProductQnAPostDTO();
        Member member = Member.builder().userId("tester").build();
        when(userDataService.getMemberByUserIdOrElseAccessDenied(any())).thenReturn(member);
        when(productDataService.getProductByIdOrElseIllegal(any())).thenThrow(IllegalArgumentException.class);

        assertThrows(
                IllegalArgumentException.class,
                () -> productQnAWriteUseCase.postProductQnA(postDTO, "writer")
        );

        verify(productQnADataService, never()).saveProductQnA(any(ProductQnA.class));
    }

    @Test
    @DisplayName(value = "상품 문의 삭제. 상품 문의 아이디가 잘못된 경우")
    void deleteProductQnAWrongQnAId() {
        when(productQnADataService.findProductQnAByIdOrElseIllegal(anyLong())).thenThrow(IllegalArgumentException.class);

        assertThrows(
                IllegalArgumentException.class,
                () -> productQnAWriteUseCase.deleteProductQnA(1L, "writer")
        );

        verify(productQnADataService, never()).deleteProductQnAById(anyLong());
    }

    @Test
    @DisplayName(value = "상품 문의 삭제. 작성자가 일치하지 않는 경우")
    void deleteProductQnAWriterNotEquals() {
        ProductQnA productQnA = ProductQnA.builder()
                                .id(1L)
                                .member(Member.builder().userId("writer").build())
                                .build();
        when(productQnADataService.findProductQnAByIdOrElseIllegal(anyLong())).thenReturn(productQnA);

        assertThrows(
                CustomAccessDeniedException.class,
                () -> productQnAWriteUseCase.deleteProductQnA(1L, "wrongUser")
        );

        verify(productQnADataService, never()).deleteProductQnAById(anyLong());
    }
}
