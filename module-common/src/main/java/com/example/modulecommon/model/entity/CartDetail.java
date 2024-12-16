package com.example.modulecommon.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CartDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cartId")
    private Cart cart;

    @ManyToOne
    @JoinColumn(name = "productOptionId")
    private ProductOption productOption;

    private int cartCount;

    private int cartPrice;

    public void setCart(Cart cart) {
        this.cart = cart;
    }

    public void setCartCount(int cartCount) {
        this.cartCount = cartCount;
    }

    public void setCartPrice(int cartPrice) {
        this.cartPrice = cartPrice;
    }
}
