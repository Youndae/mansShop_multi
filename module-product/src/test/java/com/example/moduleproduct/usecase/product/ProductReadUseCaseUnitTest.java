package com.example.moduleproduct.usecase.product;

import com.example.modulecommon.model.entity.Classification;
import com.example.modulecommon.model.entity.Product;
import com.example.moduleproduct.model.dto.page.ProductDetailPageDTO;
import com.example.moduleproduct.model.dto.product.business.ProductOptionDTO;
import com.example.moduleproduct.model.dto.product.business.ProductQnADTO;
import com.example.moduleproduct.model.dto.product.business.ProductQnAReplyDTO;
import com.example.moduleproduct.model.dto.product.business.ProductQnAResponseDTO;
import com.example.moduleproduct.model.dto.product.out.ProductDetailDTO;
import com.example.moduleproduct.model.dto.product.out.ProductDetailQnAReplyListDTO;
import com.example.moduleproduct.model.dto.product.out.ProductDetailReviewDTO;
import com.example.moduleproduct.model.dto.product.out.ProductPageableDTO;
import com.example.moduleproduct.service.product.ProductDataService;
import com.example.moduleproduct.service.product.ProductDomainService;
import com.example.moduleproduct.service.productLike.ProductLikeDataService;
import com.example.moduleproduct.service.productQnA.ProductQnADataService;
import com.example.moduleproduct.service.productReview.ProductReviewDataService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ProductReadUseCaseUnitTest {

    @InjectMocks
    private ProductReadUseCase productReadUseCase;

    @Mock
    private ProductDataService productDataService;

    @Mock
    private ProductDomainService productDomainService;

    @Mock
    private ProductQnADataService productQnADataService;

    @Mock
    private ProductReviewDataService productReviewDataService;

    @Mock
    private ProductLikeDataService productLikeDataService;

    @Test
    @DisplayName(value = "상품 상세 정보 조회")
    void getProductDetail() {
        Product product = Product.builder()
                                .id("testProductId")
                                .classification(Classification.builder().id("OUTER").classificationStep(1).build())
                                .productName("testProductName")
                                .productPrice(5000)
                                .thumbnail("testProductThumbnail")
                                .isOpen(true)
                                .productSalesQuantity(1000L)
                                .productDiscount(10)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build();
        List<ProductOptionDTO> optionDTOList = new ArrayList<>();
        List<String> thumbnails = new ArrayList<>();
        List<String> infoImages = new ArrayList<>();
        List<ProductDetailReviewDTO> detailReviewDTOList = new ArrayList<>();
        List<ProductQnAResponseDTO> detailQnADTOList = new ArrayList<>();
        List<ProductQnADTO> qnADTOList = new ArrayList<>();
        List<ProductDetailQnAReplyListDTO> replyListDTO = new ArrayList<>();
        for(int i = 0; i < 3; i++) {
            optionDTOList.add(
                    new ProductOptionDTO(
                            (long) i,
                            "size" + i,
                            "color" + i,
                            i * 10
                    )
            );
            thumbnails.add("thumbnail" + i);
            infoImages.add("infoImage" + i);
            detailReviewDTOList.add(
                    new ProductDetailReviewDTO(
                            "writer" + i,
                            "reviewContent" + i,
                            LocalDate.now(),
                            "answerContent" + i,
                            LocalDate.now()
                    )
            );
            detailQnADTOList.add(
                    new ProductQnAResponseDTO(
                            (long) i,
                            "writer" + i,
                            "qnaContent" + i,
                            LocalDate.now(),
                            i % 2 == 0,
                            List.of(new ProductQnAReplyDTO(
                                    "admin",
                                    "content" + i,
                                    LocalDate.now()
                            ))
                    )
            );
            qnADTOList.add(
                    new ProductQnADTO(
                            (long) i,
                            "writer" + i,
                            "qnaContent" + i,
                            LocalDate.now(),
                            i % 2 == 0
                    )
            );
            replyListDTO.add(
                    new ProductDetailQnAReplyListDTO(
                            "admin",
                            "content" + i,
                            (long) i,
                            LocalDateTime.now()
                    )
            );
        }
        Page<ProductQnADTO> qnaDTOPage = new PageImpl<>(qnADTOList);
        Page<ProductDetailReviewDTO> reviewPage = new PageImpl<>(detailReviewDTOList);

        when(productDataService.getProductByIdOrElseIllegal(any())).thenReturn(product);
        when(productLikeDataService.getProductLikeStatusByUser(any(), any())).thenReturn(true);
        when(productDataService.getProductOptionDTOListByProductId(any())).thenReturn(optionDTOList);
        when(productDataService.getProductThumbnailImageNameList(any())).thenReturn(thumbnails);
        when(productDataService.getProductInfoImageNameList(any())).thenReturn(infoImages);
        when(productReviewDataService.getProductDetailReview(any(ProductDetailPageDTO.class), any())).thenReturn(reviewPage);
        when(productQnADataService.getProductDetailQnA(any(Pageable.class), any())).thenReturn(qnaDTOPage);
        when(productQnADataService.getProductDetailQnAReplyList(anyList())).thenReturn(replyListDTO);
        when(productDomainService.mapToProductQnAResponseDTO(any(), anyList())).thenReturn(detailQnADTOList);

        Page<ProductQnAResponseDTO> qnaResponse = new PageImpl<>(detailQnADTOList);

        ProductDetailDTO fixture = new ProductDetailDTO(
                product,
                true,
                optionDTOList,
                thumbnails,
                infoImages,
                new ProductPageableDTO<>(reviewPage),
                new ProductPageableDTO<>(qnaResponse)
        );

        ProductDetailDTO result = assertDoesNotThrow(() -> productReadUseCase.getProductDetail(product.getId(), "tester"));

        assertNotNull(result);
        assertEquals(fixture, result);
    }

    @Test
    @DisplayName(value = "해당 상품 문의 목록 조회")
    void getProductDetailQnA() {
        ProductDetailPageDTO pageDTO = new ProductDetailPageDTO();
        List<ProductQnADTO> qnADTOList = new ArrayList<>();
        List<ProductDetailQnAReplyListDTO> replyListDTO = new ArrayList<>();
        List<ProductQnAResponseDTO> detailQnADTOList = new ArrayList<>();

        for(int i = 0; i < 3; i++) {
            detailQnADTOList.add(
                    new ProductQnAResponseDTO(
                            (long) i,
                            "writer" + i,
                            "qnaContent" + i,
                            LocalDate.now(),
                            i % 2 == 0,
                            List.of(new ProductQnAReplyDTO(
                                    "admin",
                                    "content" + i,
                                    LocalDate.now()
                            ))
                    )
            );
            qnADTOList.add(
                    new ProductQnADTO(
                            (long) i,
                            "writer" + i,
                            "qnaContent" + i,
                            LocalDate.now(),
                            i % 2 == 0
                    )
            );
            replyListDTO.add(
                    new ProductDetailQnAReplyListDTO(
                            "admin",
                            "content" + i,
                            (long) i,
                            LocalDateTime.now()
                    )
            );
        }
        Page<ProductQnADTO> qnaDTOPage = new PageImpl<>(qnADTOList);
        Page<ProductQnAResponseDTO> fixture = new PageImpl<>(detailQnADTOList);

        when(productQnADataService.getProductDetailQnA(any(Pageable.class), any())).thenReturn(qnaDTOPage);
        when(productQnADataService.getProductDetailQnAReplyList(anyList())).thenReturn(replyListDTO);
        when(productDomainService.mapToProductQnAResponseDTO(any(), anyList())).thenReturn(detailQnADTOList);

        Page<ProductQnAResponseDTO> result = assertDoesNotThrow(() -> productReadUseCase.getProductDetailQnA(pageDTO, "testProductId"));

        assertNotNull(result);
        assertEquals(fixture.getContent(), result.getContent());
        assertEquals(fixture.getTotalElements(), result.getTotalElements());
        assertEquals(fixture.getTotalPages(), result.getTotalPages());
    }
}
