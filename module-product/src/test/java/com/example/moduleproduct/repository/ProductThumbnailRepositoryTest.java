package com.example.moduleproduct.repository;

import com.example.modulecommon.model.entity.Classification;
import com.example.modulecommon.model.entity.Product;
import com.example.modulecommon.model.entity.ProductThumbnail;
import com.example.moduleproduct.ModuleProductApplication;
import com.example.moduleproduct.fixture.ProductFixture;
import com.example.moduleproduct.repository.classification.ClassificationRepository;
import com.example.moduleproduct.repository.product.ProductRepository;
import com.example.moduleproduct.repository.productThumbnail.ProductThumbnailRepository;
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
public class ProductThumbnailRepositoryTest {

    @Autowired
    private ProductThumbnailRepository productThumbnailRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ClassificationRepository classificationRepository;

    /**
     * 상품 썸네일 리스트 조회를 위해 ProductThumbnail을 담고 있는 Product Entity 생성 후 save
     */
    @BeforeEach
    void init() {
        Product product = ProductFixture.createProductAndImage();
        List<Classification> classifications = ProductFixture.createClassificationList();

        classificationRepository.saveAll(classifications);
        productRepository.save(product);
    }

    @Test
    @DisplayName("상품의 썸네일 이미지 리스트 조회")
    void findByProductThumbnail() {
        Product product = ProductFixture.createProductAndImage();
        List<String> thumbnailList = product.getProductThumbnailSet()
                                            .stream()
                                            .map(ProductThumbnail::getImageName)
                                            .sorted()
                                            .toList();
        List<String> result = productThumbnailRepository.findByProductId(product.getId());

        Assertions.assertEquals(thumbnailList, result);
    }
}
