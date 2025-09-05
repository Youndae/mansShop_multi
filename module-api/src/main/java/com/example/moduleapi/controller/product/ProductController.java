package com.example.moduleapi.controller.product;

import com.example.moduleapi.annotation.swagger.DefaultApiResponse;
import com.example.moduleapi.annotation.swagger.SwaggerAuthentication;
import com.example.moduleapi.mapper.PagingResponseMapper;
import com.example.moduleapi.model.response.PagingElementsResponseDTO;
import com.example.moduleapi.service.PrincipalService;
import com.example.moduleproduct.model.dto.page.ProductDetailPageDTO;
import com.example.moduleproduct.model.dto.product.business.ProductQnAResponseDTO;
import com.example.moduleproduct.model.dto.product.in.ProductQnAPostDTO;
import com.example.moduleproduct.model.dto.product.out.ProductDetailDTO;
import com.example.moduleproduct.model.dto.product.out.ProductDetailReviewDTO;
import com.example.moduleproduct.usecase.product.ProductReadUseCase;
import com.example.moduleproduct.usecase.productLike.ProductLikeWriteUseCase;
import com.example.moduleproduct.usecase.productQnA.ProductQnAWriteUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@Tag(name = "Product Controller")
@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductReadUseCase productReadUseCase;

    private final PrincipalService principalService;

    private final PagingResponseMapper pagingResponseMapper;

    private final ProductLikeWriteUseCase productLikeWriteUseCase;

    private final ProductQnAWriteUseCase productQnAWriteUseCase;

    /**
     *
     * @param productId
     * @param principal
     * @return
     *
     * 상품 상세 페이지 데이터 요청.
     * 상품에 대한 찜하기 상태가 필요하므로 Principal을 같이 받아줌.
     */
    @Operation(summary = "상품 상세 데이터 조회",
            description = "JWT를 같이 보내면 관심상품 여부를 확인 가능"
    )
    @DefaultApiResponse
    @SwaggerAuthentication
    @Parameters({
            @Parameter(
                    name = "productId",
                    description = "상품 아이디",
                    example = "BAGS20210629134401",
                    required = true,
                    in = ParameterIn.PATH
            )
    })
    @GetMapping("/{productId}")
    public ResponseEntity<ProductDetailDTO> getDetail(@PathVariable(name = "productId") String productId, Principal principal) {
        String userId = principalService.extractUserIdIfExist(principal);

        ProductDetailDTO responseDTO = productReadUseCase.getProductDetail(productId, userId);

        return ResponseEntity.ok(responseDTO);
    }

    /**
     *
     * @param productId
     * @param page
     * @return
     *
     * 상품 상세 페이지의 리뷰 데이터 요청.
     * 위 상세 페이지 데이터와 별개로 요청이 한번 더 발생하는 것이 아닌
     * 리뷰 페이징 처리를 위함.
     *
     */
    @Operation(summary = "상품 상세 페이지에서 리뷰 데이터 조회")
    @DefaultApiResponse
    @SwaggerAuthentication
    @Parameters({
            @Parameter(
                    name = "productId",
                    description = "상품 아이디",
                    example = "BAGS20210629134401",
                    required = true,
                    in = ParameterIn.PATH
            ),
            @Parameter(
                    name = "page",
                    description = "페이지 번호",
                    example = "1",
                    required = true,
                    in = ParameterIn.QUERY
            )
    })
    @GetMapping("/{productId}/review")
    public ResponseEntity<PagingElementsResponseDTO<ProductDetailReviewDTO>> getReview(@PathVariable(name = "productId") String productId,
                                                                                       @RequestParam(name = "page") int page) {
        ProductDetailPageDTO pageDTO = new ProductDetailPageDTO(page);
        Page<ProductDetailReviewDTO> responseDTO = productReadUseCase.getProductDetailReview(pageDTO, productId);

        return pagingResponseMapper.toPagingElementsResponse(responseDTO);
    }

    /**
     *
     * @param productId
     * @param page
     * @return
     *
     * 상품 상세 페이지 QnA 데이터 요청
     * 위 리뷰와 마찬가지로 첫페이지의 데이터가 아닌 페이징 기능을 위함
     */
    @Operation(summary = "상품 상세 페이지에서 상품 문의 데이터 조회")
    @DefaultApiResponse
    @SwaggerAuthentication
    @Parameters({
            @Parameter(
                    name = "productId",
                    description = "상품 아이디",
                    example = "BAGS20210629134401",
                    required = true,
                    in = ParameterIn.PATH
            ),
            @Parameter(
                    name = "page",
                    description = "페이지 번호.",
                    example = "2",
                    required = true,
                    in = ParameterIn.QUERY
            )
    })
    @GetMapping("/{productId}/qna")
    public ResponseEntity<PagingElementsResponseDTO<ProductQnAResponseDTO>> getQnA(@PathVariable(name = "productId") String productId,
                                                                                   @RequestParam(name = "page") int page) {

        ProductDetailPageDTO pageDTO = new ProductDetailPageDTO(page);
        Page<ProductQnAResponseDTO> responseDTO = productReadUseCase.getProductDetailQnA(pageDTO, productId);

        return pagingResponseMapper.toPagingElementsResponse(responseDTO);
    }

    /**
     *
     * @param postDTO
     * @param principal
     *
     * 상품 상세 페이지에서 상품 문의 작성
     * 로그인한 사용자만 요청 가능
     */
    @Operation(summary = "상품 문의 작성")
    @DefaultApiResponse
    @SwaggerAuthentication
    @PostMapping("/qna")
    public ResponseEntity<Void> postProductQnA(@RequestBody ProductQnAPostDTO postDTO, Principal principal) {

        String userId = principalService.extractUserId(principal);
        productQnAWriteUseCase.postProductQnA(postDTO, userId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     *
     * @param productIdMap
     * @param principal
     * @return
     *
     * 관심상품 등록 기능
     */
    @Operation(summary = "관심상품 등록")
    @DefaultApiResponse
    @SwaggerAuthentication
    @PostMapping("/like")
    public ResponseEntity<Void> likeProduct(@Schema(name = "productId", description = "상품 아이디")
                                                          @RequestBody Map<String, String> productIdMap,
                                                          Principal principal) {

        String productId = productIdMap.get("productId");

        if(!productIdMap.containsKey("productId") || productId == null || productId.isBlank()){
            log.warn("ProductService.likeProduct :: IllegalArgumentException by productIdMap");
            throw new IllegalArgumentException();
        }

        String userId = principalService.extractUserId(principal);
        productLikeWriteUseCase.likeProduct(productId, userId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     *
     * @param productId
     * @param principal
     * @return
     *
     * 관심상품 해제 기능
     */
    @Operation(summary = "관심상품 해제")
    @DefaultApiResponse
    @SwaggerAuthentication
    @Parameter(name = "productId",
            description = "상품 아이디",
            example = "BAGS20210629134401",
            required = true,
            in = ParameterIn.PATH
    )
    @DeleteMapping("/like/{productId}")
    public ResponseEntity<Void> deLikeProduct(@PathVariable(name = "productId") String productId,
                                                            Principal principal) {

        String userId = principalService.extractUserId(principal);
        productLikeWriteUseCase.deleteProductLike(productId, userId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
