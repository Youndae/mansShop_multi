package com.example.moduletest.admin.product.usecase;

import com.example.modulecommon.fixture.ClassificationFixture;
import com.example.modulecommon.fixture.ProductFixture;
import com.example.modulecommon.model.entity.*;
import com.example.modulecommon.model.enumuration.Result;
import com.example.modulefile.service.FileService;
import com.example.moduleproduct.model.dto.admin.product.in.AdminDiscountPatchDTO;
import com.example.moduleproduct.model.dto.admin.product.in.AdminProductImageDTO;
import com.example.moduleproduct.model.dto.admin.product.in.AdminProductPatchDTO;
import com.example.moduleproduct.model.dto.admin.product.in.PatchOptionDTO;
import com.example.moduleproduct.repository.classification.ClassificationRepository;
import com.example.moduleproduct.repository.product.ProductRepository;
import com.example.moduleproduct.repository.productInfoImage.ProductInfoImageRepository;
import com.example.moduleproduct.repository.productOption.ProductOptionRepository;
import com.example.moduleproduct.repository.productThumbnail.ProductThumbnailRepository;
import com.example.moduleproduct.usecase.admin.product.AdminProductWriteUseCase;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = ModuleTestApplication.class)
@EntityScan(basePackages = "com.example.modulecommon")
@EnableJpaRepositories(basePackages = "com.example")
@ComponentScan(basePackages = {"com.example.moduleconfig", "com.example.modulecommon"})
@ActiveProfiles("test")
@Transactional
public class AdminProductWriteUseCaseIT {

    @Autowired
    private AdminProductWriteUseCase adminProductWriteUseCase;

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

    @MockitoBean
    private FileService fileService;

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

    private MockMultipartFile createMockMultipartFile(String fieldName, String fileName) {
        return new MockMultipartFile(
                fieldName,
                fileName + ".jpg",
                "image/jpeg",
                fileName.getBytes()
        );
    }

    private List<MockMultipartFile> createMockMultipartFileList(String type, int count) {
        List<MockMultipartFile> result = new ArrayList<>();
        String fieldName = "thumbnail";
        String fileName = "thumb";

        if(type.equals("info")) {
            fieldName = "infoImage";
            fileName = "info";
        }

        for(int i = 0; i < count; i++) {
            fileName = fileName + i;
            result.add(createMockMultipartFile(fieldName, fileName));
        }

        return result;
    }

    @Test
    @DisplayName(value = "상품 등록")
    void postProduct() {
        List<PatchOptionDTO> optionList = IntStream.range(0, 2)
                .mapToObj(v -> new PatchOptionDTO(
                        0L,
                        "postSize" + v,
                        "postColor" + v,
                        100,
                        true
                ))
                .toList();
        AdminProductPatchDTO patchDTO = new AdminProductPatchDTO(
                "postProduct",
                "TOP",
                20000,
                true,
                0,
                optionList
        );
        MockMultipartFile firstThumbnail = createMockMultipartFile("firstThumbnail", "firstThumb");
        List<MultipartFile> thumbnail = new ArrayList<>(createMockMultipartFileList("thumbnail", 3));
        List<MultipartFile> infoImage = new ArrayList<>(createMockMultipartFileList("info", 3));
        AdminProductImageDTO imageDTO = new AdminProductImageDTO(
                firstThumbnail,
                null,
                thumbnail,
                null,
                infoImage,
                null
        );

        try {
            given(fileService.imageInsert(any(MultipartFile.class)))
                    .willReturn("saved-first-thumb.jpg");
        }catch (Exception e) {
            e.printStackTrace();
        }

        String result = assertDoesNotThrow(() -> adminProductWriteUseCase.postProduct(patchDTO, imageDTO));

        assertNotNull(result);

        Product saveProduct = productRepository.findById(result).orElse(null);

        assertNotNull(saveProduct);
        assertEquals(patchDTO.getProductName(), saveProduct.getProductName());
        assertEquals(patchDTO.getClassification(), saveProduct.getClassification().getId());
        assertEquals(patchDTO.getPrice(), saveProduct.getProductPrice());
        assertEquals(patchDTO.getIsOpen(), saveProduct.isOpen());
        assertEquals(patchDTO.getDiscount(), saveProduct.getProductDiscount());
        assertEquals(patchDTO.getOptionList().size(), saveProduct.getProductOptions().size());
        assertEquals(imageDTO.getThumbnail().size(), saveProduct.getProductThumbnails().size());
        assertEquals(imageDTO.getInfoImage().size(), saveProduct.getProductInfoImages().size());
    }

    @Test
    @DisplayName(value = "상품 등록. 대표 썸네일이 없는 경우")
    void postProductFirstThumbnailIsNull() {
        List<PatchOptionDTO> optionList = IntStream.range(0, 2)
                .mapToObj(v -> new PatchOptionDTO(
                        0L,
                        "postSize" + v,
                        "postColor" + v,
                        100,
                        true
                ))
                .toList();
        AdminProductPatchDTO patchDTO = new AdminProductPatchDTO(
                "postProduct",
                "TOP",
                20000,
                true,
                0,
                optionList
        );
        List<MultipartFile> thumbnail = new ArrayList<>(createMockMultipartFileList("thumbnail", 3));
        List<MultipartFile> infoImage = new ArrayList<>(createMockMultipartFileList("info", 3));
        AdminProductImageDTO imageDTO = new AdminProductImageDTO(
                null,
                null,
                thumbnail,
                null,
                infoImage,
                null
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> adminProductWriteUseCase.postProduct(patchDTO, imageDTO)
        );

        try {
            verify(fileService, never()).imageInsert(any(MultipartFile.class));
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName(value = "상품 수정 처리")
    void patchProduct() {
        Product productFixture = productList.get(0);
        List<Long> deleteOptionList = List.of(productFixture.getProductOptions().get(0).getId());
        List<PatchOptionDTO> addOptionList = List.of(new PatchOptionDTO(
                        0L,
                        "newPostSize",
                        "newPostColor",
                        100,
                        true
                )
        );
        AdminProductPatchDTO patchDTO = new AdminProductPatchDTO(
                "postProduct",
                "TOP",
                20000,
                true,
                0,
                addOptionList
        );

        MockMultipartFile firstThumbnail = createMockMultipartFile("firstThumbnail", "firstThumb");
        List<MultipartFile> thumbnail = new ArrayList<>(createMockMultipartFileList("thumbnail", 3));
        List<MultipartFile> infoImage = new ArrayList<>(createMockMultipartFileList("info", 3));
        AdminProductImageDTO imageDTO = new AdminProductImageDTO(
                firstThumbnail,
                "deleteFirstThumbnailName",
                thumbnail,
                List.of("deleteThumbnailName"),
                infoImage,
                List.of("deleteInfoImageName")
        );

        try {
            given(fileService.imageInsert(any(MultipartFile.class)))
                    .willReturn("saved-first-thumb.jpg");

            willDoNothing().given(fileService).deleteImage(anyString());
        }catch (Exception e) {
            e.printStackTrace();
        }

        String result = assertDoesNotThrow(() -> adminProductWriteUseCase.patchProduct(productFixture.getId(), deleteOptionList, patchDTO, imageDTO));

        verify(fileService, times(3)).deleteImage(anyString());

        assertNotNull(result);
        assertEquals(productFixture.getId(), result);
    }

    @Test
    @DisplayName(value = "상품 수정 처리. 추가하는 대표 썸네일은 있으나, 삭제하는 대표 썸네일이 없는 경우")
    void patchProductDeleteFirstThumbnailException() {
        Product productFixture = productList.get(0);
        List<Long> deleteOptionList = List.of(productFixture.getProductOptions().get(0).getId());
        List<PatchOptionDTO> addOptionList = List.of(new PatchOptionDTO(
                        0L,
                        "newPostSize",
                        "newPostColor",
                        100,
                        true
                )
        );
        AdminProductPatchDTO patchDTO = new AdminProductPatchDTO(
                "postProduct",
                "TOP",
                20000,
                true,
                0,
                addOptionList
        );

        MockMultipartFile firstThumbnail = createMockMultipartFile("firstThumbnail", "firstThumb");
        List<MultipartFile> thumbnail = new ArrayList<>(createMockMultipartFileList("thumbnail", 3));
        List<MultipartFile> infoImage = new ArrayList<>(createMockMultipartFileList("info", 3));
        AdminProductImageDTO imageDTO = new AdminProductImageDTO(
                firstThumbnail,
                null,
                thumbnail,
                List.of("deleteThumbnailName"),
                infoImage,
                List.of("deleteInfoImageName")
        );

        try {
            given(fileService.imageInsert(any(MultipartFile.class)))
                    .willReturn("saved-first-thumb.jpg");

            willDoNothing().given(fileService).deleteImage(anyString());
        }catch (Exception e) {
            e.printStackTrace();
        }

        assertThrows(
                IllegalArgumentException.class,
                () -> adminProductWriteUseCase.patchProduct(productFixture.getId(), deleteOptionList, patchDTO, imageDTO)
        );

        verify(fileService, times(6)).deleteImage(anyString());
    }

    @Test
    @DisplayName(value = "상품 수정 처리. 추가하는 대표 썸네일은 없으나, 삭제하는 대표 썸네일이 있는 경우")
    void patchProductFirstThumbnailException() {
        Product productFixture = productList.get(0);
        List<Long> deleteOptionList = List.of(productFixture.getProductOptions().get(0).getId());
        List<PatchOptionDTO> addOptionList = List.of(new PatchOptionDTO(
                        0L,
                        "newPostSize",
                        "newPostColor",
                        100,
                        true
                )
        );
        AdminProductPatchDTO patchDTO = new AdminProductPatchDTO(
                "postProduct",
                "TOP",
                20000,
                true,
                0,
                addOptionList
        );

        List<MultipartFile> thumbnail = new ArrayList<>(createMockMultipartFileList("thumbnail", 3));
        List<MultipartFile> infoImage = new ArrayList<>(createMockMultipartFileList("info", 3));
        AdminProductImageDTO imageDTO = new AdminProductImageDTO(
                null,
                "deleteFirstThumbnailName",
                thumbnail,
                List.of("deleteThumbnailName"),
                infoImage,
                List.of("deleteInfoImageName")
        );

        try {
            given(fileService.imageInsert(any(MultipartFile.class)))
                    .willReturn("saved-first-thumb.jpg");

            willDoNothing().given(fileService).deleteImage(anyString());
        }catch (Exception e) {
            e.printStackTrace();
        }

        assertThrows(
                IllegalArgumentException.class,
                () -> adminProductWriteUseCase.patchProduct(productFixture.getId(), deleteOptionList, patchDTO, imageDTO)
        );

        verify(fileService, times(6)).deleteImage(anyString());
    }

    @Test
    @DisplayName(value = "상품 수정 처리. 상품 데이터가 없는 경우(아이디가 잘못 된 경우)")
    void patchProductNotFound() {
        Product productFixture = productList.get(0);
        List<Long> deleteOptionList = List.of(productFixture.getProductOptions().get(0).getId());
        List<PatchOptionDTO> addOptionList = List.of(new PatchOptionDTO(
                        0L,
                        "newPostSize",
                        "newPostColor",
                        100,
                        true
                )
        );
        AdminProductPatchDTO patchDTO = new AdminProductPatchDTO(
                "postProduct",
                "TOP",
                20000,
                true,
                0,
                addOptionList
        );

        MockMultipartFile firstThumbnail = createMockMultipartFile("firstThumbnail", "firstThumb");
        List<MultipartFile> thumbnail = new ArrayList<>(createMockMultipartFileList("thumbnail", 3));
        List<MultipartFile> infoImage = new ArrayList<>(createMockMultipartFileList("info", 3));
        AdminProductImageDTO imageDTO = new AdminProductImageDTO(
                firstThumbnail,
                "deleteFirstThumbnailName",
                thumbnail,
                List.of("deleteThumbnailName"),
                infoImage,
                List.of("deleteInfoImageName")
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> adminProductWriteUseCase.patchProduct("noneProductId", deleteOptionList, patchDTO, imageDTO)
        );

        try {
            verify(fileService, never()).deleteImage(anyString());
            verify(fileService, never()).imageInsert(any(MultipartFile.class));
        }catch (Exception e) {
            e.printStackTrace();
            fail("verify fail");
        }
    }

    @Test
    @DisplayName(value = "상품 수정 처리. 상품 분류 데이터가 없는 경우 (상품 분류 아이디가 잘못 된 경우)")
    void patchProductClassificationNotFound() {
        Product productFixture = productList.get(0);
        List<Long> deleteOptionList = List.of(productFixture.getProductOptions().get(0).getId());
        List<PatchOptionDTO> addOptionList = List.of(new PatchOptionDTO(
                        0L,
                        "newPostSize",
                        "newPostColor",
                        100,
                        true
                )
        );
        AdminProductPatchDTO patchDTO = new AdminProductPatchDTO(
                "postProduct",
                "noneClassification",
                20000,
                true,
                0,
                addOptionList
        );

        MockMultipartFile firstThumbnail = createMockMultipartFile("firstThumbnail", "firstThumb");
        List<MultipartFile> thumbnail = new ArrayList<>(createMockMultipartFileList("thumbnail", 3));
        List<MultipartFile> infoImage = new ArrayList<>(createMockMultipartFileList("info", 3));
        AdminProductImageDTO imageDTO = new AdminProductImageDTO(
                firstThumbnail,
                "deleteFirstThumbnailName",
                thumbnail,
                List.of("deleteThumbnailName"),
                infoImage,
                List.of("deleteInfoImageName")
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> adminProductWriteUseCase.patchProduct(productFixture.getId(), deleteOptionList, patchDTO, imageDTO)
        );

        try {
            verify(fileService, never()).deleteImage(anyString());
            verify(fileService, never()).imageInsert(any(MultipartFile.class));
        }catch (Exception e) {
            e.printStackTrace();
            fail("verify fail");
        }
    }

    @Test
    @DisplayName(value = "상품 할인율 수정. 단일 수정")
    void patchDiscountOneProduct() {
        Product fixture = productList.get(0);
        List<String> productIdList = List.of(fixture.getId());
        int discount = fixture.getProductDiscount() + 10;

        AdminDiscountPatchDTO patchDTO = new AdminDiscountPatchDTO(productIdList, discount);

        String result = assertDoesNotThrow(() -> adminProductWriteUseCase.patchDiscountProduct(patchDTO));

        em.flush();
        em.clear();

        assertNotNull(result);
        assertEquals(Result.OK.getResultKey(), result);

        Product resultEntity = productRepository.findById(fixture.getId()).orElseThrow(IllegalArgumentException::new);

        assertEquals(discount, resultEntity.getProductDiscount());
    }

    @Test
    @DisplayName(value = "상품 할인율 수정. 다중 수정")
    void patchDiscountProduct() {
        List<Product> fixtureList = List.of(productList.get(0), productList.get(1));
        List<String> productIdList = fixtureList.stream().map(Product::getId).toList();
        int discount = 60;
        AdminDiscountPatchDTO patchDTO = new AdminDiscountPatchDTO(productIdList, discount);

        String result = assertDoesNotThrow(() -> adminProductWriteUseCase.patchDiscountProduct(patchDTO));

        assertNotNull(result);
        assertEquals(Result.OK.getResultKey(), result);

        em.flush();
        em.clear();

        Product resultEntity1 = productRepository.findById(fixtureList.get(0).getId()).orElseThrow(IllegalArgumentException::new);
        Product resultEntity2 = productRepository.findById(fixtureList.get(1).getId()).orElseThrow(IllegalArgumentException::new);

        assertEquals(discount, resultEntity1.getProductDiscount());
        assertEquals(discount, resultEntity2.getProductDiscount());
    }
}
