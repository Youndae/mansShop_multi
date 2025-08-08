package com.example.moduleproduct.repository.productQnAReply;

import com.example.modulecommon.model.dto.qna.out.QnADetailReplyDTO;
import com.example.moduleproduct.model.dto.product.out.ProductDetailQnAReplyListDTO;

import java.util.List;

public interface ProductQnAReplyDSLRepository {

    List<ProductDetailQnAReplyListDTO> getQnAReplyListByQnAIds(List<Long> qnaIds);

    List<QnADetailReplyDTO> findAllByQnAId(long qnaId);
}
