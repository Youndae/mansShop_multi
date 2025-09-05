package com.example.moduleapi.controller.order;

import com.example.moduleapi.annotation.swagger.DefaultApiResponse;
import com.example.moduleapi.annotation.swagger.SwaggerAuthentication;
import com.example.moduleapi.service.PrincipalService;
import com.example.moduleapi.utils.CartUtils;
import com.example.moduleapi.utils.OrderTokenUtils;
import com.example.modulecommon.customException.CustomNotFoundException;
import com.example.modulecommon.model.enumuration.ErrorCode;
import com.example.modulecommon.model.enumuration.Role;
import com.example.moduleorder.model.dto.in.OrderProductRequestDTO;
import com.example.moduleorder.model.dto.in.PaymentDTO;
import com.example.moduleorder.model.dto.out.OrderDataResponseDTO;
import com.example.moduleorder.usecase.OrderWriteUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@Tag(name = "Order Controller")
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderWriteUseCase orderWriteUseCase;

    private final CartUtils cartUtils;

    private final OrderTokenUtils orderTokenUtils;

    private final PrincipalService principalService;

    /**
     *
     * @param paymentDTO
     * @param request
     * @param principal
     *
     * 결제 완료 이후 주문 정보 처리
     */
    @Operation(summary = "결제 완료 이후 주문 데이터 처리")
    @DefaultApiResponse
    @SwaggerAuthentication
    @Parameter(
            name = "cartCookie",
            description = "비회원이면서 장바구니를 통한 결제를 한 경우 JWT가 아닌 이 쿠키값이 필요.",
            in = ParameterIn.COOKIE
    )
    @PostMapping("/")
    public ResponseEntity<Void> payment(@RequestBody PaymentDTO paymentDTO,
                                                      HttpServletRequest request,
                                                      HttpServletResponse response,
                                                      Principal principal) {
        Cookie cartCookie = cartUtils.getCartCookie(request);
        Cookie orderTokenCookie = orderTokenUtils.getOrderTokenCookie(request);
        String userId = principalService.extractUserIdIfExist(principal);

        orderWriteUseCase.orderDataProcessAfterPayment(paymentDTO, cartCookie, userId, orderTokenCookie, response);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     *
     * @param requestDTO
     *
     * 상품 상세 페이지에서 주문 요청 시 해당 상품 데이터를 조회해서 반환
     */
    @Operation(summary = "상품 상세 페이지에서 결제 요청 시 상품 결제 정보 반환")
    @DefaultApiResponse
    @SwaggerAuthentication
    @PostMapping("/product")
    public ResponseEntity<OrderDataResponseDTO> orderProduct(@Schema(name = "주문 요청 상품 데이터", type = "array") @RequestBody List<OrderProductRequestDTO> requestDTO,
                                                             Principal principal,
                                                             HttpServletRequest request,
                                                             HttpServletResponse response){
        Cookie orderTokenCookie = orderTokenUtils.getOrderTokenCookie(request);
        String userId = principalService.extractUserIdIfExist(principal);

        OrderDataResponseDTO responseDTO = orderWriteUseCase.getProductOrderData(requestDTO, orderTokenCookie, response, userId);

        return ResponseEntity.ok(responseDTO);
    }

    @Operation(summary = "장바구니 페이지에서 결제 요청 시 상품 결제 정보 반환")
    @DefaultApiResponse
    @SwaggerAuthentication
    @Parameter(
            name = "cartCookie",
            description = "비회원인 경우 갖게 되는 장바구니 cookieId. 비회원의 경우 JWT가 아닌 이 쿠키값이 필요.",
            in = ParameterIn.COOKIE
    )
    @PostMapping("/cart")
    public ResponseEntity<OrderDataResponseDTO> orderCart(@Schema(name = "장바구니 상세 데이터 아이디 리스트", type = "array")
                                                          @RequestBody List<Long> cartDetailIds,
                                                          HttpServletRequest request,
                                                          HttpServletResponse response,
                                                          Principal principal) {
        Cookie orderTokenCookie = orderTokenUtils.getOrderTokenCookie(request);
        Cookie cartCookie = cartUtils.getCartCookie(request);
        String userId = principalService.extractUserIdIfExist(principal);

        if(cartCookie == null && userId == null) {
            log.error("OrderController.orderCart :: cartCookie and UserId is null");
            throw new CustomNotFoundException(ErrorCode.BAD_REQUEST, ErrorCode.BAD_REQUEST.getMessage());
        }

        OrderDataResponseDTO responseDTO = orderWriteUseCase.getCartOrderData(cartDetailIds, orderTokenCookie, cartCookie, userId, response);

        return ResponseEntity.ok(responseDTO);
    }

    @Operation(summary = "결제 API 호출 이전 주문 데이터 검증", hidden = true)
    @PostMapping("/validate")
    public ResponseEntity<Void> validateOrder(@RequestBody OrderDataResponseDTO requestDTO,
                                                            Principal principal,
                                                            HttpServletRequest request,
                                                            HttpServletResponse response) {
        Cookie orderTokenCookie = orderTokenUtils.getOrderTokenCookie(request);
        String userId = principalService.extractUserIdIfExist(principal);

        if(userId == null)
            userId = Role.ANONYMOUS.getRole();

        orderWriteUseCase.validateOrderData(requestDTO, userId, orderTokenCookie, response);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
