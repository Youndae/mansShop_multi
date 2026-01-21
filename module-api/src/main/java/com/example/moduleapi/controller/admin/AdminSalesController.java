package com.example.moduleapi.controller.admin;

import com.example.moduleadmin.model.dto.page.AdminSalesPageDTO;
import com.example.moduleadmin.model.dto.sales.in.SalesLocalDateOrderDTO;
import com.example.moduleadmin.model.dto.sales.in.SalesLocalDateTermDTO;
import com.example.moduleadmin.model.dto.sales.in.SalesYearMonthClassificationDTO;
import com.example.moduleadmin.model.dto.sales.in.SalesYearMonthTermDTO;
import com.example.moduleadmin.model.dto.sales.out.*;
import com.example.moduleadmin.usecase.sales.SalesReadeUseCase;
import com.example.moduleapi.annotation.swagger.DefaultApiResponse;
import com.example.moduleapi.annotation.swagger.SwaggerAuthentication;
import com.example.moduleapi.mapper.PagingResponseMapper;
import com.example.moduleapi.model.response.PagingElementsResponseDTO;
import com.example.moduleapi.model.response.PagingResponseDTO;
import com.example.modulecommon.model.dto.request.ListRequestDTO;
import com.example.modulecommon.model.dto.response.PagingListDTO;
import com.example.moduleorder.model.dto.admin.out.AdminDailySalesResponseDTO;
import com.example.moduleorder.usecase.admin.AdminOrderReadUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(name = "Admin Sales Controller")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminSalesController {

    private final SalesReadeUseCase salesReadeUseCase;

    private final AdminOrderReadUseCase adminOrderReadUseCase;

    private final PagingResponseMapper pagingResponseMapper;

    /**
     *
     * @param term
     *
     * 기간별 매출 조회.
     * term은 연도를 받는다.
     */
    @Operation(summary = "기간별 매출 조회",
            description = "연도별 매출 조회기능. 해당 연도의 월별 매출 데이터를 조회"
    )
    @DefaultApiResponse
    @SwaggerAuthentication
    @Parameter(name = "term",
            description = "조회할 연도. 3년 전 데이터까지만 저장",
            example = "2023",
            required = true,
            in = ParameterIn.PATH
    )
    @GetMapping("/sales/period/{term}")
    public ResponseEntity<AdminPeriodSalesResponseDTO<AdminPeriodSalesListDTO>> getPeriodSales(@PathVariable(name = "term") int term) {

        AdminPeriodSalesResponseDTO<AdminPeriodSalesListDTO> responseDTO = salesReadeUseCase.getPeriodSales(term);

        return ResponseEntity.ok(responseDTO);
    }


    /**
     *
     * @param termDTO
     *
     * 관리자의 월매출 조회.
     * term은 YYYY-MM으로 연월을 받는다.
     */
    @Operation(summary = "월 매출 조회",
            description = "해당 월의 매출 데이터 조회"
    )
    @DefaultApiResponse
    @SwaggerAuthentication
    @Parameter(name = "term",
            description = "조회하고자 하는 연/월",
            example = "2023-04",
            required = true,
            in = ParameterIn.PATH
    )
    @GetMapping("sales/period/detail")
    public ResponseEntity<AdminPeriodMonthDetailResponseDTO> getPeriodSalesDetail(@ParameterObject @Valid SalesYearMonthTermDTO termDTO) {

        AdminPeriodMonthDetailResponseDTO responseDTO = salesReadeUseCase.getPeriodSalesDetailByYearMonth(termDTO.term());

        return ResponseEntity.ok(responseDTO);
    }

    /**
     *
     * @param salesYearMonthClassificationDTO
     *
     * 특정 상품 분류의 월 매출 조회
     * term으로 YYYY-MM 구조의 연월을 받는다.
     */
    @Operation(summary = "선택한 상품 분류의 월 매출 조회")
    @DefaultApiResponse
    @SwaggerAuthentication
    @Parameters({
            @Parameter(name = "term",
                    description = "조회하고자 하는 연/월",
                    example = "2023-04",
                    required = true,
                    in = ParameterIn.QUERY
            ),
            @Parameter(name = "classification",
                    description = "조회하고자 하는 상품 분류",
                    example = "OUTER",
                    required = true,
                    in = ParameterIn.QUERY
            )
    })
    @GetMapping("/sales/period/detail/classification")
    public ResponseEntity<AdminClassificationSalesResponseDTO> getSalesByClassification(@ParameterObject @Valid SalesYearMonthClassificationDTO salesYearMonthClassificationDTO) {
        AdminClassificationSalesResponseDTO responseDTO = salesReadeUseCase.getSalesByClassification(salesYearMonthClassificationDTO);

        return ResponseEntity.ok(responseDTO);
    }

    /**
     *
     * @param termDTO
     *
     * 일매출 조회.
     * term으로 YYYY-MM-DD 로 연월일을 받는다.
     */
    @Operation(summary = "일 매출 조회")
    @DefaultApiResponse
    @SwaggerAuthentication
    @Parameter(name = "term",
            description = "조회하고자 하는 연/월/일",
            example = "2023-04-02",
            required = true,
            in = ParameterIn.QUERY
    )
    @GetMapping("/sales/period/detail/day")
    public ResponseEntity<AdminPeriodSalesResponseDTO<AdminPeriodClassificationDTO>> getSalesByDay(@ParameterObject @Valid SalesLocalDateTermDTO termDTO) {

        AdminPeriodSalesResponseDTO<AdminPeriodClassificationDTO> responseDTO = salesReadeUseCase.getSalesByDay(termDTO.term());

        return ResponseEntity.ok(responseDTO);
    }

    /**
     *
     * @param orderDTO
     *
     * 선택 일자의 모든 주문 목록을 조회
     * term으로 YYYY-MM-DD구조의 연월일을 받는다.
     */
    @Operation(summary = "선택일자의 모든 주문 목록 조회")
    @DefaultApiResponse
    @SwaggerAuthentication
    @Parameters({
            @Parameter(name = "term",
                    description = "조회하고자 하는 연/월/일",
                    example = "2023-04-02",
                    required = true,
                    in = ParameterIn.QUERY
            ),
            @Parameter(name = "page",
                    description = "페이지 번호",
                    example = "1",
                    required = true,
                    in = ParameterIn.QUERY
            )
    })
    @GetMapping("/sales/period/order-list")
    public ResponseEntity<PagingElementsResponseDTO<AdminDailySalesResponseDTO>> getOrderListByDay(@ParameterObject @Valid SalesLocalDateOrderDTO orderDTO) {

        PagingListDTO<AdminDailySalesResponseDTO> responseDTO = adminOrderReadUseCase.getOrderListByDay(orderDTO.term(), orderDTO.page());
        return pagingResponseMapper.toPagingElementsResponse(responseDTO);
    }


    /**
     *
     * @param requestDTO
     *
     * 상품별 매출 조회.
     * 상품 분류를 기준으로 정렬한다.
     */
    @Operation(summary = "상품별 매출 조회",
            description = "상품 분류를 기준으로 정렬"
    )
    @DefaultApiResponse
    @SwaggerAuthentication
    @Parameters({
            @Parameter(name = "page",
                    description = "페이지 번호",
                    example = "1",
                    in = ParameterIn.QUERY
            ),
            @Parameter(name = "keyword",
                    description = "검색어. 상품명",
                    example = "DummyOUTER",
                    in = ParameterIn.QUERY
            )
    })
    @GetMapping("/sales/product")
    public ResponseEntity<PagingResponseDTO<AdminProductSalesListDTO>> getProductSales(@ParameterObject @Valid ListRequestDTO requestDTO) {

        AdminSalesPageDTO pageDTO = AdminSalesPageDTO.fromRequestDTO(requestDTO);

        Page<AdminProductSalesListDTO> responseDTO = salesReadeUseCase.getProductSalesList(pageDTO);
        return pagingResponseMapper.toPagingResponse(responseDTO);
    }

    /**
     *
     * @param productId
     *
     * 상품 매출 상세 내역 조회
     *
     */
    @Operation(summary = "상품 매출의 상세 내역 조회")
    @DefaultApiResponse
    @SwaggerAuthentication
    @Parameter(name = "productId",
            description = "상품 아이디",
            example = "BAGS20210629134401",
            required = true,
            in = ParameterIn.PATH
    )
    @GetMapping("/sales/product/detail/{productId}")
    public ResponseEntity<AdminProductSalesDetailDTO> getProductSales(@PathVariable(name = "productId") String productId) {

        AdminProductSalesDetailDTO responseDTO = salesReadeUseCase.getProductSalesDetail(productId);

        return ResponseEntity.ok(responseDTO);
    }
}
