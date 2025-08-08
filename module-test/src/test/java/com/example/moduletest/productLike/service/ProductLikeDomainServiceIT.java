package com.example.moduletest.productLike.service;

import com.example.modulecommon.customException.CustomNotFoundException;
import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.entity.Product;
import com.example.modulecommon.model.entity.ProductLike;
import com.example.moduleproduct.service.productLike.ProductLikeDomainService;
import com.example.moduletest.ModuleTestApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = ModuleTestApplication.class)
@EntityScan(basePackages = "com.example.modulecommon")
@EnableJpaRepositories(basePackages = "com.example")
@ComponentScan(basePackages = {"com.example.moduleconfig", "com.example.modulecommon"})
@ActiveProfiles("test")
@Transactional
public class ProductLikeDomainServiceIT {

    @Autowired
    private ProductLikeDomainService productLikeDomainService;

    @Test
    @DisplayName(value = "LikeProduct Entity build")
    void buildLikeProduct() {
        Member member = Member.builder().userId("testUser").build();
        Product product = Product.builder().id("testProductId").build();

        ProductLike result = assertDoesNotThrow(() -> productLikeDomainService.buildLikeProduct(member, product));
        assertNotNull(result);
        assertEquals(member.getUserId(), result.getMember().getUserId());
        assertEquals(product.getId(), result.getProduct().getId());
    }

    @Test
    @DisplayName(value = "LikeProduct Entity build. Member가 null인 경우")
    void buildLikeProductMemberIsNull() {
        Product product = Product.builder().id("testProductId").build();

        assertThrows(
                CustomNotFoundException.class,
                () -> productLikeDomainService.buildLikeProduct(null, product)
        );
    }

    @Test
    @DisplayName(value = "LikeProduct Entity build. Product가 null인 경우")
    void buildLikeProductProductIsNull() {
        Member member = Member.builder().userId("testUser").build();

        assertThrows(
                CustomNotFoundException.class,
                () -> productLikeDomainService.buildLikeProduct(member, null)
        );
    }
}
