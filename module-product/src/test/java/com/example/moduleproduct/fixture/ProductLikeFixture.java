package com.example.moduleproduct.fixture;

import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.entity.Product;
import com.example.modulecommon.model.entity.ProductLike;

public class ProductLikeFixture {

    /**
     * Create ProductLike Entity
     * 관심상품 등록 여부 체크를 위해 공통 사용되는 Product를 관심상품으로 등록하기 위함.
     *
     * @return
     */
    public static ProductLike createSuccessLikeCountEntity() {
        Member member = MemberFixture.createMember();
        Product product = ProductFixture.createOneProductEntity();

        return ProductLike.builder()
                .member(member)
                .product(product)
                .build();
    }

    /**
     * Create ProductLike to Count Fail
     * 관심상품으로 등록하지 않은 상품을 담은 ProductLike Entity 반환.
     * 해당 상품이 관심상품으로 등록되지 않았다는 테스트를 보기 위함.
     * @return
     */
    public static ProductLike createFailLikeCountEntity() {
        Member member = MemberFixture.createMember();
        Product product = Product.builder()
                .id("testFailProductId")
                .classification(null)
                .productName("DummyProduct")
                .productPrice(1000)
                .thumbnail("productThumbnail")
                .isOpen(true)
                .productSales(0L)
                .productDiscount(0)
                .build();

        return ProductLike.builder()
                .member(member)
                .product(product)
                .build();
    }
}
