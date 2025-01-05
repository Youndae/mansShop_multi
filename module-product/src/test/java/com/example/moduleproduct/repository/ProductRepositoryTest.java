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

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@SpringBootTest(classes = ModuleProductApplication.class)
@EntityScan(basePackages = "com.example.modulecommon")
@EnableJpaRepositories(basePackages = "com.example")
@ComponentScan(basePackages = {"com.example.moduleconfig", "com.example.modulecommon"})
@ActiveProfiles("test")
public class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ClassificationRepository classificationRepository;

    /**
     * 상품 조회 테스트를 위해 여러 Product Entity를 생성하고 save
     * 상품 분류인 Classification은 별도로 생성하지 않고 생성된 ProductList에서 Set으로 새로 매핑하도록 처리.
     * OUTER5개, TOP1개로 구성되어 있기 때문에 Set으로 생성해 OUTER, TOP만 갖고 저장하게 됨.
     */
    @BeforeEach
    void init() {
        List<Product> productList = ProductFixture.createProductList();
        Set<Classification> classifications = productList.stream().map(Product::getClassification).collect(Collectors.toSet());

        classificationRepository.saveAll(classifications);
        productRepository.saveAll(productList);
    }

    @Test
    @DisplayName(value = "BEST 상품 조회")
    void findByBestProduct() {
        List<Product> productList = ProductFixture.createProductList();
        ProductPageDTO pageDTO = ProductFixture.createProductPageDTO("BEST");
        List<MainListDTO> result = productRepository.getProductDefaultList(pageDTO);
        System.out.println("result : " + result);
        Assertions.assertEquals(productList.size(), result.size());

        for(int i = 0; i < productList.size(); i++){
            Product product = productList.get(i);
            MainListDTO resultDTO = result.get(i);
            Assertions.assertEquals(product.getId(), resultDTO.productId());
            Assertions.assertEquals(product.getProductName(), resultDTO.productName());
        }
    }

    @Test
    @DisplayName(value = "분류별 상품 조회. OUTER 조회")
    void findByClassificationProduct() {
        List<Product> outerList = ProductFixture.createProductListByOUTER();
        ProductPageDTO pageDTO = ProductFixture.createProductPageDTO("OUTER");
        Pageable pageable = ProductFixture.createProductListPageable();
        Page<MainListDTO> result = productRepository.getProductClassificationAndSearchList(pageDTO, pageable);

        Assertions.assertEquals(outerList.size(), result.getTotalElements());

        for(int i = 0; i < outerList.size(); i++) {
            Product product = outerList.get(outerList.size() - i - 1);
            MainListDTO resultDTO = result.getContent().get(i);

            Assertions.assertEquals(product.getId(), resultDTO.productId());
            Assertions.assertEquals(product.getProductName(), resultDTO.productName());
        }
    }
}
