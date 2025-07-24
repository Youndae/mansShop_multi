package com.example.modulecart.repository;

import com.example.modulecart.model.dto.business.CartMemberDTO;
import com.example.modulecommon.model.entity.Cart;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import static com.example.modulecommon.model.entity.QCart.cart;

@Repository
@RequiredArgsConstructor
public class CartDSLRepositoryImpl implements CartDSLRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Long findIdByUserId(CartMemberDTO cartMemberDTO) {
        return jpaQueryFactory.select(cart.id)
                .from(cart)
                .where(userType(cartMemberDTO))
                .fetchOne();
    }

    @Override
    public Cart findByUserIdAndCookieValue(CartMemberDTO cartMemberDTO) {
        return jpaQueryFactory.selectFrom(cart)
                .where(userType(cartMemberDTO))
                .fetchOne();
    }

    private BooleanExpression userType(CartMemberDTO cartMemberDTO) {
        if(cartMemberDTO.cartCookieValue() == null)
            return cart.member.userId.eq(cartMemberDTO.uid());
        else
            return cart.cookieId.eq(cartMemberDTO.cartCookieValue()).and(cart.member.userId.eq(cartMemberDTO.uid()));
    }
}
