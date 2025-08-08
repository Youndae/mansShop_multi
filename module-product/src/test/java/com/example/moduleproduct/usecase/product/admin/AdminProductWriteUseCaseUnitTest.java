package com.example.moduleproduct.usecase.product.admin;

import com.example.modulecommon.fixture.ProductFixture;
import com.example.modulecommon.model.entity.Classification;
import com.example.modulecommon.model.entity.Product;
import com.example.modulecommon.model.enumuration.Result;
import com.example.moduleproduct.model.dto.admin.product.in.AdminDiscountPatchDTO;
import com.example.moduleproduct.model.dto.admin.product.in.AdminProductImageDTO;
import com.example.moduleproduct.model.dto.admin.product.in.AdminProductPatchDTO;
import com.example.moduleproduct.model.dto.admin.product.in.PatchOptionDTO;
import com.example.moduleproduct.service.product.ProductDataService;
import com.example.moduleproduct.service.product.ProductDomainService;
import com.example.moduleproduct.usecase.admin.product.AdminProductWriteUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminProductWriteUseCaseUnitTest {

    @InjectMocks
    private AdminProductWriteUseCase adminProductWriteUseCase;

    @Mock
    private ProductDataService productDataService;

    @Mock
    private ProductDomainService productDomainService;

    private MockMultipartFile createMockMultipartFile(String imageName) {
        return new MockMultipartFile("fieldName", imageName, "image/jpeg", new byte[]{1, 2, 3, 4, 5});
    }

    @Test
    @DisplayName(value = "상품 등록.")
    void postProduct() throws Exception {
        Product product = ProductFixture.createDefaultProductByOUTER(1).get(0);
        MockMultipartFile firstThumb = createMockMultipartFile(product.getThumbnail());
        List<MockMultipartFile> thumbnailMock = product.getProductThumbnails()
                .stream()
                .map(v -> createMockMultipartFile(v.getImageName()))
                .toList();
        List<MultipartFile> thumbnails = new ArrayList<>(thumbnailMock);
        List<MockMultipartFile> infoImagesMock = product.getProductInfoImages()
                .stream()
                .map(v -> createMockMultipartFile(v.getImageName()))
                .toList();
        List<MultipartFile> infoImages = new ArrayList<>(infoImagesMock);
        List<PatchOptionDTO> optionDTOList = product.getProductOptions()
                .stream()
                .map(v -> new PatchOptionDTO(0L, v.getSize(), v.getColor(), v.getStock(), v.isOpen()))
                .toList();
        AdminProductPatchDTO postDTO = new AdminProductPatchDTO(
                product.getProductName(),
                product.getClassification().getId(),
                product.getProductPrice(),
                product.isOpen(),
                product.getProductDiscount(),
                optionDTOList
        );
        AdminProductImageDTO imageDTO = new AdminProductImageDTO(
                firstThumb,
                null,
                thumbnails,
                null,
                infoImages,
                null
        );
        List<String> saveImages = new ArrayList<>(List.of("thumbnailName", "infoImageName"));

        when(productDomainService.setProductFirstThumbnail(any(Product.class), any(MockMultipartFile.class))).thenReturn("firstThumbnailName");
        when(productDomainService.saveProductImage(any(Product.class), any(AdminProductImageDTO.class))).thenReturn(saveImages);
        when(productDataService.saveProductAndReturnId(any(Product.class))).thenReturn(product.getId());
        doNothing().when(productDataService).saveProductOptions(anyList());
        doNothing().when(productDataService).saveProductThumbnails(anyList());
        doNothing().when(productDataService).saveProductInfoImages(anyList());

        String result = assertDoesNotThrow(() -> adminProductWriteUseCase.postProduct(postDTO, imageDTO));

        verify(productDomainService, never()).deleteImages(anyList());

        assertNotNull(result);
        assertEquals(product.getId(), result);
    }

    @Test
    @DisplayName(value = "상품 등록. 대표 썸네일이 없는 경우")
    void postProductFirstThumbnailIsNull() throws Exception {
        Product product = ProductFixture.createDefaultProductByOUTER(1).get(0);
        List<MockMultipartFile> thumbnailMock = product.getProductThumbnails()
                .stream()
                .map(v -> createMockMultipartFile(v.getImageName()))
                .toList();
        List<MultipartFile> thumbnails = new ArrayList<>(thumbnailMock);
        List<MockMultipartFile> infoImagesMock = product.getProductInfoImages()
                .stream()
                .map(v -> createMockMultipartFile(v.getImageName()))
                .toList();
        List<MultipartFile> infoImages = new ArrayList<>(infoImagesMock);
        List<PatchOptionDTO> optionDTOList = product.getProductOptions()
                .stream()
                .map(v -> new PatchOptionDTO(0L, v.getSize(), v.getColor(), v.getStock(), v.isOpen()))
                .toList();
        AdminProductPatchDTO postDTO = new AdminProductPatchDTO(
                product.getProductName(),
                product.getClassification().getId(),
                product.getProductPrice(),
                product.isOpen(),
                product.getProductDiscount(),
                optionDTOList
        );
        AdminProductImageDTO imageDTO = new AdminProductImageDTO(
                null,
                null,
                thumbnails,
                null,
                infoImages,
                null
        );

        when(productDomainService.setProductFirstThumbnail(any(Product.class), any(MockMultipartFile.class))).thenReturn(null);
        doNothing().when(productDomainService).deleteImages(anyList());

        assertThrows(IllegalArgumentException.class, () -> adminProductWriteUseCase.postProduct(postDTO, imageDTO));

        verify(productDomainService, never()).saveProductImage(any(Product.class), any(AdminProductImageDTO.class));
        verify(productDataService, never()).saveProductAndReturnId(any(Product.class));
        verify(productDataService, never()).saveProductOptions(anyList());
        verify(productDataService, never()).saveProductThumbnails(anyList());
        verify(productDataService, never()).saveProductInfoImages(anyList());
    }

    @Test
    @DisplayName(value = "상품 등록. DB 장애로 인한 오류 발생")
    void postProductDBError() throws Exception {
        Product product = ProductFixture.createDefaultProductByOUTER(1).get(0);
        MockMultipartFile firstThumb = createMockMultipartFile(product.getThumbnail());
        List<MockMultipartFile> thumbnailMock = product.getProductThumbnails()
                .stream()
                .map(v -> createMockMultipartFile(v.getImageName()))
                .toList();
        List<MultipartFile> thumbnails = new ArrayList<>(thumbnailMock);
        List<MockMultipartFile> infoImagesMock = product.getProductInfoImages()
                .stream()
                .map(v -> createMockMultipartFile(v.getImageName()))
                .toList();
        List<MultipartFile> infoImages = new ArrayList<>(infoImagesMock);
        List<PatchOptionDTO> optionDTOList = product.getProductOptions()
                .stream()
                .map(v -> new PatchOptionDTO(0L, v.getSize(), v.getColor(), v.getStock(), v.isOpen()))
                .toList();
        AdminProductPatchDTO postDTO = new AdminProductPatchDTO(
                product.getProductName(),
                product.getClassification().getId(),
                product.getProductPrice(),
                product.isOpen(),
                product.getProductDiscount(),
                optionDTOList
        );
        AdminProductImageDTO imageDTO = new AdminProductImageDTO(
                firstThumb,
                null,
                thumbnails,
                null,
                infoImages,
                null
        );
        List<String> saveImages = new ArrayList<>(List.of("thumbnailName", "infoImageName"));

        when(productDomainService.setProductFirstThumbnail(any(Product.class), any(MockMultipartFile.class))).thenReturn("firstThumbnailName");
        when(productDomainService.saveProductImage(any(Product.class), any(AdminProductImageDTO.class))).thenReturn(saveImages);
        when(productDataService.saveProductAndReturnId(any(Product.class))).thenThrow(new RuntimeException());
        doNothing().when(productDomainService).deleteImages(anyList());


        assertThrows(IllegalArgumentException.class, () -> adminProductWriteUseCase.postProduct(postDTO, imageDTO));

        verify(productDataService, never()).saveProductOptions(anyList());
        verify(productDataService, never()).saveProductThumbnails(anyList());
        verify(productDataService, never()).saveProductInfoImages(anyList());
    }

    @Test
    @DisplayName(value = "상품 수정. 수정할 파일이 존재하지 않는 경우")
    void patchProductEmptyPatchFile() throws Exception {
        Product product = ProductFixture.createDefaultProductByOUTER(1).get(0);
        List<PatchOptionDTO> optionDTOList = product.getProductOptions()
                .stream()
                .map(v -> new PatchOptionDTO(1L, v.getSize(), v.getColor(), v.getStock(), v.isOpen()))
                .toList();
        AdminProductPatchDTO patchDTO = new AdminProductPatchDTO(
                product.getProductName(),
                product.getClassification().getId(),
                product.getProductPrice(),
                product.isOpen(),
                product.getProductDiscount(),
                optionDTOList
        );
        AdminProductImageDTO imageDTO = new AdminProductImageDTO(
                null,
                null,
                null,
                null,
                null,
                null
        );
        List<Long> deleteOptionList = new ArrayList<>(List.of(1L, 2L, 3L));

        when(productDataService.getProductByIdOrElseIllegal(any())).thenReturn(product);
        doNothing().when(productDomainService).setPatchProductOptionData(any(Product.class), any(AdminProductPatchDTO.class));
        when(productDomainService.saveProductImage(any(Product.class), any(AdminProductImageDTO.class))).thenReturn(Collections.emptyList());
        when(productDomainService.setProductFirstThumbnail(any(Product.class), any())).thenReturn(null);
        doNothing().when(productDataService).saveProductOptions(anyList());
        doNothing().when(productDataService).saveProductThumbnails(anyList());
        doNothing().when(productDataService).saveProductInfoImages(anyList());
        when(productDataService.saveProductAndReturnId(any(Product.class))).thenReturn(product.getId());
        doNothing().when(productDataService).deleteAllProductOptionByIds(anyList());

        String result = assertDoesNotThrow(() -> adminProductWriteUseCase.patchProduct(product.getId(), deleteOptionList, patchDTO, imageDTO));

        verify(productDataService, never()).getClassificationByIdOrElseIllegal(any());
        verify(productDomainService, never()).deleteImages(anyList());
        verify(productDomainService, never()).deleteImage(any());
        verify(productDataService, never()).deleteAllProductThumbnailByImageNames(anyList());

        assertNotNull(result);
        assertEquals(product.getId(), result);
    }

    @Test
    @DisplayName(value = "상품 수정. 수정할 파일이 존재하는 경우")
    void patchProduct() throws Exception {
        Product product = ProductFixture.createDefaultProductByOUTER(1).get(0);
        MockMultipartFile firstThumb = createMockMultipartFile(product.getThumbnail());
        List<MockMultipartFile> thumbnailMock = product.getProductThumbnails()
                .stream()
                .map(v -> createMockMultipartFile(v.getImageName()))
                .toList();
        List<MultipartFile> thumbnails = new ArrayList<>(thumbnailMock);
        List<MockMultipartFile> infoImagesMock = product.getProductInfoImages()
                .stream()
                .map(v -> createMockMultipartFile(v.getImageName()))
                .toList();
        List<MultipartFile> infoImages = new ArrayList<>(infoImagesMock);
        List<PatchOptionDTO> optionDTOList = product.getProductOptions()
                .stream()
                .map(v -> new PatchOptionDTO(1L, v.getSize(), v.getColor(), v.getStock(), v.isOpen()))
                .toList();
        AdminProductPatchDTO patchDTO = new AdminProductPatchDTO(
                product.getProductName(),
                product.getClassification().getId(),
                product.getProductPrice(),
                product.isOpen(),
                product.getProductDiscount(),
                optionDTOList
        );
        AdminProductImageDTO imageDTO = new AdminProductImageDTO(
                firstThumb,
                "deleteThumbnail",
                thumbnails,
                List.of("deleteThumb1", "deleteThumb2", "deleteThumb3"),
                infoImages,
                List.of("deleteInfo1", "deleteInfo2", "deleteInfo3")
        );
        List<Long> deleteOptionList = new ArrayList<>(List.of(1L, 2L, 3L));
        List<String> saveImages = new ArrayList<>(imageDTO.getDeleteThumbnail());
        saveImages.addAll(imageDTO.getDeleteInfoImage());

        when(productDataService.getProductByIdOrElseIllegal(any())).thenReturn(product);
        doNothing().when(productDomainService).setPatchProductOptionData(any(Product.class), any(AdminProductPatchDTO.class));
        when(productDomainService.saveProductImage(any(Product.class), any(AdminProductImageDTO.class))).thenReturn(saveImages);
        when(productDomainService.setProductFirstThumbnail(any(Product.class), any())).thenReturn("newFirstThumb");
        doNothing().when(productDataService).saveProductOptions(anyList());
        doNothing().when(productDataService).saveProductThumbnails(anyList());
        doNothing().when(productDataService).saveProductInfoImages(anyList());
        when(productDataService.saveProductAndReturnId(any(Product.class))).thenReturn(product.getId());
        doNothing().when(productDataService).deleteAllProductOptionByIds(anyList());
        doNothing().when(productDomainService).deleteImage(any());
        doNothing().when(productDataService).deleteAllProductThumbnailByImageNames(anyList());
        doNothing().when(productDomainService).deleteImages(anyList());

        String result = assertDoesNotThrow(() -> adminProductWriteUseCase.patchProduct(product.getId(), deleteOptionList, patchDTO, imageDTO));

        verify(productDataService, never()).getClassificationByIdOrElseIllegal(any());

        assertNotNull(result);
        assertEquals(product.getId(), result);
    }

    @Test
    @DisplayName(value = "상품 수정. 상품 분류를 수정하는 경우")
    void patchProductClassificationPatch() throws Exception {
        Product product = ProductFixture.createDefaultProductByOUTER(1).get(0);
        List<PatchOptionDTO> optionDTOList = product.getProductOptions()
                .stream()
                .map(v -> new PatchOptionDTO(1L, v.getSize(), v.getColor(), v.getStock(), v.isOpen()))
                .toList();
        Classification newClassification = Classification.builder().id("TOP").classificationStep(2).build();
        AdminProductPatchDTO patchDTO = new AdminProductPatchDTO(
                product.getProductName(),
                newClassification.getId(),
                product.getProductPrice(),
                product.isOpen(),
                product.getProductDiscount(),
                optionDTOList
        );
        AdminProductImageDTO imageDTO = new AdminProductImageDTO(
                null,
                null,
                null,
                null,
                null,
                null
        );
        List<Long> deleteOptionList = new ArrayList<>(List.of(1L, 2L, 3L));

        when(productDataService.getProductByIdOrElseIllegal(any())).thenReturn(product);
        when(productDataService.getClassificationByIdOrElseIllegal(any())).thenReturn(newClassification);
        doNothing().when(productDomainService).setPatchProductOptionData(any(Product.class), any(AdminProductPatchDTO.class));
        when(productDomainService.saveProductImage(any(Product.class), any(AdminProductImageDTO.class))).thenReturn(Collections.emptyList());
        when(productDomainService.setProductFirstThumbnail(any(Product.class), any())).thenReturn(null);
        doNothing().when(productDataService).saveProductOptions(anyList());
        doNothing().when(productDataService).saveProductThumbnails(anyList());
        doNothing().when(productDataService).saveProductInfoImages(anyList());
        when(productDataService.saveProductAndReturnId(any(Product.class))).thenReturn(product.getId());
        doNothing().when(productDataService).deleteAllProductOptionByIds(anyList());

        String result = assertDoesNotThrow(() -> adminProductWriteUseCase.patchProduct(product.getId(), deleteOptionList, patchDTO, imageDTO));

        verify(productDomainService, never()).deleteImages(anyList());
        verify(productDomainService, never()).deleteImage(any());
        verify(productDataService, never()).deleteAllProductThumbnailByImageNames(anyList());

        assertNotNull(result);
        assertEquals(product.getId(), result);
    }

    @Test
    @DisplayName(value = "상품 수정. 상품 아이디가 잘못된 경우")
    void patchProductWrongProductId() throws Exception {
        Product product = ProductFixture.createDefaultProductByOUTER(1).get(0);
        List<PatchOptionDTO> optionDTOList = product.getProductOptions()
                .stream()
                .map(v -> new PatchOptionDTO(1L, v.getSize(), v.getColor(), v.getStock(), v.isOpen()))
                .toList();
        AdminProductPatchDTO patchDTO = new AdminProductPatchDTO(
                product.getProductName(),
                product.getClassification().getId(),
                product.getProductPrice(),
                product.isOpen(),
                product.getProductDiscount(),
                optionDTOList
        );
        AdminProductImageDTO imageDTO = new AdminProductImageDTO(
                null,
                null,
                null,
                null,
                null,
                null
        );
        List<Long> deleteOptionList = new ArrayList<>(List.of(1L, 2L, 3L));

        when(productDataService.getProductByIdOrElseIllegal(any())).thenThrow(IllegalArgumentException.class);

        assertThrows(
                IllegalArgumentException.class,
                () -> adminProductWriteUseCase.patchProduct(product.getId(), deleteOptionList, patchDTO, imageDTO)
        );

        verify(productDomainService, never()).setPatchProductOptionData(any(Product.class), any(AdminProductPatchDTO.class));
        verify(productDomainService, never()).saveProductImage(any(Product.class), any(AdminProductImageDTO.class));
        verify(productDomainService, never()).setProductFirstThumbnail(any(Product.class), any());
        verify(productDataService, never()).saveProductOptions(anyList());
        verify(productDataService, never()).saveProductThumbnails(anyList());
        verify(productDataService, never()).saveProductInfoImages(anyList());
        verify(productDataService, never()).saveProductAndReturnId(any(Product.class));
        verify(productDataService, never()).deleteAllProductOptionByIds(anyList());
        verify(productDataService, never()).getClassificationByIdOrElseIllegal(any());
        verify(productDomainService, never()).deleteImages(anyList());
        verify(productDomainService, never()).deleteImage(any());
        verify(productDataService, never()).deleteAllProductThumbnailByImageNames(anyList());
    }

    @Test
    @DisplayName(value = "상품 수정. 상품 분류 아이디가 잘못된 경우")
    void patchProductWrongClassificationId() throws Exception {
        Product product = ProductFixture.createDefaultProductByOUTER(1).get(0);
        List<PatchOptionDTO> optionDTOList = product.getProductOptions()
                .stream()
                .map(v -> new PatchOptionDTO(1L, v.getSize(), v.getColor(), v.getStock(), v.isOpen()))
                .toList();
        AdminProductPatchDTO patchDTO = new AdminProductPatchDTO(
                product.getProductName(),
                "WrongId",
                product.getProductPrice(),
                product.isOpen(),
                product.getProductDiscount(),
                optionDTOList
        );
        AdminProductImageDTO imageDTO = new AdminProductImageDTO(
                null,
                null,
                null,
                null,
                null,
                null
        );
        List<Long> deleteOptionList = new ArrayList<>(List.of(1L, 2L, 3L));

        when(productDataService.getProductByIdOrElseIllegal(any())).thenReturn(product);
        when(productDataService.getClassificationByIdOrElseIllegal(any())).thenThrow(IllegalArgumentException.class);

        assertThrows(
                IllegalArgumentException.class,
                () -> adminProductWriteUseCase.patchProduct(product.getId(), deleteOptionList, patchDTO, imageDTO)
        );

        verify(productDomainService, never()).setPatchProductOptionData(any(Product.class), any(AdminProductPatchDTO.class));
        verify(productDomainService, never()).saveProductImage(any(Product.class), any(AdminProductImageDTO.class));
        verify(productDomainService, never()).setProductFirstThumbnail(any(Product.class), any());
        verify(productDataService, never()).saveProductOptions(anyList());
        verify(productDataService, never()).saveProductThumbnails(anyList());
        verify(productDataService, never()).saveProductInfoImages(anyList());
        verify(productDataService, never()).saveProductAndReturnId(any(Product.class));
        verify(productDataService, never()).deleteAllProductOptionByIds(anyList());
        verify(productDomainService, never()).deleteImages(anyList());
        verify(productDomainService, never()).deleteImage(any());
        verify(productDataService, never()).deleteAllProductThumbnailByImageNames(anyList());
    }

    @Test
    @DisplayName(value = "상품 수정. 추가할 대표 썸네일은 존재하지만, 삭제할 대표 썸네일이 null인 경우")
    void patchProductEmptyDeleteFirstThumbnail() throws Exception {
        Product product = ProductFixture.createDefaultProductByOUTER(1).get(0);
        MockMultipartFile firstThumb = createMockMultipartFile(product.getThumbnail());
        List<PatchOptionDTO> optionDTOList = product.getProductOptions()
                .stream()
                .map(v -> new PatchOptionDTO(1L, v.getSize(), v.getColor(), v.getStock(), v.isOpen()))
                .toList();
        AdminProductPatchDTO patchDTO = new AdminProductPatchDTO(
                product.getProductName(),
                product.getClassification().getId(),
                product.getProductPrice(),
                product.isOpen(),
                product.getProductDiscount(),
                optionDTOList
        );
        AdminProductImageDTO imageDTO = new AdminProductImageDTO(
                firstThumb,
                null,
                null,
                null,
                null,
                null
        );
        List<Long> deleteOptionList = new ArrayList<>(List.of(1L, 2L, 3L));

        when(productDataService.getProductByIdOrElseIllegal(any())).thenReturn(product);
        doNothing().when(productDomainService).setPatchProductOptionData(any(Product.class), any(AdminProductPatchDTO.class));
        when(productDomainService.saveProductImage(any(Product.class), any(AdminProductImageDTO.class))).thenReturn(Collections.emptyList());
        doNothing().when(productDomainService).deleteImages(anyList());

        assertThrows(
                IllegalArgumentException.class,
                () -> adminProductWriteUseCase.patchProduct(product.getId(), deleteOptionList, patchDTO, imageDTO)
        );

        verify(productDomainService, never()).setProductFirstThumbnail(any(Product.class), any());
        verify(productDataService, never()).saveProductOptions(anyList());
        verify(productDataService, never()).saveProductThumbnails(anyList());
        verify(productDataService, never()).saveProductInfoImages(anyList());
        verify(productDataService, never()).saveProductAndReturnId(any(Product.class));
        verify(productDataService, never()).deleteAllProductOptionByIds(anyList());
        verify(productDataService, never()).getClassificationByIdOrElseIllegal(any());
        verify(productDomainService, never()).deleteImage(any());
        verify(productDataService, never()).deleteAllProductThumbnailByImageNames(anyList());
    }

    @Test
    @DisplayName(value = "상품 수정. 삭제할 대표 썸네일은 존재하지만, 추가할 대표 썸네일이 null인 경우")
    void patchProductEmptyFirstThumbnail() throws Exception {
        Product product = ProductFixture.createDefaultProductByOUTER(1).get(0);
        List<PatchOptionDTO> optionDTOList = product.getProductOptions()
                .stream()
                .map(v -> new PatchOptionDTO(1L, v.getSize(), v.getColor(), v.getStock(), v.isOpen()))
                .toList();
        AdminProductPatchDTO patchDTO = new AdminProductPatchDTO(
                product.getProductName(),
                product.getClassification().getId(),
                product.getProductPrice(),
                product.isOpen(),
                product.getProductDiscount(),
                optionDTOList
        );
        AdminProductImageDTO imageDTO = new AdminProductImageDTO(
                null,
                "originFirstThumb",
                null,
                null,
                null,
                null
        );
        List<Long> deleteOptionList = new ArrayList<>(List.of(1L, 2L, 3L));

        when(productDataService.getProductByIdOrElseIllegal(any())).thenReturn(product);
        doNothing().when(productDomainService).setPatchProductOptionData(any(Product.class), any(AdminProductPatchDTO.class));
        when(productDomainService.saveProductImage(any(Product.class), any(AdminProductImageDTO.class))).thenReturn(Collections.emptyList());
        doNothing().when(productDomainService).deleteImages(anyList());

        assertThrows(
                IllegalArgumentException.class,
                () -> adminProductWriteUseCase.patchProduct(product.getId(), deleteOptionList, patchDTO, imageDTO)
        );

        verify(productDomainService, never()).setProductFirstThumbnail(any(Product.class), any());
        verify(productDataService, never()).saveProductOptions(anyList());
        verify(productDataService, never()).saveProductThumbnails(anyList());
        verify(productDataService, never()).saveProductInfoImages(anyList());
        verify(productDataService, never()).saveProductAndReturnId(any(Product.class));
        verify(productDataService, never()).deleteAllProductOptionByIds(anyList());
        verify(productDataService, never()).getClassificationByIdOrElseIllegal(any());
        verify(productDomainService, never()).deleteImage(any());
        verify(productDataService, never()).deleteAllProductThumbnailByImageNames(anyList());
    }

    @Test
    @DisplayName(value = "상품 할인율 수정 요청")
    void patchDiscountProduct() {
        List<Product> productFixture = ProductFixture.createDefaultProductByOUTER(5);
        List<String> productIds = productFixture.stream().map(Product::getId).toList();
        int discount = 10;
        AdminDiscountPatchDTO patchDTO = new AdminDiscountPatchDTO(productIds, discount);

        when(productDataService.getProductListByIds(anyList())).thenReturn(productFixture);
        doNothing().when(productDataService).patchProductDiscount(any(AdminDiscountPatchDTO.class));

        String result = assertDoesNotThrow(() -> adminProductWriteUseCase.patchDiscountProduct(patchDTO));

        assertEquals(Result.OK.getResultKey(), result);
    }

    @Test
    @DisplayName(value = "상품 할인율 수정 요청. 상품 아이디 리스트가 잘못된 경우")
    void patchDiscountProductWrongIds() {
        List<Product> productFixture = ProductFixture.createDefaultProductByOUTER(5);
        List<String> productIds = productFixture.stream().map(Product::getId).toList();
        int discount = 10;
        AdminDiscountPatchDTO patchDTO = new AdminDiscountPatchDTO(productIds, discount);

        when(productDataService.getProductListByIds(anyList())).thenReturn(Collections.emptyList());

        assertThrows(
                IllegalArgumentException.class,
                () -> adminProductWriteUseCase.patchDiscountProduct(patchDTO)
        );

        verify(productDataService, never()).patchProductDiscount(any(AdminDiscountPatchDTO.class));
    }

    @Test
    @DisplayName(value = "상품 할인율 수정 요청. 상품 아이디 리스트가 일부만 잘못된 경우")
    void patchDiscountProductWrongIdsPart() {
        List<Product> productFixture = ProductFixture.createDefaultProductByOUTER(5);
        List<String> productIds = productFixture.stream().map(Product::getId).toList();
        List<Product> resultProductFixture = productFixture.stream().limit(productFixture.size() - 2).toList();
        int discount = 10;
        AdminDiscountPatchDTO patchDTO = new AdminDiscountPatchDTO(productIds, discount);

        when(productDataService.getProductListByIds(anyList())).thenReturn(resultProductFixture);

        assertThrows(
                IllegalArgumentException.class,
                () -> adminProductWriteUseCase.patchDiscountProduct(patchDTO)
        );

        verify(productDataService, never()).patchProductDiscount(any(AdminDiscountPatchDTO.class));
    }
}
