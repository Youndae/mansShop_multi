package com.example.moduleproduct.repository.productReviewReply;

import com.example.modulecommon.model.entity.ProductReviewReply;

public interface ProductReviewReplyDSLRepository {

    ProductReviewReply findByReviewId(Long reviewId);
}
