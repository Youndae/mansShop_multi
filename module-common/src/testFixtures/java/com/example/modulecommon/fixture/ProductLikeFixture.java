package com.example.modulecommon.fixture;

import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.entity.Product;
import com.example.modulecommon.model.entity.ProductLike;

import java.util.ArrayList;
import java.util.List;

public class ProductLikeFixture {

    public static List<ProductLike> createDefaultProductLike(List<Member> members, List<Product> products) {
        List<ProductLike> result = new ArrayList<>();

        for(Member m : members) {
            for(Product p : products) {
                result.add(
                        ProductLike.builder()
                                .member(m)
                                .product(p)
                                .build()
                );
            }
        }

        return result;
    }
}
