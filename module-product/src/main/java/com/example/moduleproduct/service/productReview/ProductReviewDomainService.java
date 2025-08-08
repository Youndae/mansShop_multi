package com.example.moduleproduct.service.productReview;

import com.example.modulecommon.model.entity.*;
import org.springframework.stereotype.Service;

@Service
public class ProductReviewDomainService {


    public ProductReview buildProductReview(Member member, Product product, String content, ProductOption productOption) {
        return ProductReview.builder()
                .member(member)
                .product(product)
                .reviewContent(content)
                .productOption(productOption)
                .build();
    }

    public ProductReviewReply buildProductReviewReplyEntity(Member member, ProductReview productReview, String content) {
        return ProductReviewReply.builder()
                .member(member)
                .replyContent(content)
                .productReview(productReview)
                .build();
    }
}
