package com.example.moduleproduct.fixture;

import com.example.modulecommon.model.entity.*;
import com.example.modulecommon.model.enumuration.OAuthProvider;

import java.util.ArrayList;
import java.util.List;

public class ProductFixture {

    public static ProductLike createSuccessLikeCountEntity() {
        Member member = createMember();
        Product product = createProduct();

        return ProductLike.builder()
                .member(member)
                .product(product)
                .build();
    }

    public static ProductLike createFailLikeCountEntity() {
        Member member = createMember();
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

    public static Member createMember() {
        Member member = Member.builder()
                .userId("testUser1")
                .userName("testUserName")
                .provider(OAuthProvider.LOCAL.getKey())
                .build();

        member.addMemberAuth(new Auth().toMemberAuth());

        return member;
    }

    public static Product createProduct() {
        Product product = Product.builder()
                .id("testProductId")
                .classification(null)
                .productName("DummyProduct")
                .productPrice(1000)
                .thumbnail("productThumbnail")
                .isOpen(true)
                .productSales(0L)
                .productDiscount(0)
                .build();

        return product;
    }

    public static Product createProductAndImage() {
        Product product = createProduct();
        List<ProductThumbnail> thumbnailSet = createProductThumbnailList();
        List<ProductInfoImage> infoImageSet = createProductInfoImageList();

        thumbnailSet.forEach(product::addProductThumbnail);
        infoImageSet.forEach(product::addProductInfoImage);

        return product;
    }

    public static List<ProductThumbnail> createProductThumbnailList() {
        List<String> imageNameList = List.of("thumb1", "thumb2", "thumb3", "thumb4");

        return imageNameList.stream()
                            .map(ProductFixture::createProductThumbnail)
                            .toList();
    }

    public static List<ProductInfoImage> createProductInfoImageList() {
        List<String> imageNameList = List.of("info1", "info2", "info3");

        return imageNameList.stream()
                            .map(ProductFixture::createProductInfoImage)
                            .toList();
    }

    public static ProductThumbnail createProductThumbnail(String imageName) {
        return ProductThumbnail.builder()
                .imageName(imageName)
                .build();
    }

    public static ProductInfoImage createProductInfoImage(String imageName) {
        return ProductInfoImage.builder()
                .imageName(imageName)
                .build();
    }

    public static Product createProductAndOption() {
        Product product = createProduct();
        List<ProductOption> options = createProductOptionList();
        options.forEach(product::addProductOption);

        return product;
    }

    public static List<ProductOption> createProductOptionList() {
        List<ProductOption> result = new ArrayList<>();
        for(int i = 0; i < 5; i++)
            result.add(createProductOption(i));

        return result;
    }

    public static ProductOption createProductOption(int i) {
        return ProductOption.builder()
                .size("testSize" + i)
                .color("testColor" + i)
                .stock(i)
                .isOpen(true)
                .build();
    }
}
