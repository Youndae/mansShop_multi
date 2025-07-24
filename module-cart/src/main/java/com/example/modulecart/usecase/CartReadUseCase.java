package com.example.modulecart.usecase;

import com.example.modulecart.model.dto.business.CartMemberDTO;
import com.example.modulecart.model.dto.out.CartDetailDTO;
import com.example.modulecart.service.CartDataService;
import com.example.modulecart.service.CartDomainService;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartReadUseCase {

    private final CartDomainService cartDomainService;

    private final CartDataService cartDataService;

    /**
     *
     * @param cartCookie
     * @param userId
     *
     * 회원의 장바구니 리스트 조회
     * 데이터가 존재하지 않는 경우 오류를 발생시킬 것이 아니라 Null을 반환해 상품이 없다는 문구를 출력하도록 처리.
     */
    public List<CartDetailDTO> getCartList(Cookie cartCookie, String userId) {
        CartMemberDTO cartMemberDTO = cartDomainService.setCartMemberDTO(cartCookie, userId);

        Long userCartId = cartDataService.getUserCartId(cartMemberDTO);

        if(userCartId == null)
            return Collections.emptyList();

        return cartDataService.getCartList(userCartId);
    }
}
