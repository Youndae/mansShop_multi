package com.example.moduleproduct.repository.productQnAReply;

import com.example.modulecommon.model.entity.ProductQnAReply;

import java.util.List;

public interface ProductQnAReplyDSLRepository {

    List<ProductQnAReply> findByQnAReply(List<Long> qnaIds);
}
