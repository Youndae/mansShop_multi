package com.example.moduleproduct.usecase.product.admin;

import com.example.modulecommon.model.dto.response.PagingListDTO;
import com.example.modulecommon.model.entity.Classification;
import com.example.modulecommon.utils.PaginationUtils;
import com.example.moduleproduct.model.dto.admin.product.business.AdminProductStockDataDTO;
import com.example.moduleproduct.model.dto.admin.product.out.AdminProductListDTO;
import com.example.moduleproduct.model.dto.admin.product.out.AdminProductStockDTO;
import com.example.moduleproduct.model.dto.page.AdminProductPageDTO;
import com.example.moduleproduct.service.product.ProductDataService;
import com.example.moduleproduct.service.product.ProductDomainService;
import com.example.moduleproduct.usecase.admin.product.AdminProductReadUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminProductReadUseCaseUnitTest {

    @InjectMocks
    private AdminProductReadUseCase adminProductReadUseCase;

    @Mock
    private ProductDataService productDataService;

    @Mock
    private ProductDomainService productDomainService;

    @Test
    @DisplayName(value = "상품 목록 조회")
    void getProductList() {
        List<AdminProductListDTO> fixtureList = IntStream.range(0, 20)
                .mapToObj(v -> new AdminProductListDTO(
                        "testProductId" + v,
                        "OUTER",
                        "testProductName" + v,
                        10 * v,
                        3L,
                        1000 * v
                ))
                .toList();
        AdminProductPageDTO pageDTO = new AdminProductPageDTO(1);
        long totalElements = 30L;
        int totalPages = PaginationUtils.getTotalPages((int) totalElements, pageDTO.amount());
        when(productDataService.getAdminProductPageList(any(AdminProductPageDTO.class))).thenReturn(fixtureList);
        when(productDataService.getAdminProductListFullCount(any(AdminProductPageDTO.class))).thenReturn(totalElements);

        PagingListDTO<AdminProductListDTO> result = assertDoesNotThrow(() -> adminProductReadUseCase.getProductList(pageDTO));

        assertNotNull(result);
        assertEquals(fixtureList.size(), result.content().size());
        assertEquals(totalElements, result.pagingData().getTotalElements());
        assertEquals(totalPages, result.pagingData().getTotalPages());
        assertFalse(result.pagingData().isEmpty());
        assertEquals(fixtureList, result.content());
    }

    @Test
    @DisplayName(value = "상품 목록 조회. 데이터가 없는 경우")
    void getProductListEmpty() {
        AdminProductPageDTO pageDTO = new AdminProductPageDTO(1);

        when(productDataService.getAdminProductPageList(any(AdminProductPageDTO.class))).thenReturn(Collections.emptyList());


        PagingListDTO<AdminProductListDTO> result = assertDoesNotThrow(() -> adminProductReadUseCase.getProductList(pageDTO));

        verify(productDataService, never()).getAdminProductListFullCount(any(AdminProductPageDTO.class));

        assertNotNull(result);
        assertTrue(result.content().isEmpty());
        assertEquals(0, result.pagingData().getTotalElements());
        assertEquals(0, result.pagingData().getTotalPages());
        assertTrue(result.pagingData().isEmpty());
    }

    @Test
    @DisplayName(value = "상품 분류 아이디 목록 조회")
    void getProductClassificationIdList() {
        Classification outer = Classification.builder().id("OUTER").classificationStep(1).build();
        Classification top = Classification.builder().id("TOP").classificationStep(2).build();
        List<Classification> classificationFixture = List.of(outer, top);
        List<String> resultFixture = List.of("OUTER", "TOP");

        when(productDataService.getAllClassification()).thenReturn(classificationFixture);

        List<String> result = assertDoesNotThrow(() -> adminProductReadUseCase.getProductClassificationIdList());
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(classificationFixture.size(), result.size());
        assertEquals(resultFixture, result);
    }

    @Test
    @DisplayName(value = "상품 재고 목록 조회")
    void getProductStockList() {
        AdminProductPageDTO pageDTO = new AdminProductPageDTO(1);
        List<AdminProductStockDataDTO> fixtureList = IntStream.range(0, 20)
                .mapToObj(v -> new AdminProductStockDataDTO(
                        "testProductId" + v,
                        "OUTER",
                        "testProductName" + v,
                        10 * v,
                        v % 2 == 0
                ))
                .toList();
        long totalElements = 30L;
        int totalPages = PaginationUtils.getTotalPages((int) totalElements, pageDTO.amount());
        List<AdminProductStockDTO> resultFixture = fixtureList.stream()
                .map(v -> new AdminProductStockDTO(
                        v.productId(),
                        v.classification(),
                        v.productName(),
                        v.totalStock(),
                        v.isOpen(),
                        Collections.emptyList()
                ))
                .toList();


        when(productDataService.getAllProductStockData(any(AdminProductPageDTO.class))).thenReturn(fixtureList);
        when(productDataService.getAllProductStockDataCount(any(AdminProductPageDTO.class))).thenReturn(totalElements);
        when(productDataService.getAllProductOptionStockByProductIds(anyList())).thenReturn(Collections.emptyList());
        when(productDomainService.mapProductStockDTO(anyList(), anyList())).thenReturn(resultFixture);

        PagingListDTO<AdminProductStockDTO> result = assertDoesNotThrow(() -> adminProductReadUseCase.getProductStockList(pageDTO));

        assertNotNull(result);
        assertEquals(fixtureList.size(), result.content().size());
        assertEquals(totalElements, result.pagingData().getTotalElements());
        assertEquals(totalPages, result.pagingData().getTotalPages());
        assertFalse(result.pagingData().isEmpty());
        assertEquals(resultFixture, result.content());
    }

    @Test
    @DisplayName(value = "상품 재고 목록 조회. 데이터가 없는 경우")
    void getProductStockListEmpty() {
        AdminProductPageDTO pageDTO = new AdminProductPageDTO(1);

        when(productDataService.getAllProductStockData(any(AdminProductPageDTO.class))).thenReturn(Collections.emptyList());

        PagingListDTO<AdminProductStockDTO> result = assertDoesNotThrow(() -> adminProductReadUseCase.getProductStockList(pageDTO));

        verify(productDataService, never()).getAllProductStockDataCount(any(AdminProductPageDTO.class));
        verify(productDataService, never()).getAllProductOptionStockByProductIds(anyList());
        verify(productDomainService, never()).mapProductStockDTO(anyList(), anyList());

        assertNotNull(result);
        assertTrue(result.content().isEmpty());
        assertEquals(0, result.pagingData().getTotalElements());
        assertEquals(0, result.pagingData().getTotalPages());
        assertTrue(result.pagingData().isEmpty());
    }
}
