package com.example.moduleproduct.fixture;

import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.entity.Product;
import com.example.modulecommon.model.entity.ProductOption;
import com.example.modulecommon.model.entity.ProductReview;
import com.example.modulecommon.model.enumuration.PageAmount;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ProductReviewFixture {

    /**
     * Create ProductReview Entity List
     * 상품 리뷰 리스트 생성 후 반환.
     * 리뷰에는 ProductOption 연관관계가 설정되어 있기 때문에 ProductOption을 포함한 ProductEntity를 생성
     *
     * @return
     */
    public static List<ProductReview> createProductReviewList() {
        Product product = ProductFixture.createProductAndOption();
        Member member = MemberFixture.createMember();

        return IntStream.range(0, 5)
                        .mapToObj(v ->
                                createProductReview(v, member, product)
                        )
                        .toList();
    }

    /**
     * Create ProductReview Entity
     * Product를 파라미터로 받아 Product와 ProductOption을 담을 수 있도록 처리.
     * 정상적인 데이터 반환을 테스트하기 위해 content에 i를 추가.
     *
     * @param i
     * @param member
     * @param product
     * @return
     */
    public static ProductReview createProductReview(int i,
                                                    Member member,
                                                    Product product) {
        return ProductReview.builder()
                .member(member)
                .product(product)
                .productOption(product.getProductOptionSet().get(0))
                .reviewContent("TestReview" + i)
                .build();
    }

    /**
     * Create ProductReview Pageable Object
     * 리뷰 조회 시 사용될 Pageable 객체 생성 후 반환.
     * @return
     */
    public static Pageable createReviewPageable() {

        return PageRequest.of(0,
                                PageAmount.PRODUCT_REVIEW_AMOUNT.getAmount(),
                                Sort.by("createdAt").descending()
                        );
    }
}
