package com.example.moduleapi.controller.product;

import com.example.moduleapi.useCase.product.MainReadUserCase;
import com.example.modulecommon.model.dto.response.PagingListResponseDTO;
import com.example.modulecommon.model.dto.response.PagingMappingDTO;
import com.example.moduleproduct.model.dto.main.out.MainListResponseDTO;
import com.example.moduleproduct.model.dto.page.ProductPageDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.StringToClassMapItem;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@Tag(name = "Main Controller")
@RestController
@RequestMapping("/api/main")
@RequiredArgsConstructor
@Slf4j
public class MainController {

    private final MainReadUserCase mainReadUserCase;

    @Value("#{filePath['file.product.path']}")
    private String filePath;

    @Operation(summary = "메인 BEST 상품과 NEW 카테고리 상품 조회",
    description = "요청 URL이 / 인 경우 BEST 상품을 조회하고 /new 인 경우 새로운 상품 기준 조회"
    )
    @ApiResponses(
            @ApiResponse(responseCode = "200", description = "Success",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(
                                    schema = @Schema(implementation = MainListResponseDTO.class)
                            )
                    )
            )
    )
    @GetMapping({"/", "/new"})
    public ResponseEntity<?> getMainProduct(HttpServletRequest request) {
        log.info("MainController :: getMainProduct");
        String requestURI = request.getRequestURI();
        String classification = requestURI.substring(requestURI.lastIndexOf("/") + 1);
        classification = classification.equals("") ? "BEST" : classification;

        ProductPageDTO pageDTO = ProductPageDTO.builder()
                                            .pageNum(1)
                                            .keyword(null)
                                            .classification(classification)
                                            .build();

        List<MainListResponseDTO> responseDTO = mainReadUserCase.getMainProduct(pageDTO);

        return ResponseEntity.status(HttpStatus.OK)
                .body(responseDTO);
    }


    @Operation(summary = "메인 카테고리별 상품 조회",
            description = "상품 등록순으로 조회"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200", description = "Success",
                    useReturnTypeSchema = true
            ),
            @ApiResponse(responseCode = "800", description = "token stealing"
                    , content = @Content(
                            examples = @ExampleObject(value = "토큰 탈취 응답입니다.")
                    )
            ),
            @ApiResponse(responseCode = "403", description = "Access Denied"
                    , content = @Content(
                            examples = @ExampleObject(value = "권한 오류 응답입니다.")
                    )
            )
    })
    @Parameters({
            @Parameter(
                    name = "classification",
                    description = "상품 분류. OUTER, TOP, PANTS, SHOES, BAGS",
                    example = "OUTER",
                    required = true
            ),
            @Parameter(
                    name = "page",
                    description = "상품 리스트 페이지 번호. 최소값 1",
                    required = true
            )
    })
    @GetMapping("/{classification}")
    public ResponseEntity<PagingListResponseDTO<MainListResponseDTO>> getClassificationProduct(@PathVariable(name = "classification") String classification,
                                                    @RequestParam(name = "keyword", required = false) String keyword,
                                                    @RequestParam(name = "page") int page) {
        ProductPageDTO pageDTO = ProductPageDTO.builder()
                                                .pageNum(page)
                                                .keyword(keyword)
                                                .classification(classification)
                                                .build();

        PagingListResponseDTO<MainListResponseDTO> responseDTO = mainReadUserCase.getClassificationProduct(pageDTO);

        return ResponseEntity.ok(responseDTO);
    }

    @Operation(summary = "메인 상품 검색")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200", description = "Success",
                    useReturnTypeSchema = true
            ),
            @ApiResponse(responseCode = "800", description = "token stealing"
                    , content = @Content(
                        examples = @ExampleObject(value = "토큰 탈취 응답입니다.")
                    )
            ),
            @ApiResponse(responseCode = "403", description = "Access Denied"
                    , content = @Content(
                        examples = @ExampleObject(value = "권한 오류 응답입니다.")
                    )
            )
    })
    @Parameters({
            @Parameter(
                    name = "keyword",
                    description = "상품명 검색 키워드",
                    example = "DummyOUTER",
                    required = true
            ),
            @Parameter(
                    name = "page",
                    description = "상품 리스트 페이지 번호. 최소값 1",
                    required = true
            )
    })
    @GetMapping("/search")
    public ResponseEntity<PagingListResponseDTO<MainListResponseDTO>> getSearchProduct(@RequestParam(name = "keyword") String keyword,
                                                                                       @RequestParam(name = "page") int page) {
        ProductPageDTO pageDTO = ProductPageDTO.builder()
                                                .pageNum(page)
                                                .keyword(keyword)
                                                .classification(null)
                                                .build();

        PagingListResponseDTO<MainListResponseDTO> responseDTO = mainReadUserCase.getClassificationProduct(pageDTO);

        return ResponseEntity.ok(responseDTO);
    }

    @Operation(summary = "이미지 조회",
    description = "byte[] 타입으로 반환")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200", description = "Success",
                    useReturnTypeSchema = true
            )
    })
    @Parameter(name = "imageName",
                description = "테스트 이미지명으로 테스트 가능",
                example = "mansShop_testImage",
                required = true
    )
    @GetMapping("/display/{imageName}")
    public ResponseEntity<byte[]> display(@PathVariable(name = "imageName") String imageName) {
        File file = new File(filePath + imageName);
        ResponseEntity<byte[]> result = null;

        try {
            HttpHeaders header = new HttpHeaders();
            header.add("Content-Type", Files.probeContentType(file.toPath()));

            result = new ResponseEntity<>(FileCopyUtils.copyToByteArray(file), HttpStatus.OK);
        }catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }
}
