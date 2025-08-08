package com.example.moduleproduct.service.productQnA;

import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.entity.ProductQnA;
import com.example.modulecommon.model.entity.ProductQnAReply;
import org.springframework.stereotype.Service;

@Service
public class ProductQnADomainService {

    public ProductQnAReply buildProductQnAReply(Member member, ProductQnA productQnA, String content) {
        return ProductQnAReply.builder()
                .member(member)
                .productQnA(productQnA)
                .replyContent(content)
                .build();
    }
}
