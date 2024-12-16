package com.example.moduleproduct.repository;

import com.example.modulecommon.model.dto.response.PagingListResponseDTO;
import com.example.moduleproduct.ModuleProductApplication;
import com.example.moduleproduct.model.dto.main.business.MainListDTO;
import com.example.moduleproduct.model.dto.page.ProductPageDTO;
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

@SpringBootTest(classes = ModuleProductApplication.class)
@EntityScan(basePackages = "com.example.modulecommon")
@EnableJpaRepositories(basePackages = "com.example")
@ComponentScan(basePackages = {"com.example.moduleconfig", "com.example.modulecommon"})
@ActiveProfiles("dev")
public class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    @DisplayName(value = "BEST 상품 조회")
    void findByBestProduct() {
        ProductPageDTO pageDTO = ProductPageDTO.builder()
                                                .pageNum(1)
                                                .keyword(null)
                                                .classification("BEST")
                                                .build();
        List<MainListDTO> result = productRepository.getProductDefaultList(pageDTO);

        System.out.println(result);
    }

    @Test
    @DisplayName(value = "분류별 상품 조회. OUTER 조회")
    void findByClassificationProduct() {
        ProductPageDTO pageDTO = ProductPageDTO.builder()
                                                .pageNum(1)
                                                .keyword(null)
                                                .classification("OUTER")
                                                .build();
        Pageable pageable = PageRequest.of(pageDTO.pageNum() - 1,
                pageDTO.mainProductAmount(),
                Sort.by("createdAt").descending());

        Page<MainListDTO> result = productRepository.getProductClassificationAndSearchList(pageDTO, pageable);

        System.out.println("totalElements : " + result.getTotalElements());
        System.out.println("content" + result.getContent());
        System.out.println("content size : " + result.getContent().size());
    }
}
