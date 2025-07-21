package com.example.moduleproduct.service.unit;

import com.example.modulecommon.model.dto.response.PagingListDTO;
import com.example.moduleproduct.model.dto.main.business.MainListDTO;
import com.example.moduleproduct.model.dto.main.out.MainListResponseDTO;
import com.example.moduleproduct.model.dto.page.MainPageDTO;
import com.example.moduleproduct.repository.product.ProductRepository;
import com.example.moduleproduct.service.MainService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
public class MainServiceUnitTest {

    @InjectMocks
    private MainService mainService;

    @Mock
    private ProductRepository productRepository;

    @Test
    @DisplayName(value = "베스트 상품 리스트 조회")
    void getBestProductList() {
        MainPageDTO pageDTO = new MainPageDTO( "BEST");
        List<MainListDTO> resultList = List.of(
                new MainListDTO(
                        "testProductId",
                        "testProductName",
                        "testThumbnail",
                        10000,
                        10,
                        100
                )
        );

        when(productRepository.findListDefault(pageDTO)).thenReturn(resultList);

        List<MainListResponseDTO> result = assertDoesNotThrow(() -> mainService.getBestAndNewList(pageDTO));

        assertEquals(resultList.size(), result.size());
    }

    @Test
    @DisplayName(value = "새로운 상품 리스트 조회")
    void getNewProductList() {
        MainPageDTO pageDTO = new MainPageDTO( "NEW");
        List<MainListDTO> resultList = List.of(
                new MainListDTO(
                        "testProductId",
                        "testProductName",
                        "testThumbnail",
                        10000,
                        10,
                        100
                )
        );

        when(productRepository.findListDefault(pageDTO)).thenReturn(resultList);

        List<MainListResponseDTO> result = assertDoesNotThrow(()-> mainService.getBestAndNewList(pageDTO));

        assertEquals(resultList.size(), result.size());
    }

    @Test
    @DisplayName(value = "새로운 상품 리스트 조회. 상품 데이터가 없는 경우")
    void getBestAndNewListEmpty() {
        MainPageDTO pageDTO = new MainPageDTO( "NEW");
        List<MainListDTO> resultList = Collections.emptyList();

        when(productRepository.findListDefault(pageDTO)).thenReturn(resultList);

        List<MainListResponseDTO> result = assertDoesNotThrow(() -> mainService.getBestAndNewList(pageDTO));

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName(value = "상품 분류에 따른 목록 조회")
    void getClassificationAndSearchList() {
        MainPageDTO pageDTO = new MainPageDTO( "OUTER");
        List<MainListDTO> resultList = List.of(
                new MainListDTO(
                        "testProductId",
                        "testProductName",
                        "testThumbnail",
                        10000,
                        10,
                        100
                )
        );
        Pageable pageable =  PageRequest.of(pageDTO.pageNum() - 1
                , pageDTO.amount()
                , Sort.by("createdAt").descending()
        );
        Page<MainListDTO> resultPages = new PageImpl<>(resultList);

        when(productRepository.findListPageable(pageDTO, pageable)).thenReturn(resultPages);

        PagingListDTO<MainListResponseDTO> result = assertDoesNotThrow(() ->mainService.getClassificationAndSearchList(pageDTO));

        assertNotNull(result);
        assertEquals(resultList.size(), result.pagingData().getTotalElements());
        assertFalse(result.pagingData().isEmpty());
        assertEquals(1, result.pagingData().getTotalPages());
        assertEquals(resultList.size(), result.content().size());
    }
}
