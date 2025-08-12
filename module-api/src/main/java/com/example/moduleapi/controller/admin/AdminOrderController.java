package com.example.moduleapi.controller.admin;

import com.example.moduleapi.annotation.swagger.DefaultApiResponse;
import com.example.moduleapi.annotation.swagger.SwaggerAuthentication;
import com.example.moduleapi.mapper.PagingResponseMapper;
import com.example.moduleapi.model.response.PagingElementsResponseDTO;
import com.example.moduleapi.model.response.PagingResponseDTO;
import com.example.modulecache.model.cache.CacheRequest;
import com.example.modulecache.service.FullCountScanCachingService;
import com.example.modulecommon.model.dto.response.PagingListDTO;
import com.example.modulecommon.model.dto.response.ResponseMessageDTO;
import com.example.modulecommon.model.enumuration.RedisCaching;
import com.example.moduleorder.model.dto.admin.out.AdminOrderResponseDTO;
import com.example.moduleorder.model.dto.admin.page.AdminOrderPageDTO;
import com.example.moduleorder.usecase.admin.AdminOrderReadUseCase;
import com.example.moduleorder.usecase.admin.AdminOrderWriteUseCase;
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

@Tag(name = "Admin Order Controller")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminOrderController {

    private final PagingResponseMapper pagingResponseMapper;

    private final AdminOrderReadUseCase adminOrderReadUseCase;

    private final AdminOrderWriteUseCase adminOrderWriteUseCase;

    private final FullCountScanCachingService fullCountScanCachingService;

    /**
     *
     * @param searchType
     * @param keyword
     * @param page
     *
     * 모든 주문내역 리스트
     */
    @Operation(summary = "전체 주문내역 목록 조회")
    @DefaultApiResponse
    @SwaggerAuthentication
    @Parameters({
            @Parameter(name = "page",
                    description = "페이지 번호",
                    example = "1",
                    in = ParameterIn.QUERY
            ),
            @Parameter(name = "searchType",
                    description = "검색 타입. recipient, userId",
                    example = "userId",
                    in = ParameterIn.QUERY
            ),
            @Parameter(name = "keyword",
                    description = "검색어",
                    example = "tester1",
                    in = ParameterIn.QUERY
            )
    })
    @GetMapping("/order/all")
    public ResponseEntity<PagingResponseDTO<AdminOrderResponseDTO>> getAllOrder(@RequestParam(name = "searchType", required = false) String searchType,
                                                                                @RequestParam(value = "keyword", required = false) String keyword,
                                                                                @RequestParam(name = "page", required = false, defaultValue = "1") int page) {

        AdminOrderPageDTO pageDTO = new AdminOrderPageDTO(keyword, searchType, page);
        long totalElements = 0L;
        if(keyword == null)
            totalElements = fullCountScanCachingService.getFullScanCountCache(RedisCaching.ADMIN_ORDER_COUNT, new CacheRequest<>(pageDTO));

        PagingListDTO<AdminOrderResponseDTO> responseDTO = adminOrderReadUseCase.getAdminAllOrderList(pageDTO, totalElements);

        return pagingResponseMapper.toPagingResponse(responseDTO);
    }

    /**
     *
     * @param searchType
     * @param keyword
     * @param page
     *
     * 미처리 주문 내역 리스트
     */
    @Operation(summary = "미처리 주문내역 목록 조회")
    @DefaultApiResponse
    @SwaggerAuthentication
    @Parameters({
            @Parameter(name = "page",
                    description = "페이지 번호",
                    example = "1",
                    in = ParameterIn.QUERY
            ),
            @Parameter(name = "searchType",
                    description = "검색 타입. recipient, userId",
                    example = "userId",
                    in = ParameterIn.QUERY
            ),
            @Parameter(name = "keyword",
                    description = "검색어",
                    example = "tester1",
                    in = ParameterIn.QUERY
            )
    })
    @GetMapping("/order/new")
    public ResponseEntity<PagingElementsResponseDTO<AdminOrderResponseDTO>> getNewOrder(@RequestParam(name = "searchType", required = false) String searchType,
                                                                                        @RequestParam(name = "keyword", required = false) String keyword,
                                                                                        @RequestParam(name = "page", required = false, defaultValue = "1") int page) {

        AdminOrderPageDTO pageDTO = new AdminOrderPageDTO(keyword, searchType, page);
        PagingListDTO<AdminOrderResponseDTO> responseDTO = adminOrderReadUseCase.getAdminNewOrderList(pageDTO);

        return pagingResponseMapper.toPagingElementsResponse(responseDTO);
    }

    /**
     *
     * @param orderId
     *
     * 주문 처리.
     * 사용자가 결제한 주문 확인중 상태에서 관리자가 주문 확인 버튼을 클릭.
     * 상태를 상품 준비중으로 수정.
     */
    @Operation(summary = "주문 확인 처리")
    @DefaultApiResponse
    @SwaggerAuthentication
    @Parameter(name = "orderId",
            description = "주문 아이디",
            example = "1",
            required = true,
            in = ParameterIn.PATH
    )
    @PatchMapping("/order/{orderId}")
    public ResponseEntity<ResponseMessageDTO> patchOrder(@PathVariable(name = "orderId") long orderId) {
        String responseMessage = adminOrderWriteUseCase.orderPreparation(orderId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ResponseMessageDTO(responseMessage));
    }
}
