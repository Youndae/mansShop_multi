package com.example.moduleproduct.repository;

import com.example.modulecommon.model.entity.Product;
import com.example.modulecommon.model.entity.ProductOption;
import com.example.moduleproduct.ModuleProductApplication;
import com.example.moduleproduct.fixture.ProductFixture;
import com.example.moduleproduct.model.dto.product.business.ProductOptionDTO;
import com.example.moduleproduct.repository.product.ProductRepository;
import com.example.moduleproduct.repository.productOption.ProductOptionRepository;
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
import java.util.stream.Collectors;

@SpringBootTest(classes = ModuleProductApplication.class)
@EntityScan(basePackages = "com.example.modulecommon")
@EnableJpaRepositories(basePackages = "com.example")
@ComponentScan(basePackages = {"com.example.moduleconfig", "com.example.modulecommon"})
@ActiveProfiles("test")
public class ProductOptionRepositoryTest {

    @Autowired
    private ProductOptionRepository productOptionRepository;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void init() {
        Product product = ProductFixture.createProductAndOption();
        productRepository.save(product);
    }

    @Test
    @DisplayName("상품 옵션 리스트 조회")
    void getProductOption() {
        Product product = ProductFixture.createProductAndOption();
        List<ProductOptionDTO> expectedDTOs = product.getProductOptionSet().stream()
                .map(option -> new ProductOptionDTO(
                        1L,
                        option.getSize(),
                        option.getColor(),
                        option.getStock()
                )).toList();

        List<ProductOptionDTO> result = productOptionRepository.findByDetailOption(product.getId());

        Assertions.assertEquals(expectedDTOs.size(), result.size());

        for(int i = 0; i < expectedDTOs.size(); i++) {
            ProductOptionDTO expected = expectedDTOs.get(i);
            ProductOptionDTO actual = result.get(i);

            Assertions.assertEquals(expected.size(), actual.size());
            Assertions.assertEquals(expected.color(), actual.color());
            Assertions.assertEquals(expected.stock(), actual.stock());
        }
    }
}
