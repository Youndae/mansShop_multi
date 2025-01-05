package com.example.moduleproduct.fixture;

import com.example.modulecommon.model.entity.*;
import com.example.modulecommon.model.enumuration.OAuthProvider;
import com.example.modulecommon.model.enumuration.PageAmount;
import com.example.moduleproduct.model.dto.page.ProductPageDTO;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class ProductFixture {

    /**
     *
     * Create ProductList
     * OUTER 5개와 1개의 TOP으로 분류된 6개의 Product Entity 리스트 반환
     *
     * @return
     */
    public static List<Product> createProductList() {

        List<Product> product = createProductListByOUTER();

        Product otherClassification = Product.builder()
                .id("testProductId" + 6)
                .classification(
                        Classification.builder()
                                .id("TOP")
                                .classificationStep(0)
                                .build()
                )
                .productName("DummyProduct")
                .productPrice(1000)
                .thumbnail("productThumbnail")
                .isOpen(true)
                .productSales(0L)
                .productDiscount(0)
                .build();
        otherClassification.addProductOption(createProductOption(6));
        product.add(otherClassification);

        return product;
    }

    /**
     * Create ProductList OUTER
     * OUTER로 5개의 ProductEntity 리스트 생성
     *
     * @return
     */
    public static List<Product> createProductListByOUTER() {
        List<Product> result = new ArrayList<>();
        for(int i = 0; i < 5; i++) {
            Product product = createProduct(i);
            ProductOption productOption = createProductOption(i);
            product.addProductOption(productOption);

            result.add(product);
        }


        return result;
    }

    /**
     * Create ProductEntity
     * ProductEntity 생성 메소드
     * Primary Key인 id를 간단하게 분리하기 위해 int i를 받아 추가.
     *
     * @param i (index)
     * @return
     */
    public static Product createProduct(int i) {
        return Product.builder()
                        .id("testProductId" + i)
                        .classification(createProductClassificationByOUTER())
                        .productName("DummyProduct" + i)
                        .productPrice(1000)
                        .thumbnail("productThumbnail" + i)
                        .isOpen(true)
                        .productSales(0L)
                        .productDiscount(0)
                        .build();
    }

    /**
     * Create Product classification
     * OUTER 분류로 나눠질 Classification Entity 생성 후 반환.
     *
     * @return
     */
    private static Classification createProductClassificationByOUTER() {
        return Classification.builder()
                .id("OUTER")
                .classificationStep(0)
                .build();
    }

    /**
     * Create ProductPageDTO
     * 상품 리스트 조회 시 사용되는 ProductPageDTO 생성 후 반환.
     * 파라미터로 해당 분류를 받아서 처리.
     *
     *
     * @param classification
     * @return
     */
    public static ProductPageDTO createProductPageDTO(String classification) {
        return ProductPageDTO.builder()
                            .pageNum(1)
                            .keyword(null)
                            .classification(classification)
                            .build();
    }

    /**
     * Create ProductList Pageable Object
     * BEST, NEW를 제외한 분류별 상품 리스트 조회시 사용될 Pageable 객체 생성 후 반환
     *
     * @return
     */
    public static Pageable createProductListPageable() {
        return PageRequest.of(0, PageAmount.MAIN_AMOUNT.getAmount(), Sort.by("createdAt").descending());
    }

    /**
     * Create Product and ProductThumbnail, ProductInfoImage Entity
     * 상품 썸네일과 정보 이미지 조회를 위해 임의의 ProductThumbnailEntity, ProductInfoImageEntity 생성 후 Product에 담아 반환.
     * Product 내부에서는 ProductThumbnail과 ProductInfoImage Entity를 Set으로 갖기 때문에 add를 통해 추가.
     * 파일의 실제 저장은 처리하지 않고 데이터베이스 요청 테스트만 검증하기 위함.
     *
     * @return
     */
    public static Product createProductAndImage() {
        Product product = createProduct(0);
        List<ProductThumbnail> thumbnailSet = createProductThumbnailList();
        List<ProductInfoImage> infoImageSet = createProductInfoImageList();

        thumbnailSet.forEach(product::addProductThumbnail);
        infoImageSet.forEach(product::addProductInfoImage);

        return product;
    }

    /**
     * Create ProductThumbnail List
     * 상품 썸네일 Entity 리스트 생성 후 반환.
     *
     * @return
     */
    public static List<ProductThumbnail> createProductThumbnailList() {
        List<String> imageNameList = List.of("thumb1", "thumb2", "thumb3", "thumb4");

        return imageNameList.stream()
                            .map(ProductFixture::createProductThumbnail)
                            .toList();
    }

    /**
     * Create ProductInfoImage List
     * 상품 정보 이미지 Entity 리스트 생성 후 반환.
     *
     * @return
     */
    public static List<ProductInfoImage> createProductInfoImageList() {
        List<String> imageNameList = List.of("info1", "info2", "info3");

        return imageNameList.stream()
                            .map(ProductFixture::createProductInfoImage)
                            .toList();
    }

    /**
     * Create ProductThumbnail Entity
     * 상품 썸네일 Entity 생성 후 반환.
     * 추후 테스트에 필드 추가 가능성으로 인해 분리.
     *
     * @param imageName
     * @return
     */
    public static ProductThumbnail createProductThumbnail(String imageName) {
        return ProductThumbnail.builder()
                .imageName(imageName)
                .build();
    }

    /**
     * Create ProductInfoImage Entity
     * 상품 정보 이미지 Entity 생성 후 반환.
     * 추후 테스트에 필드 추가 가능성으로 인해 분리.
     *
     * @param imageName
     * @return
     */
    public static ProductInfoImage createProductInfoImage(String imageName) {
        return ProductInfoImage.builder()
                .imageName(imageName)
                .build();
    }

    /**
     * Create Product Entity And ProductOption Entity
     * 여러개의 ProductOption Entity가 존재하는 Product Entity 생성 후 반환.
     * 하나의 상품의 여러 옵션 조회 테스트를 위함.
     *
     * @return
     */
    public static Product createProductAndOption() {
        Product product = createProduct(0);
        List<ProductOption> options = createProductOptionList();
        options.forEach(product::addProductOption);

        return product;
    }

    /**
     * Create ProductOption Entity List
     * 상품 옵션 Entity 리스트 생성 후 반환.
     *
     * @return
     */
    public static List<ProductOption> createProductOptionList() {
        List<ProductOption> result = new ArrayList<>();
        for(int i = 0; i < 5; i++)
            result.add(createProductOption(i));

        return result;
    }

    /**
     * Create ProductOption Entity
     * 상품 옵션 Entity 생성
     * size, color를 통한 검증을 위해 i를 추가해 테스트코드에서 정확하게 검증하도록 처리.
     *
     * @param i
     * @return
     */
    public static ProductOption createProductOption(int i) {
        return ProductOption.builder()
                .size("testSize" + i)
                .color("testColor" + i)
                .stock(i)
                .isOpen(true)
                .build();
    }
}
