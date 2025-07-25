package com.example.modulecart.service;

import com.example.modulecart.model.dto.business.CartMemberDTO;
import com.example.modulecart.repository.CartDetailRepository;
import com.example.modulecart.repository.CartRepository;
import com.example.modulecommon.model.entity.Cart;
import com.example.modulecommon.model.entity.CartDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartReader {

    private final CartRepository cartRepository;

    private final CartDetailRepository cartDetailRepository;

    public Long findIdByUserId(CartMemberDTO cartMemberDTO) {
        return cartRepository.findIdByUserId(cartMemberDTO);
    }

    public List<CartDetail> findAllCartDetailByCartId(long cartId) {
        return cartDetailRepository.findAllCartDetailByCartId(cartId);
    }

    public List<CartDetail> findAllCartDetailByIds(List<Long> cartDetailIds) {
        return cartDetailRepository.findAllById(cartDetailIds);
    }

    public Cart findCartById(Long cartId) {
        return cartRepository.findById(cartId).orElseThrow(IllegalArgumentException::new);
    }

    public List<Cart> findAll() {
        return cartRepository.findAll();
    }
}
