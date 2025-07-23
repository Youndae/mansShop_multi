package com.example.modulecommon.utils;

public class ProductDiscountUtils {

    public static int calcDiscountPrice(int price, int discount) {
        return (int) (price * (1 - ((double) discount / 100)));
    }
}
