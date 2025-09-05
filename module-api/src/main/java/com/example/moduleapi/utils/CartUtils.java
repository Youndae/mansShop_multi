package com.example.moduleapi.utils;

import com.example.moduleauthapi.service.JWTTokenProvider;
import com.example.modulecommon.customException.CustomAccessDeniedException;
import com.example.modulecommon.model.enumuration.ErrorCode;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class CartUtils {

    private final JWTTokenProvider jwtTokenProvider;

    @Value("#{jwt['cookie.cart.header']}")
    private String cartCookieHeader;

    @Value("#{jwt['cookie.cart.expirationDay']}")
    private long cartCookieExpirationDay;

    public Cookie getCartCookie(HttpServletRequest request) {
        return WebUtils.getCookie(request, cartCookieHeader);
    }

    /**
     *
     * @param cartCookie
     * @param userId
     *
     * CartList 조회 같은 경우 둘다 null이어도 괜찮지만,
     * 그 외 요청의 경우 허용되지 않음.
     * 둘다 null이라는건 정상적인 클라이언트 요청이 아니기 때문에 AccessDenied 발생
     */
    public void exceptionAfterValidateCartMemberDTO(Cookie cartCookie, String userId) {
        if(!validateCartMemberDTO(cartCookie, userId))
            throw new CustomAccessDeniedException(ErrorCode.FORBIDDEN, ErrorCode.FORBIDDEN.getMessage());
    }

    public boolean validateCartMemberDTO(Cookie cartCookie, String userId) {
        if(cartCookie == null && userId == null)
            return false;

        return true;
    }

    /**
     * 비회원의 CartCookie 생성 및 갱신.
     * 비회원이 장바구니 기능 중 어떤것을 사용했을 때
     * 기간을 갱신해주기 위해 무조건 적인 ResponseCookie 생성 및 반환으로 만료일을 갱신
     */
    public void setCartResponseCookie(String cookieValue, HttpServletResponse response) {
        jwtTokenProvider.setTokenCookie(
                cartCookieHeader,
                cookieValue,
                Duration.ofDays(cartCookieExpirationDay),
                response
        );
    }

    public void deleteCartCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(cartCookieHeader, null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
    }
}
