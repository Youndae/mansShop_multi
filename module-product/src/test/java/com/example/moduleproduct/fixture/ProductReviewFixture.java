package com.example.moduleproduct.fixture;

import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.entity.Product;
import com.example.modulecommon.model.entity.ProductReview;
import com.example.modulecommon.model.entity.ProductReviewReply;
import com.example.modulecommon.model.enumuration.PageAmount;
import com.example.moduleproduct.model.dto.product.business.ProductReviewResponseDTO;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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
        Member member = MemberFixture.createOneMember();

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

    public static Page<ProductReviewResponseDTO> createPageObjectByProductReview() {
        List<ProductReviewResponseDTO> reviewDTO = IntStream.range(0, 5)
                                                        .mapToObj(ProductReviewFixture::createProductReviewResponseDTO)
                                                        .toList();
        return new PageImpl<>(reviewDTO, createReviewPageable(), reviewDTO.size());
    }

    public static ProductReviewResponseDTO createProductReviewResponseDTO(int i) {

        return new ProductReviewResponseDTO("writer" + i,
                                            "reviewContent" + i,
                                            LocalDate.now(),
                                            "answerContent" + i,
                                            LocalDate.now()
                                    );
    }

    public static ProductReviewReply createProductReviewReply(Member member, ProductReview productReview, int i) {
        return ProductReviewReply.builder()
                .member(member)
                .productReview(productReview)
                .replyContent("replyContent" + i)
                .build();
    }

    public static List<ProductReviewResponseDTO> productReviewResponseDTOFilter(String productId,
                                                                                List<ProductReview> review,
                                                                                List<ProductReviewReply> reviewReply) {
        List<ProductReview> reviewList = review.stream()
                                                .filter(v ->
                                                        v.getProduct().getId().equals(productId)
                                                )
                                                .sorted((v1, v2) ->
                                                        Long.compare(v2.getId(), v1.getId())
                                                )
                                                .toList();
        List<ProductReviewResponseDTO> result = new ArrayList<>();

        for(int i = 0; i < reviewList.size(); i++) {
            ProductReview reviewEntity = reviewList.get(i);
            String replyContent = null;

            for(int j = 0; j < reviewReply.size(); j++) {
                if(reviewEntity.getId() == reviewReply.get(j).getProductReview().getId()){
                    replyContent = reviewReply.get(j).getReplyContent();
                    break;
                }
            }

            result.add(new ProductReviewResponseDTO(
                    reviewEntity.getMember().getNickname() == null ?
                            reviewEntity.getMember().getUserName() : reviewEntity.getMember().getNickname(),
                    reviewEntity.getReviewContent(),
                    null,
                    replyContent,
                    null
            ));
        }

        return result;
    }
}
