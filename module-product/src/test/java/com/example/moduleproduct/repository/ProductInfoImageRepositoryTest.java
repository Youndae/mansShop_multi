package com.example.moduleproduct.repository;

import com.example.modulecommon.model.entity.Product;
import com.example.modulecommon.model.entity.ProductInfoImage;
import com.example.moduleproduct.ModuleProductApplication;
import com.example.moduleproduct.fixture.ProductFixture;
import com.example.moduleproduct.repository.product.ProductRepository;
import com.example.moduleproduct.repository.productInfoImage.ProductInfoImageRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

@SpringBootTest(classes = ModuleProductApplication.class)
@EntityScan(basePackages = "com.example.modulecommon")
@EnableJpaRepositories(basePackages = "com.example")
@ComponentScan(basePackages = {"com.example.moduleconfig", "com.example.modulecommon"})
@ActiveProfiles("test")
public class ProductInfoImageRepositoryTest {

    @Autowired
    private ProductInfoImageRepository productInfoImageRepository;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void init() {
        Product product = ProductFixture.createProductAndImage();

        productRepository.save(product);
    }

    @Test
    @DisplayName("상품의 정보 이미지 리스트 조회")
    void findByProductInfoImage() {
        Product product = ProductFixture.createProductAndImage();
        List<String> infoImageList = product.getProductInfoImageSet()
                                            .stream()
                                            .map(ProductInfoImage::getImageName)
                                            .sorted()
                                            .toList();
        List<String> result = productInfoImageRepository.findByProductId(product.getId());

        Assertions.assertEquals(infoImageList, result);
    }
}
