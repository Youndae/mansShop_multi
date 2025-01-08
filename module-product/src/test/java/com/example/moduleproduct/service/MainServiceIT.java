package com.example.moduleproduct.service;

import com.example.modulecommon.model.dto.response.PagingListResponseDTO;
import com.example.modulecommon.model.entity.Classification;
import com.example.modulecommon.model.entity.Product;
import com.example.moduleproduct.ModuleProductApplication;
import com.example.moduleproduct.fixture.ProductFixture;
import com.example.moduleproduct.model.dto.main.business.MainListDTO;
import com.example.moduleproduct.model.dto.main.out.MainListResponseDTO;
import com.example.moduleproduct.model.dto.page.ProductPageDTO;
import com.example.moduleproduct.repository.classification.ClassificationRepository;
import com.example.moduleproduct.repository.product.ProductRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@SpringBootTest(classes = ModuleProductApplication.class)
@EntityScan(basePackages = "com.example.modulecommon")
@EnableJpaRepositories(basePackages = "com.example")
@ComponentScan(basePackages = {"com.example.moduleconfig", "com.example.modulecommon"})
@ActiveProfiles("integration-test")
@Transactional
public class MainServiceIT {

    @Autowired
    private MainService mainService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ClassificationRepository classificationRepository;

    private List<Product> productList;

    @BeforeEach
    void init() {
        productList = ProductFixture.createProductList();
        List<Classification> classifications = ProductFixture.createClassificationList();

        classificationRepository.saveAll(classifications);
        productRepository.saveAll(productList);
    }

    /**
     * 정상적으로 베스트 상품이 조회되는지 확인하기 위해서는
     * 베스트 상품 목록이 있어야 한다.
     * 12개니까 최소 13개 이상의 데이터가 들어가야 한다.
     *
     * 그럼 Repository 테스트도 잘못된거고.
     * 다른 테스트에서 사용할 수 있도록 현재 Fixture는 그대로 유지한채로 추가적인 Fixture를 만들어서 사용해야 할 필요가 있음.
     *
     * Fixture 구조는
     * 20개의 데이터.
     * 1. 전체 리스트 생성 메소드
     * 2. 베스트 상품 리스트만 반환하는 메소드
     * 3. 새로운 상품 리스트만 반환하는 메소드
     * 4. 분류 검색을 위한 OUTER만 최신순으로 반환하는 메소드
     *
     * 데이터 구성
     * id = test + classification + product + i
     * sales =  += 10
     * discount = 10% 5개 50% 5개 나머지 0
     * stock = 5개 0
     *
     * classification -> OUTER, TOP
     *
     * 상품 하나당 옵션 3개
     *
     * 각 상황에 맞는 List<MainListResponseDTO>
     */

    @Test
    @DisplayName("베스트 상품 조회")
    void getBestProductList() {
        ProductPageDTO pageDTO = ProductFixture.createProductPageDTO("BEST");
        List<MainListResponseDTO> result = mainService.getBestAndNewProductList(pageDTO);
        List<MainListResponseDTO> fixture = ProductFixture.bestMainResponseListDTOList();

        Assertions.assertNotNull(result);
        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(fixture.size(), result.size());
        Assertions.assertEquals(fixture, result);
    }

    @Test
    @DisplayName("새로운 상품 조회")
    void getNewProductList() {
        ProductPageDTO pageDTO = ProductFixture.createProductPageDTO("NEW");
        List<Product> newProductList = ProductFixture.newProductFilter(productList);
        List<MainListDTO> dtoList = ProductFixture.createMainListDTOByProductList(newProductList);
        List<MainListResponseDTO> fixture = ProductFixture.mappingMainListResponseDTO(dtoList);
        List<MainListResponseDTO> result = Assertions.assertDoesNotThrow(() -> mainService.getBestAndNewProductList(pageDTO));

        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(fixture.size(), result.size());
        Assertions.assertEquals(fixture, result);
    }

    @Test
    @DisplayName("상품이 하나도 존재하지 않는 경우")
    void getEmptyProductList() {
        productRepository.deleteAll();
        ProductPageDTO pageDTO = ProductFixture.createProductPageDTO("BEST");
        List<MainListResponseDTO> result = Assertions.assertDoesNotThrow(() -> mainService.getBestAndNewProductList(pageDTO));

        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("OUTER 분류 상품 조회")
    void getOuterProductList() {
        ProductPageDTO pageDTO = ProductFixture.createProductPageDTO("OUTER");
        List<Product> outerList = ProductFixture.outerProductFilter(productList);
        int outerTotalElements = outerList.stream().filter(Product::isOpen).toList().size();
        List<MainListDTO> dtoList = ProductFixture.createMainListDTOByDataReverse(outerList);
        List<MainListResponseDTO> fixture = ProductFixture.mappingMainListResponseDTO(dtoList);
        PagingListResponseDTO<MainListResponseDTO> result = Assertions.assertDoesNotThrow(
                                                                    () -> mainService.getClassificationAndSearchProductList(pageDTO)
                                                            );

        Assertions.assertEquals(fixture.size(), result.content().size());
        Assertions.assertEquals(outerTotalElements, result.pagingData().getTotalElements());
        Assertions.assertEquals(0, result.pagingData().getNumber());
        Assertions.assertEquals(2, result.pagingData().getTotalPages());
        Assertions.assertEquals(fixture, result.content());
    }

    @Test
    @DisplayName("상품명에 TOP이 들어간 상품 검색")
    void searchProductListByTOP() {
        List<Product> topList = ProductFixture.topProductFilter(productList);
        int topTotalElements = topList.stream().filter(Product::isOpen).toList().size();
        List<MainListDTO> dtoList = ProductFixture.createMainListDTOByDataReverse(topList);
        List<MainListResponseDTO> fixture = ProductFixture.mappingMainListResponseDTO(dtoList);
        ProductPageDTO pageDTO = ProductPageDTO.builder()
                                                .pageNum(1)
                                                .keyword("TOP")
                                                .classification(null)
                                                .build();

        PagingListResponseDTO<MainListResponseDTO> result = Assertions.assertDoesNotThrow(
                                                                    () -> mainService.getClassificationAndSearchProductList(pageDTO)
                                                            );

        Assertions.assertEquals(fixture.size(), result.content().size());
        Assertions.assertEquals(topTotalElements, result.pagingData().getTotalElements());
        Assertions.assertEquals(0, result.pagingData().getNumber());
        Assertions.assertEquals(1, result.pagingData().getTotalPages());
        Assertions.assertEquals(fixture, result.content());
    }

    @Test
    @DisplayName("데이터가 존재하지 않는 SHOES가 들어간 상품명 검색")
    void searchEmptyProductList() {
        ProductPageDTO pageDTO = ProductPageDTO.builder()
                                                .pageNum(1)
                                                .keyword("SHOES")
                                                .classification(null)
                                                .build();

        PagingListResponseDTO<MainListResponseDTO> result = Assertions.assertDoesNotThrow(
                                                                    () -> mainService.getClassificationAndSearchProductList(pageDTO)
                                                            );

        Assertions.assertTrue(result.content().isEmpty());
        Assertions.assertEquals(0, result.pagingData().getTotalElements());
        Assertions.assertEquals(0, result.pagingData().getNumber());
        Assertions.assertEquals(0, result.pagingData().getTotalPages());
    }


}
