package com.example.moduleapi.useCase;

import com.example.moduleapi.ModuleApiApplication;
import com.example.moduleapi.useCase.product.MainReadUserCase;
import com.example.moduleproduct.model.dto.main.out.MainListResponseDTO;
import com.example.moduleproduct.model.dto.page.ProductPageDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

@SpringBootTest(classes = ModuleApiApplication.class)
@EntityScan(basePackages = "com.example.modulecommon")
@EnableJpaRepositories(basePackages = "com.example")
@ComponentScan(basePackages = {"com.example.moduleconfig", "com.example.modulecommon"})
@ActiveProfiles("dev")
public class MainUseCaseTests {

    @Autowired
    private MainReadUserCase mainReadUserCase;

    @Test
    @DisplayName(value = "BEST 상품 조회")
    void findByBestProduct() {
        ProductPageDTO pageDTO = ProductPageDTO.builder()
                                            .pageNum(1)
                                            .keyword(null)
                                            .classification("BEST")
                                            .build();

        List<MainListResponseDTO> dto = mainReadUserCase.getMainProduct(pageDTO);

        System.out.println(dto);
    }
}
