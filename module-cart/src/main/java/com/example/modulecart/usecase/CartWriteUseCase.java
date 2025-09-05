package com.example.modulecart.usecase;


import com.example.modulecart.model.dto.business.CartMemberDTO;
import com.example.modulecart.model.dto.in.AddCartDTO;
import com.example.modulecart.model.dto.out.CartCookieResponseDTO;
import com.example.modulecart.service.CartDataService;
import com.example.modulecart.service.CartDomainService;
import com.example.modulecommon.customException.CustomNotFoundException;
import com.example.modulecommon.model.entity.Cart;
import com.example.modulecommon.model.entity.CartDetail;
import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.enumuration.ErrorCode;
import com.example.modulecommon.model.enumuration.Result;
import com.example.moduleuser.service.UserDataService;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartWriteUseCase {

    /**
     * 장바구니 담기 요청 ( 회원 비회원. 비회원이 Cookie가 없는 경우 고려)
     *
     * 장바구니 상품 수량 증가
     * 장바구니 상품 수량 감소
     * 장바구니 선택 상품 삭제
     * 장바구니 전체 상품 삭제
     */

    private final CartDataService cartDataService;

    private final CartDomainService cartDomainService;

    private final UserDataService userDataService;

    @Transactional(rollbackFor = Exception.class)
    public CartCookieResponseDTO addProductForCart(List<AddCartDTO> addList,
                                  Cookie cartCookie,
                                  String userId) {
        CartMemberDTO cartMemberDTO = cartDomainService.setCartMemberDTO(cartCookie, userId);
        Cart userCart = cartDataService.getCartData(cartMemberDTO);
        List<CartDetail> savedDetails = new ArrayList<>();

        if(userCart == null) {
            Member member = userDataService.getMemberByUserIdOrElseNull(cartMemberDTO.uid());
            if(member == null) {
                log.info("CartWriteUseCase.addProductForCart :: member is null");
                throw new IllegalArgumentException();
            }

            userCart = cartDomainService.buildCart(member, cartMemberDTO.cartCookieValue());
        }else {
            List<Long> addOptionIds = cartDomainService.mapAddCartOptionIds(addList);
            savedDetails = cartDataService.findAllUserCartDetails(userCart.getId(), addOptionIds);
        }

        cartDomainService.mapCartAndCartDetails(addList, userCart, savedDetails);
        cartDataService.saveCart(userCart);

        return new CartCookieResponseDTO(Result.OK.getResultKey(), cartMemberDTO.cartCookieValue());
    }

    public CartCookieResponseDTO cartCountUp(long cartDetailId, Cookie cartCookie, String userId) {
        return patchProductCountFromCart(cartDetailId, cartCookie, userId, "up");
    }

    public CartCookieResponseDTO cartCountDown(long cartDetailId, Cookie cartCookie, String userId) {
        return patchProductCountFromCart(cartDetailId, cartCookie, userId, "down");
    }

    private CartCookieResponseDTO patchProductCountFromCart(long cartDetailId, Cookie cartCookie, String userId, String countType) {
        CartMemberDTO cartMemberDTO = cartDomainService.setCartMemberDTO(cartCookie, userId);
        Cart userCart = cartDataService.getCartData(cartMemberDTO);
        if(userCart == null){
            log.info("CartWriteUseCase.patchProductCountFromCart :: userCart is null");
            throw new CustomNotFoundException(ErrorCode.BAD_REQUEST, ErrorCode.BAD_REQUEST.getMessage());
        }

        CartDetail cartDetail = cartDataService.getCartDetail(cartDetailId);

        if(!userCart.getId().equals(cartDetail.getCart().getId()))
            throw new IllegalArgumentException("cart and cartDetail ids do not match");

        cartDetail.countUpDown(countType);

        cartDataService.saveCartDetail(cartDetail);

        return new CartCookieResponseDTO(Result.OK.getResultKey(), cartMemberDTO.cartCookieValue());
    }

    public CartCookieResponseDTO deleteSelectProductFromCart(List<Long> deleteSelectIds, Cookie cartCookie, String userId) {
        CartMemberDTO cartMemberDTO = cartDomainService.setCartMemberDTO(cartCookie, userId);
        Long cartId = cartDataService.getUserCartId(cartMemberDTO);

        if(cartId == null){
            log.info("CartWriteUseCase.deleteSelectProductFromCart :: cartId is null");
            throw new CustomNotFoundException(ErrorCode.BAD_REQUEST, ErrorCode.BAD_REQUEST.getMessage());
        }

        List<Long> userCartDetailIds = cartDataService.getAllUserCartDetailId(cartId);

        cartDomainService.validateDeleteIdsFromUserDetailIds(deleteSelectIds, userCartDetailIds);
        String cookieValue = cartMemberDTO.cartCookieValue();
        if(deleteSelectIds.size() == userCartDetailIds.size()) {
            cartDataService.deleteUserCart(cartId);
            cookieValue = null;
        }else
            cartDataService.deleteSelectProductFromCartDetail(deleteSelectIds);

        return new CartCookieResponseDTO(Result.OK.getResultKey(), cookieValue);
    }


    public void deleteAllProductFromCart(Cookie cartCookie, String userId) {
        CartMemberDTO cartMemberDTO = cartDomainService.setCartMemberDTO(cartCookie, userId);
        Long cartId = cartDataService.getUserCartId(cartMemberDTO);

        if(cartId == null) {
            log.info("CartWriteUseCase.deleteAllProductFromCart :: cartId is null");
            throw new CustomNotFoundException(ErrorCode.BAD_REQUEST, ErrorCode.BAD_REQUEST.getMessage());
        }

        cartDataService.deleteUserCart(cartId);
    }
}
