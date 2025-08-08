package com.example.moduleproduct.usecase.productQnA;

import com.example.modulecommon.customException.CustomAccessDeniedException;
import com.example.modulecommon.customException.CustomNotFoundException;
import com.example.moduleproduct.model.dto.productQnA.business.ProductQnADetailDTO;
import com.example.moduleproduct.service.productQnA.ProductQnADataService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductQnAReadUseCaseUnitTest {

    @InjectMocks
    private ProductQnAReadUseCase productQnAReadUseCase;

    @Mock
    private ProductQnADataService productQnADataService;

    @Test
    @DisplayName(value = "상품 문의 상세 조회. 문의 아이디가 잘못된 경우")
    void getProductQnADetailWrongQnAId() {

        when(productQnADataService.getProductQnADetail(anyLong())).thenReturn(null);

        assertThrows(
                CustomNotFoundException.class,
                () -> productQnAReadUseCase.getProductQnADetail(1L, "writer")
        );

        verify(productQnADataService, never()).getProductQnADetailAllReplies(anyLong());
    }

    @Test
    @DisplayName(value = "상품 문의 상세 조회. 작성자가 일치하지 않는 경우")
    void getProductQnADetailWriterNotEquals() {
        ProductQnADetailDTO fixture = new ProductQnADetailDTO(
                1L,
                "testProductName",
                "tester",
                "testContent",
                LocalDateTime.now(),
                false
        );

        when(productQnADataService.getProductQnADetail(anyLong())).thenReturn(fixture);
        when(productQnADataService.getProductQnADetailAllReplies(anyLong())).thenReturn(Collections.emptyList());

        assertThrows(
                CustomAccessDeniedException.class,
                () -> productQnAReadUseCase.getProductQnADetail(1L, "writer")
        );
    }
}
