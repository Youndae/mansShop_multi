package com.example.moduleapi.controller.admin;

import com.example.moduleapi.annotation.swagger.DefaultApiResponse;
import com.example.moduleapi.annotation.swagger.SwaggerAuthentication;
import com.example.moduleapi.mapper.PagingResponseMapper;
import com.example.moduleapi.model.response.PagingResponseDTO;
import com.example.moduleapi.service.PrincipalService;
import com.example.modulecache.model.cache.CacheRequest;
import com.example.modulecache.service.FullCountScanCachingService;
import com.example.modulecommon.model.dto.response.PagingListDTO;
import com.example.modulecommon.model.enumuration.AdminListType;
import com.example.modulecommon.model.enumuration.RedisCaching;
import com.example.moduleproduct.model.dto.admin.review.in.AdminReviewReplyRequestDTO;
import com.example.moduleproduct.model.dto.admin.review.out.AdminReviewDTO;
import com.example.moduleproduct.model.dto.admin.review.out.AdminReviewDetailDTO;
import com.example.moduleproduct.model.dto.page.AdminReviewPageDTO;
import com.example.moduleproduct.usecase.admin.productReview.AdminProductReviewReadUseCase;
import com.example.moduleproduct.usecase.admin.productReview.AdminProductReviewWriteUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Tag(name = "Admin Review Controller")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminReviewController {

    private final AdminProductReviewReadUseCase adminProductReviewReadUseCase;

    private final AdminProductReviewWriteUseCase adminProductReviewWriteUseCase;

    private final PrincipalService principalService;

    private final PagingResponseMapper pagingResponseMapper;

    private final FullCountScanCachingService fullCountScanCachingService;

    /**
     *
     * @param keyword
     * @param page
     * @param searchType
     *
     * 관리자의 새로운 리뷰 리스트 조회.
     */
    @Operation(summary = "새로운 리뷰 리스트 조회",
            description = "검색은 사용자 아이디 및 닉네임 또는 상품명 기반으로 선택 후 검색. 새로운 리뷰의 기준은 답변 처리가 안된 리뷰."
    )
    @DefaultApiResponse
    @SwaggerAuthentication
    @Parameters({
            @Parameter(name = "page",
                    description = "페이지 번호",
                    example = "1",
                    in = ParameterIn.QUERY
            ),
            @Parameter(name = "searchType",
                    description = "검색 타입. user 또는 product",
                    example = "user",
                    in = ParameterIn.QUERY
            ),
            @Parameter(name = "keyword",
                    description = "검색어",
                    example = "tester1",
                    in = ParameterIn.QUERY
            )
    })
    @GetMapping("/review")
    public ResponseEntity<PagingResponseDTO<AdminReviewDTO>> getNewReviewList(@RequestParam(name = "keyword", required = false) String keyword,
                                                                              @RequestParam(name = "page", required = false, defaultValue = "1") int page,
                                                                              @RequestParam(name = "searchType", required = false) String searchType) {

        AdminReviewPageDTO pageDTO = new AdminReviewPageDTO(keyword, searchType, page);

        PagingListDTO<AdminReviewDTO> responseDTO = adminProductReviewReadUseCase.getReviewList(pageDTO, AdminListType.NEW, 0L);

        return pagingResponseMapper.toPagingResponse(responseDTO);
    }

    /**
     *
     * @param keyword
     * @param page
     * @param searchType
     *
     * 전체 리뷰 조회.
     */
    @Operation(summary = "전체 리뷰 조회",
            description = "검색은 사용자 아이디 및 닉네임 또는 상품명 기반으로 선택 후 검색. 새로운 리뷰의 기준은 답변 처리가 안된 리뷰."
    )
    @DefaultApiResponse
    @SwaggerAuthentication
    @Parameters({
            @Parameter(name = "page",
                    description = "페이지 번호",
                    example = "1",
                    in = ParameterIn.QUERY
            ),
            @Parameter(name = "searchType",
                    description = "검색 타입. user 또는 product",
                    example = "user",
                    in = ParameterIn.QUERY
            ),
            @Parameter(name = "keyword",
                    description = "검색어",
                    example = "tester1",
                    in = ParameterIn.QUERY
            )
    })
    @GetMapping("/review/all")
    public ResponseEntity<PagingResponseDTO<AdminReviewDTO>> getAllReviewList(@RequestParam(name = "keyword", required = false) String keyword,
                                                                              @RequestParam(name = "page", required = false, defaultValue = "1") int page,
                                                                              @RequestParam(name = "searchType", required = false) String searchType) {
        AdminReviewPageDTO pageDTO = new AdminReviewPageDTO(keyword, searchType, page);
        long totalElements = 0L;

        if(keyword == null)
            totalElements = fullCountScanCachingService.getFullScanCountCache(RedisCaching.ADMIN_REVIEW_COUNT, new CacheRequest<>(pageDTO, AdminListType.ALL.getType()));

        PagingListDTO<AdminReviewDTO> responseDTO = adminProductReviewReadUseCase.getReviewList(pageDTO, AdminListType.ALL, totalElements);

        return pagingResponseMapper.toPagingResponse(responseDTO);
    }

    /**
     *
     * @param reviewId
     *
     * 리뷰 상세 데이터 조회
     */
    @Operation(summary = "리뷰 상세 데이터 조회")
    @DefaultApiResponse
    @SwaggerAuthentication
    @Parameter(name = "reviewId",
            description = "리뷰 아이디",
            example = "1",
            required = true,
            in = ParameterIn.PATH
    )
    @GetMapping("/review/detail/{reviewId}")
    public ResponseEntity<AdminReviewDetailDTO> getReviewDetail(@PathVariable("reviewId") long reviewId) {
        AdminReviewDetailDTO responseDTO = adminProductReviewReadUseCase.getReviewDetail(reviewId);

        return ResponseEntity.ok(responseDTO);
    }

    /**
     *
     * @param postDTO
     * @param principal
     *
     * 관리자의 리뷰 답변 작성
     */
    @Operation(summary = "리뷰 답변 작성")
    @DefaultApiResponse
    @SwaggerAuthentication
    @PostMapping("/review/reply")
    public ResponseEntity<Void> postReviewReply(@RequestBody AdminReviewReplyRequestDTO postDTO,
                                                              Principal principal) {
        String userId = principalService.extractUserId(principal);
        adminProductReviewWriteUseCase.postReviewReply(postDTO, userId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
