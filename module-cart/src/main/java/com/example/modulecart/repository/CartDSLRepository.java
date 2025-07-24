package com.example.modulecart.repository;

import com.example.modulecart.model.dto.business.CartMemberDTO;
import com.example.modulecommon.model.entity.Cart;

public interface CartDSLRepository {

    Long findIdByUserId(CartMemberDTO cartMemberDTO);

    Cart findByUserIdAndCookieValue(CartMemberDTO cartMemberDTO);
}
