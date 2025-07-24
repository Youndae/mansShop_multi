package com.example.modulecart.service.integration;

import com.example.modulecart.ModuleCartApplication;
import com.example.modulecart.model.dto.in.AddCartDTO;
import com.example.modulecart.repository.CartRepository;
import com.example.modulecart.service.CartDomainService;
import com.example.modulecommon.fixture.ClassificationFixture;
import com.example.modulecommon.fixture.ProductFixture;
import com.example.modulecommon.model.entity.*;
import com.example.moduleproduct.repository.classification.ClassificationRepository;
import com.example.moduleproduct.repository.product.ProductRepository;
import com.example.moduleproduct.repository.productOption.ProductOptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = ModuleCartApplication.class)
@EntityScan(basePackages = "com.example.modulecommon")
@EnableJpaRepositories(basePackages = "com.example")
@ComponentScan(basePackages = {"com.example.moduleconfig", "com.example.modulecommon"})
@ActiveProfiles("test")
@Transactional
public class CartDomainServiceIT {

    @Autowired
    private CartDomainService cartDomainService;

    @Autowired
    private ClassificationRepository classificationRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductOptionRepository productOptionRepository;

    @Autowired
    private CartRepository cartRepository;

    private List<ProductOption> productOptionList;


    @BeforeEach
    void init() {
        List<Classification> classificationList = ClassificationFixture.createClassifications();
        classificationRepository.saveAll(classificationList);

        List<Product> productList = ProductFixture.createProductFixtureList(1, classificationList.get(0));
        productOptionList = productList.stream().flatMap(v -> v.getProductOptions().stream()).toList();
        productRepository.saveAll(productList);
        productOptionRepository.saveAll(productOptionList);
    }

    @Test
    @DisplayName(value = "장바구니 추가 시 cartDetail 추가 혹은 이미 존재한다면 수량 증가")
    void mapCartAndCartDetails() {
        AddCartDTO addCartDTO1 = new AddCartDTO(productOptionList.get(0).getId(), 2);
        AddCartDTO addCartDTO2 = new AddCartDTO(productOptionList.get(1).getId(), 2);
        Cart cart = Cart.builder().id(1L).build();
        CartDetail savedCartDetail = CartDetail.builder().id(1L).productOption(productOptionList.get(0)).cartCount(5).build();
        List<AddCartDTO> addList = List.of(addCartDTO1, addCartDTO2);
        List<CartDetail> savedDetails = new ArrayList<>(List.of(savedCartDetail));

        assertDoesNotThrow(() -> cartDomainService.mapCartAndCartDetails(addList, cart, savedDetails));

        List<CartDetail> resultDetails = cart.getCartDetailList();
        assertNotNull(resultDetails);
        assertFalse(resultDetails.isEmpty());
        assertEquals(2, resultDetails.size());
        assertEquals(7, resultDetails.get(0).getCartCount());
        assertEquals(2, resultDetails.get(1).getCartCount());
    }

    @Test
    @DisplayName(value = "장바구니 추가 시 cartDetail 추가 혹은 이미 존재한다면 수량 증가. savedDetails가 emptyList인 경우")
    void mapCartAndCartDetailsEmptyList() {
        AddCartDTO addCartDTO1 = new AddCartDTO(productOptionList.get(0).getId(), 2);
        AddCartDTO addCartDTO2 = new AddCartDTO(productOptionList.get(1).getId(), 2);
        Cart cart = Cart.builder().id(1L).build();
        List<AddCartDTO> addList = List.of(addCartDTO1, addCartDTO2);
        List<CartDetail> savedDetails = Collections.emptyList();

        assertDoesNotThrow(() -> cartDomainService.mapCartAndCartDetails(addList, cart, savedDetails));

        List<CartDetail> resultDetails = cart.getCartDetailList();
        assertNotNull(resultDetails);
        assertFalse(resultDetails.isEmpty());
        assertEquals(2, resultDetails.size());
        resultDetails.forEach(v -> assertEquals(2, v.getCartCount()));
    }

    @Test
    @DisplayName(value = "장바구니 추가 시 cartDetail 추가 혹은 이미 존재한다면 수량 증가. productOption 조회 결과가 null인 경우")
    void mapCartAndCartDetailsOptionNotFound() {
        AddCartDTO addCartDTO1 = new AddCartDTO(0L, 2);
        AddCartDTO addCartDTO2 = new AddCartDTO(productOptionList.get(1).getId(), 2);
        Cart cart = Cart.builder().id(1L).build();
        ProductOption productOption1 = ProductOption.builder().id(1L).build();
        CartDetail savedCartDetail = CartDetail.builder().id(1L).productOption(productOption1).cartCount(5).build();
        List<AddCartDTO> addList = List.of(addCartDTO1, addCartDTO2);
        List<CartDetail> savedDetails = new ArrayList<>(List.of(savedCartDetail));

        assertThrows(
                IllegalArgumentException.class,
                () -> cartDomainService.mapCartAndCartDetails(addList, cart, savedDetails)
        );
    }
}
