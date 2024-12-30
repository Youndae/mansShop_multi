package com.example.moduleapi.controller.product;

import com.example.moduleapi.annotation.SwaggerAuthentication;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@Tag(name = "ProductController")
@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    /*
        productDetail
        detail Review
        detail QnA
        post qna
        post like
        delete like
     */

    @Operation(summary = "상품 상세 정보 조회",
    description = "상품 기본 정보와 관심 상품 여부, 할인율, 옵션 리스트, 썸네일 리스트, 리뷰, 상품 문의 사항 조회.\n토큰이 없다면 관심 상품은 무조건 false로 반환")
    // TODO: ApiResponse
    @GetMapping("/{productId}")
    public ResponseEntity<?> getDetail(@Parameter(name = "productId",
                                                description = "상품 아이디",
                                                example = "OUTER20210630113120",
                                                in = ParameterIn.PATH
                                        )
                                       @PathVariable(name = "productId") String productId,
                                       Principal principal) {

        return null;
    }

    @Operation(summary = "상품 리뷰 리스트 조회",
    description = "상품 상세 페이지 내에서 리뷰 리스트 페이징 기능 동작 시 요청.")
    //TODO: ApiResponse
    @Parameters({
            @Parameter(
                    name = "productId",
                    description = "상품 아이디",
                    example = "OUTER20210630113120",
                    in = ParameterIn.PATH
            ),
            @Parameter(
                    name = "page",
                    description = "리뷰 페이지 번호",
                    example = "1",
                    in = ParameterIn.PATH
            )
    })
    @GetMapping("/{productId}/review/{page}")
    public ResponseEntity<?> getReview(@PathVariable(name = "productId") String productId,
                                       @PathVariable(name = "page") int page) {

        return null;
    }

    @Operation(summary = "상품 QnA 리스트 조회",
    description = "상품 상세 페이지 내에서 상품 리스트 페이징 기능 동작 시 요청")
    //TODO: ApiResponse
    @Parameters({
            @Parameter(
                    name = "productId",
                    description = "상품 아이디",
                    example = "OUTER20210630113120",
                    in = ParameterIn.PATH
            ),
            @Parameter(
                    name = "page",
                    description = "상품문의 페이지 번호",
                    example = "1",
                    in = ParameterIn.PATH
            )
    })
    @GetMapping("/{productId}/qna/{page}")
    public ResponseEntity<?> getQnA(@PathVariable(name = "productId") String productId,
                                    @PathVariable(name = "page") int page) {

        return null;
    }

    @Operation(summary = "상품 문의 작성 요청",
    description = "상품 상세 페이지 내에서 문의 작성 요청")
    //TODO: ApiResponse
    //TODO: Parameter DTO
    @PostMapping("/qna")
    //TODO: ResponseEntity<String>
    public ResponseEntity<?> postProductQnA() {

        return null;
    }

    @Operation(summary = "관심 상품 등록",
    description = "상품 상세 페이지 내에서 관심 상품 등록 요청")
    //TODO: ApiResponse
    @SwaggerAuthentication
    @PostMapping("/like")
    public ResponseEntity<?> likeProduct(@Parameter(name = "ProductIdMap",
                                                    description = "상품 아이디 Key : Value로 전달",
                                                    example = "productId : OUTER20210630113120"
                                        )
                                        @RequestBody Map<String, String> productId,
                                        Principal principal) {

        return null;
    }

    @Operation(summary = "관심 상품 해제",
    description = "상품 상세 페이지 내에서 관심 상품 해제 요청")
    //TODO: ApiResponse
    @SwaggerAuthentication
    @DeleteMapping("/like/{productId}")
    public ResponseEntity<?> deLikeProduct(@Parameter(name = "productId",
                                                    description = "상품 아이디",
                                                    example = "OUTER20210630113120",
                                                    in = ParameterIn.PATH
                                            )
                                           @PathVariable(name = "productId") String productId,
                                           Principal principal) {

        return null;
    }
}
