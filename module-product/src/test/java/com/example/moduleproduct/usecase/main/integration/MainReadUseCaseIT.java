package com.example.moduleproduct.usecase.main.integration;

import com.example.modulecommon.fixture.ClassificationFixture;
import com.example.modulecommon.fixture.ProductFixture;
import com.example.modulecommon.model.dto.response.PagingListDTO;
import com.example.modulecommon.model.entity.Classification;
import com.example.modulecommon.model.entity.Product;
import com.example.modulecommon.model.entity.ProductOption;
import com.example.modulecommon.utils.PaginationUtils;
import com.example.modulecommon.utils.ProductDiscountUtils;
import com.example.moduleproduct.ModuleProductApplication;
import com.example.moduleproduct.model.dto.main.out.MainListResponseDTO;
import com.example.moduleproduct.model.dto.page.MainPageDTO;
import com.example.moduleproduct.repository.classification.ClassificationRepository;
import com.example.moduleproduct.repository.product.ProductRepository;
import com.example.moduleproduct.repository.productOption.ProductOptionRepository;
import com.example.moduleproduct.usecase.main.MainReadUseCase;
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

import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(classes = ModuleProductApplication.class)
@EntityScan(basePackages = "com.example.modulecommon")
@EnableJpaRepositories(basePackages = "com.example")
@ComponentScan(basePackages = {"com.example.moduleconfig", "com.example.modulecommon"})
@ActiveProfiles("test")
@Transactional
public class MainReadUseCaseIT {

    @Autowired
    private MainReadUseCase mainReadUseCase;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductOptionRepository productOptionRepository;

    @Autowired
    private ClassificationRepository classificationRepository;

    private List<Product> productList;

    private List<Classification> classificationList;

    @BeforeEach
    void init() {
        classificationList = ClassificationFixture.createClassifications();
        classificationRepository.saveAll(classificationList);

        productList = ProductFixture.createProductFixtureList(50, classificationList.get(0));
        List<ProductOption> optionList = productList.stream()
                .flatMap(v -> v.getProductOptions().stream())
                .toList();
        productRepository.saveAll(productList);
        productOptionRepository.saveAll(optionList);
    }

    @Test
    @DisplayName(value = "BEST 상품 리스트 조회")
    void getBestList() {
        MainPageDTO pageDTO = new MainPageDTO("BEST");
        List<Product> fixtureList = productList.stream()
                .sorted(
                        Comparator.comparingLong(Product::getProductSalesQuantity)
                                .reversed()
                )
                .limit(12)
                .toList();

        List<MainListResponseDTO> result = assertDoesNotThrow(() -> mainReadUseCase.getBestProductList(pageDTO));

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(fixtureList.size(), result.size());

        for(int i = 0; i < result.size(); i++) {
            Product fixture = fixtureList.get(i);
            MainListResponseDTO resultDTO = result.get(i);
            int discountPrice = (int) (fixture.getProductPrice() * (1 - ((double) fixture.getProductDiscount() / 100)));
            int stock = fixture.getProductOptions().stream().mapToInt(ProductOption::getStock).sum();

            assertEquals(fixture.getId(), resultDTO.productId());
            assertEquals(fixture.getProductName(), resultDTO.productName());
            assertEquals(fixture.getThumbnail(), resultDTO.thumbnail());
            assertEquals(fixture.getProductPrice(), resultDTO.originPrice());
            assertEquals(fixture.getProductDiscount(), resultDTO.discount());
            assertEquals(discountPrice, resultDTO.discountPrice());
            assertEquals(stock == 0, resultDTO.isSoldOut());
        }
    }

    @Test
    @DisplayName(value = "NEW 상품 리스트 조회")
    void getNewList() {
        MainPageDTO pageDTO = new MainPageDTO("NEW");

        List<MainListResponseDTO> result = assertDoesNotThrow(() -> mainReadUseCase.getNewProductList(pageDTO));

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(12, result.size());
    }

    @Test
    @DisplayName(value = "BEST 상품 리스트 조회. 데이터가 없는 경우")
    void getBestListEmpty() {
        productRepository.deleteAll();
        MainPageDTO pageDTO = new MainPageDTO("BEST");

        List<MainListResponseDTO> result = assertDoesNotThrow(() -> mainReadUseCase.getBestProductList(pageDTO));

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName(value = "상품 분류 리스트 조회")
    void getClassificationList() {
        String classificationId = classificationList.get(0).getId();
        MainPageDTO pageDTO = new MainPageDTO(classificationId);
        List<Product> fixtureList = productList.stream()
                .filter(v -> v.getClassification().getId().equals(classificationId))
                .toList();
        int contentSize = Math.min(fixtureList.size(), pageDTO.amount());
        int totalPages = PaginationUtils.getTotalPages(fixtureList.size(), pageDTO.amount());

        PagingListDTO<MainListResponseDTO> result = assertDoesNotThrow(() -> mainReadUseCase.getClassificationOrSearchList(pageDTO));

        assertNotNull(result);
        assertFalse(result.content().isEmpty());
        assertEquals(contentSize, result.content().size());
        assertEquals(fixtureList.size(), result.pagingData().getTotalElements());
        assertEquals(totalPages, result.pagingData().getTotalPages());
        assertFalse(result.pagingData().isEmpty());
    }

    @Test
    @DisplayName(value = "상품 분류 리스트 조회. 데이터가 없는 경우")
    void getClassificationListEmpty() {
        String classificationId = "noneId";
        MainPageDTO pageDTO = new MainPageDTO(classificationId);

        PagingListDTO<MainListResponseDTO> result = assertDoesNotThrow(() -> mainReadUseCase.getClassificationOrSearchList(pageDTO));

        assertNotNull(result);
        assertTrue(result.content().isEmpty());
        assertEquals(0, result.pagingData().getTotalElements());
        assertEquals(0, result.pagingData().getTotalPages());
        assertTrue(result.pagingData().isEmpty());
    }

    @Test
    @DisplayName(value = "상품 검색")
    void getSearchList() {
        Product product = productList.get(0);
        int discountPrice = ProductDiscountUtils.calcDiscountPrice(product.getProductPrice(), product.getProductDiscount());
        boolean isSoldOut = product.getProductOptions().stream().mapToInt(ProductOption::getStock).sum() == 0;
        String keyword = product.getProductName();
        MainPageDTO pageDTO = new MainPageDTO(1, keyword, null);

        PagingListDTO<MainListResponseDTO> result = assertDoesNotThrow(() -> mainReadUseCase.getClassificationOrSearchList(pageDTO));

        assertNotNull(result);
        assertFalse(result.content().isEmpty());
        assertEquals(1, result.pagingData().getTotalElements());
        assertEquals(1, result.pagingData().getTotalPages());
        assertFalse(result.pagingData().isEmpty());
        assertEquals(1, result.content().size());

        MainListResponseDTO resultDTO = result.content().get(0);

        assertEquals(product.getId(), resultDTO.productId());
        assertEquals(product.getProductName(), resultDTO.productName());
        assertEquals(product.getThumbnail(), resultDTO.thumbnail());
        assertEquals(product.getProductPrice(), resultDTO.originPrice());
        assertEquals(product.getProductDiscount(), resultDTO.discount());
        assertEquals(discountPrice, resultDTO.discountPrice());
        assertEquals(isSoldOut, resultDTO.isSoldOut());
    }

    @Test
    @DisplayName(value = "상품 검색. 조회 결과가 없는 경우")
    void getSearchListEmpty() {
        MainPageDTO pageDTO = new MainPageDTO(1, "noneProduct", null);

        PagingListDTO<MainListResponseDTO> result = assertDoesNotThrow(() -> mainReadUseCase.getClassificationOrSearchList(pageDTO));

        assertNotNull(result);
        assertTrue(result.content().isEmpty());
        assertEquals(0, result.pagingData().getTotalElements());
        assertEquals(0, result.pagingData().getTotalPages());
        assertTrue(result.pagingData().isEmpty());
    }
}
