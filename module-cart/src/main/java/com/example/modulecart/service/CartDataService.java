package com.example.modulecart.service;

import com.example.modulecart.model.dto.business.CartMemberDTO;
import com.example.modulecart.model.dto.out.CartDetailDTO;
import com.example.modulecart.repository.CartDetailRepository;
import com.example.modulecart.repository.CartRepository;
import com.example.modulecommon.model.entity.Cart;
import com.example.modulecommon.model.entity.CartDetail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartDataService {

    private final CartRepository cartRepository;

    private final CartDetailRepository cartDetailRepository;

    public Long getUserCartId(CartMemberDTO cartMemberDTO) {
        return cartRepository.findIdByUserId(cartMemberDTO);
    }

    public List<CartDetailDTO> getCartList(long userCartId) {

        return cartDetailRepository.findAllByCartId(userCartId);
    }

    public Cart getCartData(CartMemberDTO cartMemberDTO) {
        return cartRepository.findByUserIdAndCookieValue(cartMemberDTO);
    }

    public List<CartDetail> findAllUserCartDetails(Long cartId, List<Long> addOptionIds) {
        return cartDetailRepository.findAllCartDetailByCartIdAndOptionIds(cartId, addOptionIds);
    }

    public void saveCart(Cart cart) {
        cartRepository.save(cart);
    }

    public CartDetail getCartDetail(long cartDetailId) {
        return cartDetailRepository.findById(cartDetailId).orElseThrow(IllegalArgumentException::new);
    }

    public void saveCartDetail(CartDetail cartDetail) {
        cartDetailRepository.save(cartDetail);
    }

    public List<Long> getAllUserCartDetailId(Long cartId) {
        return cartDetailRepository.findAllIdByCartId(cartId);
    }

    public void deleteUserCart(Long cartId) {
        cartRepository.deleteById(cartId);
    }

    public void deleteSelectProductFromCartDetail(List<Long> deleteSelectIds) {
        cartDetailRepository.deleteAllByIdInBatch(deleteSelectIds);
    }
}
