package com.example.moduleapi.controller.user;

import com.example.moduleapi.annotation.swagger.DefaultApiResponse;
import com.example.moduleapi.annotation.swagger.SwaggerAuthentication;
import com.example.moduleapi.service.PrincipalService;
import com.example.moduleapi.utils.CartUtils;
import com.example.modulecart.model.dto.business.CartMemberDTO;
import com.example.modulecart.model.dto.in.AddCartDTO;
import com.example.modulecart.model.dto.out.CartCookieResponseDTO;
import com.example.modulecart.model.dto.out.CartDetailDTO;
import com.example.modulecart.usecase.CartReadUseCase;
import com.example.modulecart.usecase.CartWriteUseCase;
import com.example.modulecommon.model.dto.response.ResponseMessageDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Collections;
import java.util.List;

@Tag(name = "Cart Controller")
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Slf4j
public class CartController {

    private final CartReadUseCase cartReadUseCase;

    private final CartWriteUseCase cartWriteUseCase;

    private final CartUtils cartUtils;

    private final PrincipalService principalService;

    /**
     *
     * @param request
     * @param principal
     *
     * 사용자의 장바구니 데이터 조회
     */
    @Operation(summary = "장바구니 데이터 조회")
    @DefaultApiResponse
    @SwaggerAuthentication
    @Parameter(
            name = "cartCookie",
            description = "비회원인 경우 갖게 되는 장바구니 cookieId. 비회원의 경우 JWT가 아닌 이 쿠키값이 필요.",
            in = ParameterIn.COOKIE
    )
    @GetMapping("/")
    public ResponseEntity<List<CartDetailDTO>> getCartList(HttpServletRequest request,
                                                           Principal principal) {
        Cookie cartCookie = cartUtils.getCartCookie(request);
        String userId = principalService.extractUserIdIfExist(principal);
        List<CartDetailDTO> responseDTO = null;
        if(!cartUtils.validateCartMemberDTO(cartCookie, userId))
            responseDTO = Collections.emptyList();
        else
            responseDTO = cartReadUseCase.getCartList(cartCookie, userId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(responseDTO);
    }

    /**
     *
     * @param addList
     * @param request
     * @param response
     * @param principal
     *
     * 상품 상세 페이지에서 장바구니 담기
     */
    @Operation(summary = "상품 상세 페이지에서 장바구니에 담기 요청")
    @DefaultApiResponse
    @SwaggerAuthentication
    @Parameter(
            name = "cartCookie",
            description = "비회원인 경우 갖게 되는 장바구니 cookieId. 비회원의 경우 JWT가 아닌 이 쿠키값이 필요. 생략하면 새로운 Cookie 발급이 되면서 새로운 장바구니에 상품이 담김.",
            in = ParameterIn.COOKIE
    )
    @PostMapping("/")
    public ResponseEntity<ResponseMessageDTO> addCart(@RequestBody List<AddCartDTO> addList,
                                                      HttpServletRequest request,
                                                      HttpServletResponse response,
                                                      Principal principal) {
        Cookie cartCookie = cartUtils.getCartCookie(request);
        String userId = principalService.extractUserIdIfExist(principal);
        CartCookieResponseDTO responseDTO = cartWriteUseCase.addProductForCart(addList, cartCookie, userId);
        if(responseDTO.cookieValue() != null)
            cartUtils.setCartResponseCookie(responseDTO.cookieValue(), response);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ResponseMessageDTO(responseDTO.responseMessage()));
    }

    /**
     *
     * @param cartDetailId
     * @param request
     * @param principal
     *
     * 장바구니내 상품 수량 증가
     */
    @Operation(summary = "장바구니 상품 수량 증가")
    @DefaultApiResponse
    @SwaggerAuthentication
    @Parameter(
            name = "cartCookie",
            description = "비회원인 경우 갖게 되는 장바구니 cookieId. 비회원의 경우 JWT가 아닌 이 쿠키값이 필요.",
            in = ParameterIn.COOKIE
    )
    @Parameter(name = "cartDetailId",
            description = "장바구니 상세 데이터 아이디",
            example = "1",
            required = true,
            in = ParameterIn.PATH
    )
    @PatchMapping("/count-up/{cartDetailId}")
    public ResponseEntity<ResponseMessageDTO> cartCountUp(@PathVariable(name = "cartDetailId") long cartDetailId,
                                                          HttpServletRequest request,
                                                          HttpServletResponse response,
                                                          Principal principal) {
        Cookie cartCookie = cartUtils.getCartCookie(request);
        String userId = principalService.extractUserIdIfExist(principal);
        cartUtils.exceptionAfterValidateCartMemberDTO(cartCookie, userId);
        CartCookieResponseDTO responseDTO = cartWriteUseCase.cartCountUp(cartDetailId, cartCookie, userId);
        if(cartCookie != null)
            cartUtils.setCartResponseCookie(responseDTO.cookieValue(), response);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ResponseMessageDTO(responseDTO.responseMessage()));
    }

    /**
     *
     * @param cartDetailId
     * @param request
     * @param principal
     *
     * 장바구니내 상품 수량 감소
     */
    @Operation(summary = "장바구니 상품 수량 감소")
    @DefaultApiResponse
    @SwaggerAuthentication
    @Parameter(
            name = "cartCookie",
            description = "비회원인 경우 갖게 되는 장바구니 cookieId. 비회원의 경우 JWT가 아닌 이 쿠키값이 필요.",
            in = ParameterIn.COOKIE
    )
    @Parameter(name = "cartDetailId",
            description = "장바구니 상세 데이터 아이디",
            example = "1",
            required = true,
            in = ParameterIn.PATH
    )
    @PatchMapping("/count-down/{cartDetailId}")
    public ResponseEntity<ResponseMessageDTO> cartCountDown(@PathVariable(name = "cartDetailId") long cartDetailId,
                                                            HttpServletRequest request,
                                                            HttpServletResponse response,
                                                            Principal principal) {
        Cookie cartCookie = cartUtils.getCartCookie(request);
        String userId = principalService.extractUserIdIfExist(principal);
        cartUtils.exceptionAfterValidateCartMemberDTO(cartCookie, userId);
        CartCookieResponseDTO responseDTO = cartWriteUseCase.cartCountDown(cartDetailId, cartCookie, userId);
        if(cartCookie != null)
            cartUtils.setCartResponseCookie(responseDTO.cookieValue(), response);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ResponseMessageDTO(responseDTO.responseMessage()));
    }

    /**
     *
     * @param deleteSelectId
     * @param request
     * @param principal
     *
     * 장바구니 선택 상품 삭제
     */
    @Operation(summary = "장바구니 선택 상품 삭제")
    @DefaultApiResponse
    @SwaggerAuthentication
    @Parameter(
            name = "cartCookie",
            description = "비회원인 경우 갖게 되는 장바구니 cookieId. 비회원의 경우 JWT가 아닌 이 쿠키값이 필요.",
            in = ParameterIn.COOKIE
    )
    @DeleteMapping("/select")
    public ResponseEntity<ResponseMessageDTO> deleteSelectCart(@RequestBody List<Long> deleteSelectId,
                                                               HttpServletRequest request,
                                                               HttpServletResponse response,
                                                               Principal principal) {
        Cookie cartCookie = cartUtils.getCartCookie(request);
        String userId = principalService.extractUserIdIfExist(principal);
        cartUtils.exceptionAfterValidateCartMemberDTO(cartCookie, userId);
        CartCookieResponseDTO responseDTO = cartWriteUseCase.deleteSelectProductFromCart(deleteSelectId, cartCookie, userId);
        if(cartCookie != null && responseDTO.cookieValue() == null)
            cartUtils.deleteCartCookie(response);
        else if(cartCookie != null)
            cartUtils.setCartResponseCookie(responseDTO.cookieValue(), response);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ResponseMessageDTO(responseDTO.responseMessage()));
    }

    /**
     *
     * @param principal
     * @param request
     * @param response
     *
     * 장바구니 모든 상품 삭제
     */
    @Operation(summary = "장바구니 상품 전체 삭제")
    @DefaultApiResponse
    @SwaggerAuthentication
    @Parameter(
            name = "cartCookie",
            description = "비회원인 경우 갖게 되는 장바구니 cookieId. 비회원의 경우 JWT가 아닌 이 쿠키값이 필요.",
            in = ParameterIn.COOKIE
    )
    @DeleteMapping("/all")
    public ResponseEntity<ResponseMessageDTO> deleteCart(Principal principal,
                                                         HttpServletRequest request,
                                                         HttpServletResponse response) {
        Cookie cartCookie = cartUtils.getCartCookie(request);
        String userId = principalService.extractUserIdIfExist(principal);
        cartUtils.exceptionAfterValidateCartMemberDTO(cartCookie, userId);
        String responseMessage = cartWriteUseCase.deleteAllProductFromCart(cartCookie, userId);
        if(cartCookie != null)
            cartUtils.deleteCartCookie(response);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ResponseMessageDTO(responseMessage));
    }
}
