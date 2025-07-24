package com.example.modulecommon.fixture;

import com.example.modulecommon.model.entity.Cart;
import com.example.modulecommon.model.entity.CartDetail;
import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.entity.ProductOption;
import org.springframework.boot.web.server.Cookie;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class CartFixture {
    private static int randomInt() {
        Random ran = new Random();

        return ran.nextInt(4) + 1;
    }

    public static List<Cart> createDefaultMemberCart(List<Member> members, List<ProductOption> options) {
        List<Cart> result = new ArrayList<>();

        for(Member m : members) {
            Cart cart = createCart(m, null);

            for(ProductOption option : options) {
                CartDetail detail = createCartDetail(option, randomInt());
                cart.addCartDetail(detail);
            }

            result.add(cart);
        }

        return result;
    }

    public static Cart createSaveAnonymousCart(ProductOption option, Member anonymous, String cookieValue) {
        Cart cart = createCart(anonymous, cookieValue);
        CartDetail cartDetail = createCartDetail(option, 2);
        cart.addCartDetail(cartDetail);

        return cart;
    }

    public static Cart createSaveAnonymousCartMultipleOptions(List<ProductOption> options, Member anonymous, String cookieValue) {
        Cart cart = createCart(anonymous, cookieValue);

        for(ProductOption option : options) {
            CartDetail detail = createCartDetail(option, randomInt());
            cart.addCartDetail(detail);
        }

        return cart;
    }

    private static CartDetail createCartDetail(ProductOption option, int count) {
        return CartDetail.builder()
                .productOption(option)
                .cartCount(count)
                .build();
    }

    private static String createCookieValue() {
        return UUID.randomUUID().toString();
    }

    private static Cart createCart(Member member, String cookieId) {
        return Cart.builder()
                .member(member)
                .cookieId(cookieId)
                .build();
    }
}
