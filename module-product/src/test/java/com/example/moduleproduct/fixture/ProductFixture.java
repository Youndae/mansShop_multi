package com.example.moduleproduct.fixture;

import com.example.modulecommon.model.dto.response.PagingMappingDTO;
import com.example.modulecommon.model.entity.*;
import com.example.modulecommon.model.enumuration.PageAmount;
import com.example.moduleproduct.model.dto.main.business.MainListDTO;
import com.example.moduleproduct.model.dto.main.out.MainListResponseDTO;
import com.example.moduleproduct.model.dto.page.ProductPageDTO;
import com.example.moduleproduct.model.dto.product.business.ProductOptionDTO;
import org.springframework.data.domain.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ProductFixture {


    /**
     *
     * Create ProductList
     * OUTER 15개 TOP 5개 데이터 생성
     * ProductOption의 경우 하나의 Product당 3개 존재.
     * Sales는 10개씩 증가.
     * discount는 10% 5개, 50% 5개, 나머지는 0. OUTER에만 discount 추가. TOP 상품 5개는 모두 0.
     * stock의 경우 5개 상품은 모든 옵션 0으로 생성. 이 5개 상품은 모두 OUTER에 존재.
     *
     *
     * @return
     */
    public static List<Product> createProductList() {

        /**
         * TOP 5개 상품은
         * 모두 30의 stock을 갖고
         * sales는 10, 20, 30, 40, 50
         * 1, 2번째 상품은 isOpen이 false.
         * 3번째 상품의 1,2번 옵션은 isOpen이 false.
         *
         * OUTER 5개 상품은
         * 모두 stock이 0.
         * Sales는 60, 70, 80, 90, 100
         *
         * 남은 OUTER 10개 상품은
         * 모두 stock이 30
         * discount는 10% 5개, 50% 5개
         * sales는 110, 120, ..., 200
         *
         *
         * 설정값
         * stock, discount, classification
         *
         */



        List<Product> productList = new ArrayList<>();
        int sales = 0;

        for(int i = 0; i < 5; i++) {
            sales += 10;
            boolean isOpen = true;

            if(i <= 1)
                isOpen = false;

            Product product = createProduct(i, "TOP", isOpen, sales, 0);

            for(int j = 0; j < 3; j++) {
                boolean optionIsOpen = true;
                if(i == 2 && j < 2)
                    optionIsOpen = false;

                ProductOption option = createProductOption(j, 10, optionIsOpen);

                product.addProductOption(option);
            }

            productList.add(product);
        }

        for(int i = 0; i < 15; i++) {
            sales += 10;
            int stock = 10;
            int discount = 0;

            if(i < 5)
                stock = 0;
            else if(i < 10)
                discount = 10;
            else
                discount = 50;

            Product product = createProduct(i, "OUTER", true, sales, discount);

            for(int j = 0; j < 3; j++){
                ProductOption productOption = createProductOption(j, stock, true);
                product.addProductOption(productOption);
            }

            productList.add(product);
        }


        return productList;
    }

    public static List<Classification> createClassificationList() {
        Classification outer = createProductClassification("OUTER");
        Classification top = createProductClassification("TOP");

        return List.of(outer, top);
    }

    public static List<Product> outerProductFilter(List<Product> productList) {
        return productFilter(productList, "OUTER");
    }

    public static List<Product> topProductFilter(List<Product> productList) {
        return productFilter(productList, "TOP");
    }

    private static List<Product> productFilter(List<Product> productList, String classification) {
        return productList.stream().filter(v -> v.getClassification().getId().equals(classification)).toList();
    }

    public static List<MainListDTO> createMainListDTOByDataReverse(List<Product> productList) {
        List<Product> list = new ArrayList<>(productList.stream().filter(Product::isOpen).toList());
        Collections.reverse(list);
        list = list.stream().limit(12).toList();

        return createMainListDTOByProductList(list);
    }

    public static List<Product> bestProductFilter(List<Product> productList) {
        return productList.stream()
                            .sorted((v1, v2) ->
                                    Long.compare(v2.getProductSales(), v1.getProductSales())
                            )
                            .limit(12)
                            .toList();
    }

    public static List<Product> newProductFilter(List<Product> productList) {
        Collections.reverse(productList);

        return productList.stream().filter(Product::isOpen).limit(12).toList();
    }

    public static List<MainListResponseDTO> outerMainResponseListDTOList() {
        List<Product> allProductList = createProductList();
        List<Product> outerProductList = outerProductFilter(allProductList);
        List<MainListDTO> mainListDTOList = createMainListDTOByProductList(outerProductList);

        return mappingMainListResponseDTO(mainListDTOList);
    }

    public static List<MainListResponseDTO> topMainResponseListDTOList() {
        List<Product> allProductList = createProductList();
        List<Product> topProductList = topProductFilter(allProductList);
        List<MainListDTO> mainListDTOList = createMainListDTOByProductList(topProductList);

        return mappingMainListResponseDTO(mainListDTOList);
    }

    public static List<MainListResponseDTO> bestMainResponseListDTOList() {
        List<Product> allProductList = createProductList();
        List<Product> bestProductList = bestProductFilter(allProductList);
        List<MainListDTO> mainListDTOList = createMainListDTOByProductList(bestProductList);

        return mappingMainListResponseDTO(mainListDTOList);
    }

    public static List<MainListDTO> createMainListDTOByProductList(List<Product> productList) {
        return productList.stream()
                            .map(v ->{
                                long stock = v.getProductOptionSet()
                                        .stream()
                                        .mapToLong(ProductOption::getStock)
                                        .reduce(0L, Long::sum);
                                return new MainListDTO(v.getId(),
                                        v.getProductName(),
                                        v.getThumbnail(),
                                        v.getProductPrice(),
                                        v.getProductDiscount(),
                                        stock);
                            })
                            .toList();
    }

    public static List<MainListResponseDTO> mappingMainListResponseDTO(List<MainListDTO> dto) {
        return Optional.ofNullable(dto)
                        .map(list -> list.stream()
                                .map(MainListResponseDTO::new)
                                .toList()
                        )
                        .orElse(null);
    }




    public static Product createOneProductEntity() {
        return createProduct(0, "OUTER", true, 0L, 0);
    }

    /**
     * Create ProductEntity
     * ProductEntity 생성 메소드
     * Primary Key인 id를 간단하게 분리하기 위해 int i를 받아 추가.
     *
     * @param i (index)
     * @return
     */
    public static Product createProduct(int i, String classification, boolean isOpen, long sales, int discount) {
        return Product.builder()
                        .id("test" + classification + "ProductId" + i)
                        .classification(createProductClassification(classification))
                        .productName("Dummy" + classification + "Product" + i)
                        .productPrice(1000)
                        .thumbnail("productThumbnail" + i)
                        .isOpen(isOpen)
                        .productSales(sales)
                        .productDiscount(discount)
                        .build();
    }

    /**
     * Create Product classification
     * OUTER 분류로 나눠질 Classification Entity 생성 후 반환.
     *
     * @return
     */
    private static Classification createProductClassification(String classification) {
        return Classification.builder()
                .id(classification)
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
        Product product = createOneProductEntity();
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
        List<String> imageNameList = List.of("info1", "info2", "info3", "info4");

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
        Product product = createOneProductEntity();
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
            result.add(createProductOption(i, i, true));

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
    public static ProductOption createProductOption(int i, int stock, boolean isOpen) {
        return ProductOption.builder()
                .size("testSize" + i)
                .color("testColor" + i)
                .stock(stock)
                .isOpen(true)
                .build();
    }


    /**
     * Create ProductOptionDTO
     * 상품 옵션 조회 시 사용되는 DTO 리스트 생성.
     * 상품 상세 조회 시 옵션 리스트 조회에 대한 결과가 ProductOptionDTO에 담겨서 반환되기 때문에
     * 그 테스트에서 사용될 리스트.
     *
     * @return
     */
    public static List<ProductOptionDTO> createProductOptionDTOList() {

        return createProductOptionList().stream()
                                        .map(v ->
                                                new ProductOptionDTO(v.getId(),
                                                                    v.getSize(),
                                                                    v.getColor(),
                                                                    v.getStock()
                                                )
                                        ).toList();
    }
}
