package com.example.modulecommon.fixture;

import com.example.modulecommon.model.entity.*;

import java.util.ArrayList;
import java.util.List;

public class ProductFixture {

    public static List<Product> createProductFixtureList(int count, Classification classification) {
        List<Product> result = new ArrayList<>();

        for(int i = 1; i <= count; i++) {
            Product product = createProduct(i, classification);
            createSaveOptionAndThumbnailAndInfoImage(product, i * 3);

            result.add(product);
        }

        return result;
    }

    private static Product createProduct(int i, Classification classification) {
        return Product.builder()
                .id("testProduct" + i)
                .classification(classification)
                .productName("testProduct" + i + "Name")
                .productPrice(i * 1000)
                .thumbnail("testProductThumbnail.jpg")
                .isOpen(true)
                .productSalesQuantity(i * 10L)
                .productDiscount(i < 10 ? 0 : i / 2)
                .build();
    }

    private static void createSaveOptionAndThumbnailAndInfoImage(Product product, int count) {
        List<ProductOption> options = createSaveProductOption(count);
        List<ProductThumbnail> thumbnails = createSaveProductThumbnail(product.getProductName(), count);
        List<ProductInfoImage> infoImages = createSaveProductInfoImage(product.getProductName(), count);

        options.forEach(product::addProductOption);
        thumbnails.forEach(product::addProductThumbnail);
        infoImages.forEach(product::addProductInfoImage);
    }

    private static List<ProductOption> createSaveProductOption(int count) {
        List<ProductOption> options = new ArrayList<>();

        for(int i = count - 3; i < count; i++) {
            options.add(
                    ProductOption.builder()
                            .size("size" + i)
                            .color("color" + i)
                            .stock(i * 10)
                            .isOpen(i % 2 != 0)
                            .build()
            );
        }

        return options;
    }

    private static List<ProductThumbnail> createSaveProductThumbnail(String productName, int count) {
        List<ProductThumbnail> thumbnails = new ArrayList<>();

        for(int i = count - 3; i < count; i++) {
            thumbnails.add(
                    ProductThumbnail.builder()
                            .imageName(productName + "thumbnail" + i + ".jpg")
                            .build()
            );
        }

        return thumbnails;
    }

    private static List<ProductInfoImage> createSaveProductInfoImage(String productName, int count) {
        List<ProductInfoImage> infoImages = new ArrayList<>();

        for(int i = count - 3; i < count; i++) {
            infoImages.add(
                    ProductInfoImage.builder()
                            .imageName(productName + "infoImage" + i + ".jpg")
                            .build()
            );
        }

        return infoImages;
    }

    public static List<Product> createAdditionalProduct (int startIdx, int endIdx, Classification classification) {
        List<Product> result = new ArrayList<>();
        for(int i = startIdx; i < startIdx + endIdx; i++) {
            Product product = createProduct(i, classification);
            createSaveOptionAndThumbnailAndInfoImage(product, (i - (startIdx - 1)) * 3);

            result.add(product);
        }

        return result;
    }

    public static List<Product> createDefaultProductByOUTER(int count) {
        List<Product> result = new ArrayList<>();
        for(int i = 1; i <= count; i++) {
            Product product = createProduct(i, Classification.builder().id("OUTER").build());
            createSaveOptionAndThumbnailAndInfoImage(product, (i * 3));

            result.add(product);
        }

        return result;
    }
}
