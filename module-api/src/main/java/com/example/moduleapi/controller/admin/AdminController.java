package com.example.moduleapi.controller.admin;

import com.example.moduleapi.annotation.swagger.SwaggerAuthentication;
import com.example.modulecommon.model.entity.Member;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@Tag(name = "Admin Controller")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

//    private final AdminUseCase adminUseCase;
//
//    private final MyPageUseCase myPageUseCase;

    /**
     *
     * @param keyword
     * @param page
     * @param principal
     *
     */
    @Operation(summary = "관리자 상품 목록 리스트 조회",
                description = "관리자 기능 중 상품 목록 리스트. \n검색, 페이징 기능 포함"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success",
                            content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Member.class))
            ),
            @ApiResponse(responseCode = "800", description = "Authentication Token Stealing"),
            @ApiResponse(responseCode = "403", description = "AccessDenied")
    })
    @Parameters({
            @Parameter(
                    name = "keyword",
                    description = "Product Search Keyword",
                    required = false,
                    example = "testKeyword"
            ),
            @Parameter(
                    name = "page",
                    description = "Start page number is 1",
                    required = true,
                    example = "1"
            )
    })
    @SwaggerAuthentication
    @GetMapping("/product")
    public ResponseEntity<?> getProductList(@RequestParam(name = "keyword", required = false) String keyword,
                                            @RequestParam(name = "page") int page,
                                            Principal principal) {
        /*
            TODO
            Swagger 테스트 먼저 진행하고 테스트 이후에 다른 코드들 작성.
            Controller Method 모두 생성한 이후에 UseCase 작성하면서 연결하는 방식으로.

            그리고 각 기능에 대해 설계 이후 진행하는 편이 나을 듯.
            서비스 모듈에서 메소드 작성 이후 테스트 코드 작성해서 꼭 테스트 통과한 다음 진행하는 방향으로 진행.
            이틀 잡아보자.
         */
        return null;
    }


}
