package com.example.moduleproduct.service;

import com.example.modulecommon.model.dto.response.PagingListResponseDTO;
import com.example.moduleproduct.ModuleProductApplication;
import com.example.moduleproduct.fixture.ProductFixture;
import com.example.moduleproduct.model.dto.main.business.MainListDTO;
import com.example.moduleproduct.model.dto.main.out.MainListResponseDTO;
import com.example.moduleproduct.model.dto.page.ProductPageDTO;
import com.example.moduleproduct.repository.product.ProductRepository;
import com.example.moduleproduct.service.fixture.MainServiceFixture;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;

@SpringBootTest(classes = ModuleProductApplication.class)
@EntityScan(basePackages = "com.example.modulecommon")
@EnableJpaRepositories(basePackages = "com.example")
@ComponentScan(basePackages = {"com.example.moduleconfig", "com.example.modulecommon"})
@ActiveProfiles("test")
public class MainServiceUnitTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private MainService mainService;

    @Test
    @DisplayName("BEST 상품 조회 성공")
    void getBestAndNewProductList() {
        ProductPageDTO pageDTO = ProductFixture.createProductPageDTO("BEST");
        MainListDTO product1 = MainServiceFixture.createMainListDTO(1);
        MainListDTO product2 = MainServiceFixture.createMainListDTO(2);

        List<MainListDTO> mockProductList = Arrays.asList(product1, product2);
        when(productRepository.getProductDefaultList(pageDTO))
                .thenReturn(mockProductList);

        List<MainListResponseDTO> response = mainService.getBestAndNewProductList(pageDTO);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(2, response.size());
        Assertions.assertEquals(product1.productName(), response.get(0).productName());
    }

    @Test
    @DisplayName("BEST 상품이 하나도 존재하지 않는다면 빈 리스트가 반환되어야 한다.")
    void getBestAndNewProductListEmpty() {
        ProductPageDTO pageDTO = ProductFixture.createProductPageDTO("BEST");
        when(productRepository.getProductDefaultList(pageDTO))
                .thenReturn(Collections.emptyList());

        List<MainListResponseDTO> response = Assertions.assertDoesNotThrow(() -> mainService.getBestAndNewProductList(pageDTO));

        Assertions.assertTrue(response.isEmpty());
    }

    @Test
    @DisplayName("분류별 상품 조회")
    void getClassificationProductList() {
        ProductPageDTO pageDTO = ProductFixture.createProductPageDTO("OUTER");
        MainListDTO product1 = MainServiceFixture.createMainListDTO(1);
        MainListDTO product2 = MainServiceFixture.createMainListDTO(2);
        Pageable pageable = ProductFixture.createProductListPageable();

        List<MainListDTO> productList = Arrays.asList(product1, product2);
        Page<MainListDTO> mockList = new PageImpl<>(productList);
        when(productRepository.getProductClassificationAndSearchList(pageDTO, pageable))
                .thenReturn(mockList);

        PagingListResponseDTO<MainListResponseDTO> response = mainService.getClassificationAndSearchProductList(pageDTO);

        Assertions.assertEquals(2, response.pagingData().getTotalElements());
        Assertions.assertFalse(response.pagingData().isEmpty());
        Assertions.assertEquals(1, response.pagingData().getTotalPages());
        Assertions.assertEquals(0, response.pagingData().getNumber());
        Assertions.assertFalse(response.content().isEmpty());
        Assertions.assertEquals(2, response.content().size());
    }

    @Test
    @DisplayName("분류별 상품이 없는 경우 예외가 아닌 empty content와 pagingData를 반환")
    void getClassificationProductListFail() {
        ProductPageDTO pageDTO = ProductFixture.createProductPageDTO("OUTER");
        Pageable pageable = ProductFixture.createProductListPageable();
        when(productRepository.getProductClassificationAndSearchList(pageDTO, pageable))
                .thenReturn(new PageImpl<>(Collections.emptyList(), pageable, 0));

        PagingListResponseDTO<MainListResponseDTO> response = Assertions.assertDoesNotThrow(() -> mainService.getClassificationAndSearchProductList(pageDTO));

        Assertions.assertTrue(response.content().isEmpty());
        Assertions.assertEquals(0, response.pagingData().getTotalPages());
        Assertions.assertEquals(0, response.pagingData().getTotalElements());
        Assertions.assertEquals(0, response.pagingData().getNumber());
    }

}
