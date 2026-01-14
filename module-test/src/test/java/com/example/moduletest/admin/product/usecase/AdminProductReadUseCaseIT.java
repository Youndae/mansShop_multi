package com.example.moduletest.admin.product.usecase;

import com.example.modulecommon.fixture.ClassificationFixture;
import com.example.modulecommon.fixture.ProductFixture;
import com.example.modulecommon.model.dto.response.PagingListDTO;
import com.example.modulecommon.model.entity.*;
import com.example.modulecommon.utils.TestPaginationUtils;
import com.example.moduleproduct.model.dto.admin.product.out.*;
import com.example.moduleproduct.model.dto.page.AdminProductPageDTO;
import com.example.moduleproduct.repository.classification.ClassificationRepository;
import com.example.moduleproduct.repository.product.ProductRepository;
import com.example.moduleproduct.repository.productInfoImage.ProductInfoImageRepository;
import com.example.moduleproduct.repository.productOption.ProductOptionRepository;
import com.example.moduleproduct.repository.productThumbnail.ProductThumbnailRepository;
import com.example.moduleproduct.usecase.admin.product.AdminProductReadUseCase;
import com.example.moduletest.ModuleTestApplication;
import jakarta.persistence.EntityManager;
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

@SpringBootTest(classes = ModuleTestApplication.class)
@EntityScan(basePackages = "com.example.modulecommon")
@EnableJpaRepositories(basePackages = "com.example")
@ComponentScan(basePackages = {"com.example.moduleconfig", "com.example.modulecommon"})
@ActiveProfiles("test")
@Transactional
public class AdminProductReadUseCaseIT {

    @Autowired
    private AdminProductReadUseCase adminProductReadUseCase;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductOptionRepository productOptionRepository;

    @Autowired
    private ProductThumbnailRepository productThumbnailRepository;

    @Autowired
    private ProductInfoImageRepository productInfoImageRepository;

    @Autowired
    private ClassificationRepository classificationRepository;

    @Autowired
    private EntityManager em;

    private List<Product> productList;

    private List<Classification> classificationList;

    @BeforeEach
    void init() {
        classificationList = ClassificationFixture.createClassifications();
        classificationRepository.saveAll(classificationList);

        productList = ProductFixture.createProductFixtureList(30, classificationList.get(0));
        List<ProductOption> optionFixture = productList.stream().flatMap(v -> v.getProductOptions().stream()).toList();
        List<ProductThumbnail> thumbnailFixture = productList.stream().flatMap(v -> v.getProductThumbnails().stream()).toList();
        List<ProductInfoImage> infoImageFixture = productList.stream().flatMap(v -> v.getProductInfoImages().stream()).toList();
        productRepository.saveAll(productList);
        productOptionRepository.saveAll(optionFixture);
        productThumbnailRepository.saveAll(thumbnailFixture);
        productInfoImageRepository.saveAll(infoImageFixture);

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName(value = "상품 목록 조회")
    void getProductList() {
        AdminProductPageDTO pageDTO = new AdminProductPageDTO(1);
        int totalPages = TestPaginationUtils.getTotalPages(productList.size(), pageDTO.amount());
        PagingListDTO<AdminProductListDTO> result = assertDoesNotThrow(() -> adminProductReadUseCase.getProductList(pageDTO));

        assertNotNull(result);
        assertFalse(result.content().isEmpty());
        assertEquals(pageDTO.amount(), result.content().size());
        assertEquals(productList.size(), result.pagingData().getTotalElements());
        assertEquals(totalPages, result.pagingData().getTotalPages());
    }

    @Test
    @DisplayName(value = "상품 목록 조회. 데이터가 없는 경우")
    void getProductListEmpty() {
        productOptionRepository.deleteAll();
        productThumbnailRepository.deleteAll();
        productInfoImageRepository.deleteAll();
        productRepository.deleteAll();
        AdminProductPageDTO pageDTO = new AdminProductPageDTO(1);
        PagingListDTO<AdminProductListDTO> result = assertDoesNotThrow(() -> adminProductReadUseCase.getProductList(pageDTO));

        assertNotNull(result);
        assertTrue(result.content().isEmpty());
        assertEquals(0, result.pagingData().getTotalElements());
        assertEquals(0, result.pagingData().getTotalPages());
    }

    @Test
    @DisplayName(value = "상품 목록 조회. 상품명 검색")
    void getProductListSearchProductName() {
        Product product = productList.get(0);
        AdminProductPageDTO pageDTO = new AdminProductPageDTO(product.getProductName(), 1);
        PagingListDTO<AdminProductListDTO> result = assertDoesNotThrow(() -> adminProductReadUseCase.getProductList(pageDTO));

        assertNotNull(result);
        assertFalse(result.content().isEmpty());
        assertEquals(1, result.content().size());
        assertEquals(1, result.pagingData().getTotalElements());
        assertEquals(1, result.pagingData().getTotalPages());

        AdminProductListDTO resultContent = result.content().get(0);

        assertEquals(product.getId(), resultContent.productId());
        assertEquals(product.getClassification().getId(), resultContent.classification());
        assertEquals(product.getProductName(), resultContent.productName());
    }

    @Test
    @DisplayName(value = "상품 분류 리스트 조회")
    void getClassification() {
        List<String> result = assertDoesNotThrow(() -> adminProductReadUseCase.getProductClassificationIdList());

        assertFalse(result.isEmpty());

        classificationList.forEach(v -> assertTrue(result.contains(v.getId())));
    }

    @Test
    @DisplayName(value = "상품 상세 데이터 조회")
    void getProductDetail() {
        Product product = productList.get(0);
        List<String> thumbnailNameList = product.getProductThumbnails().stream().map(ProductThumbnail::getImageName).toList();
        List<String> infoImageNameList = product.getProductInfoImages().stream().map(ProductInfoImage::getImageName).toList();
        AdminProductDetailDTO result = assertDoesNotThrow(() -> adminProductReadUseCase.getProductDetailData(product.getId()));

        assertNotNull(result);
        assertEquals(product.getId(), result.productId());
        assertEquals(product.getClassification().getId(), result.classification());
        assertEquals(product.getThumbnail(), result.firstThumbnail());
        assertEquals(product.getProductPrice(), result.price());
        assertEquals(product.isOpen(), result.isOpen());
        assertEquals(product.getProductSalesQuantity(), result.sales());
        assertEquals(product.getProductDiscount(), result.discount());
        assertEquals(product.getProductOptions().size(), result.optionList().size());
        thumbnailNameList.forEach(v -> assertTrue(result.thumbnailList().contains(v)));
        infoImageNameList.forEach(v -> assertTrue(result.infoImageList().contains(v)));
    }

    @Test
    @DisplayName(value = "상품 상세 데이터 조회. 데이터가 없는 경우")
    void getProductDetailNotFound() {
        assertThrows(
                IllegalArgumentException.class,
                () -> adminProductReadUseCase.getProductDetailData("emptyId")
        );
    }

    @Test
    @DisplayName(value = "상품 수정 데이터 조회")
    void getPatchProductData() {
        Product product = productList.get(0);
        List<String> thumbnailNameList = product.getProductThumbnails().stream().map(ProductThumbnail::getImageName).toList();
        List<String> infoImageNameList = product.getProductInfoImages().stream().map(ProductInfoImage::getImageName).toList();
        AdminProductPatchDataDTO result = assertDoesNotThrow(() -> adminProductReadUseCase.getPatchProductData(product.getId()));

        assertNotNull(result);
        assertEquals(product.getId(), result.productId());
        assertEquals(product.getClassification().getId(), result.classificationId());
        assertEquals(product.getThumbnail(), result.firstThumbnail());
        assertEquals(product.getProductPrice(), result.price());
        assertEquals(product.isOpen(), result.isOpen());
        assertEquals(product.getProductDiscount(), result.discount());
        assertEquals(product.getProductOptions().size(), result.optionList().size());
        thumbnailNameList.forEach(v -> assertTrue(result.thumbnailList().contains(v)));
        infoImageNameList.forEach(v -> assertTrue(result.infoImageList().contains(v)));
        classificationList.forEach(v -> assertTrue(result.classificationList().contains(v.getId())));
    }

    @Test
    @DisplayName(value = "상품 수정 데이터 조회. 데이터가 없는 경우")
    void getPatchProductDataNotFound() {
        assertThrows(
                IllegalArgumentException.class,
                () -> adminProductReadUseCase.getPatchProductData("emptyId")
        );
    }

    @Test
    @DisplayName(value = "상품 재고 리스트 반환")
    void getProductStock() {
        AdminProductPageDTO pageDTO = new AdminProductPageDTO(1);
        int totalPages = TestPaginationUtils.getTotalPages(productList.size(), pageDTO.amount());
        List<Product> fixture = productList.stream()
                .sorted(
                        Comparator.comparingInt(product ->
                                product.getProductOptions().stream()
                                        .mapToInt(ProductOption::getStock)
                                        .sum()
                        )
                )
                .limit(pageDTO.amount())
                .toList();

        PagingListDTO<AdminProductStockDTO> result = assertDoesNotThrow(() -> adminProductReadUseCase.getProductStockList(pageDTO));

        assertNotNull(result);
        assertFalse(result.content().isEmpty());
        assertEquals(pageDTO.amount(), result.content().size());
        assertEquals(productList.size(), result.pagingData().getTotalElements());
        assertEquals(totalPages, result.pagingData().getTotalPages());
        assertFalse(result.pagingData().isEmpty());

        for(int i = 0; i < fixture.size(); i++) {
            Product product = fixture.get(i);
            AdminProductStockDTO resultDTO = result.content().get(i);

            assertEquals(product.getProductName(), resultDTO.productName());
            assertEquals(product.getProductOptions().stream().mapToInt(ProductOption::getStock).sum(), resultDTO.totalStock());
        }
    }

    @Test
    @DisplayName(value = "상품 재고 리스트 반환. 상품명 기반 검색")
    void getProductStockSearchProductName() {
        Product fixture = productList.get(0);
        AdminProductPageDTO pageDTO = new AdminProductPageDTO(fixture.getProductName(), 1);
        int totalStock = fixture.getProductOptions().stream().mapToInt(ProductOption::getStock).sum();

        PagingListDTO<AdminProductStockDTO> result = assertDoesNotThrow(() -> adminProductReadUseCase.getProductStockList(pageDTO));

        assertNotNull(result);
        assertFalse(result.content().isEmpty());
        assertEquals(1, result.content().size());
        assertEquals(1, result.pagingData().getTotalElements());
        assertEquals(1, result.pagingData().getTotalPages());
        assertFalse(result.pagingData().isEmpty());

        AdminProductStockDTO resultDTO = result.content().get(0);

        assertEquals(fixture.getProductName(), resultDTO.productName());
        assertEquals(totalStock, resultDTO.totalStock());
    }

    @Test
    @DisplayName(value = "상품 재고 리스트 반환. 상품명 기반 검색. 데이터가 없는 경우")
    void getProductStockSearchProductNameEmpty() {
        AdminProductPageDTO pageDTO = new AdminProductPageDTO("noneProductName", 1);

        PagingListDTO<AdminProductStockDTO> result = assertDoesNotThrow(() -> adminProductReadUseCase.getProductStockList(pageDTO));

        assertNotNull(result);
        assertTrue(result.content().isEmpty());
        assertEquals(0, result.pagingData().getTotalElements());
        assertEquals(0, result.pagingData().getTotalPages());
        assertTrue(result.pagingData().isEmpty());
    }

    @Test
    @DisplayName(value = "할인 중인 상품 목록 조회.")
    void getDiscountProduct() {
        List<Product> fixtureList = productList.stream().filter(v -> v.getProductDiscount() > 0).toList();
        AdminProductPageDTO pageDTO = new AdminProductPageDTO(1);
        int totalPages = TestPaginationUtils.getTotalPages(fixtureList.size(), pageDTO.amount());

        PagingListDTO<AdminDiscountResponseDTO> result = assertDoesNotThrow(() -> adminProductReadUseCase.getDiscountProductList(pageDTO));

        assertNotNull(result);
        assertFalse(result.content().isEmpty());
        assertFalse(result.pagingData().isEmpty());
        assertEquals(fixtureList.size(), result.pagingData().getTotalElements());
        assertEquals(totalPages, result.pagingData().getTotalPages());

        result.content().forEach(v -> assertTrue(v.discount() > 0));
    }

    @Test
    @DisplayName(value = "할인 중인 상품 목록 조회. 상품명 기반 검색")
    void getDiscountProductSearchProductName() {
        List<Product> fixtureList = productList.stream().filter(v -> v.getProductDiscount() > 0).toList();
        Product fixture = fixtureList.get(0);
        AdminProductPageDTO pageDTO = new AdminProductPageDTO(fixture.getProductName(), 1);

        PagingListDTO<AdminDiscountResponseDTO> result = assertDoesNotThrow(() -> adminProductReadUseCase.getDiscountProductList(pageDTO));

        assertNotNull(result);
        assertFalse(result.content().isEmpty());
        assertFalse(result.pagingData().isEmpty());
        assertEquals(1, result.content().size());
        assertEquals(1, result.pagingData().getTotalElements());
        assertEquals(1, result.pagingData().getTotalPages());

        AdminDiscountResponseDTO resultDTO = result.content().get(0);
        assertEquals(fixture.getId(), resultDTO.productId());
        assertEquals(fixture.getProductName(), resultDTO.productName());
        assertEquals(fixture.getProductDiscount(), resultDTO.discount());
    }

    @Test
    @DisplayName(value = "할인 중인 상품 목록 조회. 상품명 기반 검색. 데이터가 없는 경우")
    void getDiscountProductSearchProductNameEmpty() {
        AdminProductPageDTO pageDTO = new AdminProductPageDTO("noneProductName", 1);

        PagingListDTO<AdminDiscountResponseDTO> result = assertDoesNotThrow(() -> adminProductReadUseCase.getDiscountProductList(pageDTO));

        assertNotNull(result);
        assertTrue(result.content().isEmpty());
        assertTrue(result.pagingData().isEmpty());
        assertEquals(0, result.pagingData().getTotalElements());
        assertEquals(0, result.pagingData().getTotalPages());
    }

    @Test
    @DisplayName(value = "상품 분류에 해당하는 상품 리스트 조회.")
    void getSelectDiscountProduct() {
        List<AdminDiscountProductDTO> result = assertDoesNotThrow(() -> adminProductReadUseCase.getSelectDiscountProductList(classificationList.get(0).getId()));

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(productList.size(), result.size());
    }

    @Test
    @DisplayName(value = "상품 분류에 해당하는 상품 리스트 조회. 데이터가 없는 경우")
    void getSelectDiscountProductEmpty() {
        List<AdminDiscountProductDTO> result = assertDoesNotThrow(() -> adminProductReadUseCase.getSelectDiscountProductList(classificationList.get(1).getId()));

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
