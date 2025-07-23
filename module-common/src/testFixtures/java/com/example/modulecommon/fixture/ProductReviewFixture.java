package com.example.modulecommon.fixture;

import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.entity.ProductOption;
import com.example.modulecommon.model.entity.ProductReview;
import com.example.modulecommon.model.entity.ProductReviewReply;

import java.util.ArrayList;
import java.util.List;

public class ProductReviewFixture {

    public static List<ProductReview> createReviewWithCompletedAnswer(List<Member> members, List<ProductOption> options) {
        List<ProductReview> result = createDefaultReview(members, options);
        result.forEach(v -> v.setStatus(true));

        return result;
    }


    public static List<ProductReview> createDefaultReview(List<Member> members, List<ProductOption> options) {
        List<ProductReview> result = new ArrayList<>();

        for(Member m : members)
            for(ProductOption option : options)
                result.add(createReview(m, option));

        return result;
    }

    private static ProductReview createReview(Member member, ProductOption productOption) {
        return ProductReview.builder()
                .product(productOption.getProduct())
                .productOption(productOption)
                .member(member)
                .reviewContent(member.getUserId() + " review by " + productOption.getProduct().getProductName() + ", " + productOption.getColor())
                .status(false)
                .build();
    }

    public static List<ProductReviewReply> createDefaultReviewReply(List<ProductReview> reviews, Member admin) {
        List<ProductReviewReply> result = new ArrayList<>();

        for(ProductReview review : reviews)
            result.add(createReviewReply(admin, review));

        return result;
    }

    private static ProductReviewReply createReviewReply(Member admin, ProductReview review) {
        return ProductReviewReply.builder()
                .member(admin)
                .productReview(review)
                .replyContent(review.getReviewContent() + " reply")
                .build();
    }
}
