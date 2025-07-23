package com.example.moduleproduct.repository.productQnAReply;

import com.example.modulecommon.model.entity.ProductQnAReply;
import com.example.moduleproduct.model.dto.product.out.ProductDetailQnAReplyListDTO;

import java.util.List;

public interface ProductQnAReplyDSLRepository {

    List<ProductDetailQnAReplyListDTO> getQnAReplyListByQnAIds(List<Long> qnaIds);
}
