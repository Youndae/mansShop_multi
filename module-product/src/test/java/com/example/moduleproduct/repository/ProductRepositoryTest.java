package com.example.moduleproduct.repository;

import com.example.modulecommon.model.entity.Classification;
import com.example.modulecommon.model.entity.Product;
import com.example.moduleproduct.ModuleProductApplication;
import com.example.moduleproduct.fixture.ProductFixture;
import com.example.moduleproduct.model.dto.main.business.MainListDTO;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@SpringBootTest(classes = ModuleProductApplication.class)
@EntityScan(basePackages = "com.example.modulecommon")
@EnableJpaRepositories(basePackages = "com.example")
@ComponentScan(basePackages = {"com.example.moduleconfig", "com.example.modulecommon"})
@ActiveProfiles("test")
@Transactional
public class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ClassificationRepository classificationRepository;

    private List<Product> productList;

    /**
     * 상품 리스트, 상품 분류 데이터 저장
     * 상품 리스트는 15개의 OUTER, 5개의 TOP으로 구성.
     * 상품 하나당 3개의 옵션 존재.
     * TOP에서 2개의 상품은 isOpen = false로 비공개 상태.
     * 1개 상품은 2개의 옵션이 isOpen = false로 옵션 2개 비공개 상태.
     * OUTER 0 ~ 4번까지는 상품 옵션 재고가 0
     * OUTER 5 ~ 14까지는 상품 할인율이 10% 5개, 50% 5개.
     */
    @BeforeEach
    void init() {
        productList = ProductFixture.createProductList();
        List<Classification> classifications = ProductFixture.createClassificationList();

        classificationRepository.saveAll(classifications);
        productRepository.saveAll(productList);
    }

    @Test
    @DisplayName(value = "BEST 상품 조회")
    void findByBestProduct() {
        ProductPageDTO pageDTO = ProductFixture.createProductPageDTO("BEST");
        List<MainListDTO> result = productRepository.getProductDefaultList(pageDTO);
        List<Product> bestProductList = ProductFixture.bestProductFilter(productList);
        List<MainListDTO> fixture = ProductFixture.createMainListDTOByProductList(bestProductList);

        Assertions.assertEquals(fixture.size(), result.size());
        Assertions.assertEquals(fixture, result);
    }

    @Test
    @DisplayName(value = "New 상품 조회")
    void findByNewProduct() {
        ProductPageDTO pageDTO = ProductFixture.createProductPageDTO("NEW");
        List<MainListDTO> result = productRepository.getProductDefaultList(pageDTO);
        List<Product> newProductList = ProductFixture.newProductFilter(productList);
        List<MainListDTO> fixture = ProductFixture.createMainListDTOByProductList(newProductList);

        Assertions.assertEquals(fixture.size(), result.size());
        Assertions.assertEquals(fixture, result);
    }

    @Test
    @DisplayName(value = "분류별 상품 조회. OUTER 조회")
    void findByClassificationProduct() {
        List<Product> outerList = ProductFixture.outerProductFilter(productList);
        List<MainListDTO> fixture = ProductFixture.createMainListDTOByDataReverse(outerList);
        ProductPageDTO pageDTO = ProductFixture.createProductPageDTO("OUTER");
        Pageable pageable = ProductFixture.createProductListPageable();
        Page<MainListDTO> result = productRepository.getProductClassificationAndSearchList(pageDTO, pageable);

        Assertions.assertEquals(fixture, result.getContent());
        Assertions.assertEquals(outerList.size(), result.getTotalElements());
    }

    @Test
    @DisplayName("상품 검색. 상품명에 TOP이 들어간 상품 검색")
    void searchProduct() {
        List<Product> topList = ProductFixture.topProductFilter(productList);
        List<MainListDTO> fixture = ProductFixture.createMainListDTOByDataReverse(topList);
        ProductPageDTO pageDTO = ProductPageDTO.builder()
                                                .pageNum(1)
                                                .keyword("TOP")
                                                .classification(null)
                                                .build();
        Pageable pageable = ProductFixture.createProductListPageable();
        Page<MainListDTO> result = productRepository.getProductClassificationAndSearchList(pageDTO, pageable);

        Assertions.assertEquals(fixture, result.getContent());
        Assertions.assertEquals(fixture.size(), result.getTotalElements());
    }


}
