package com.example.moduleproduct.model.dto.product.in;

import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.entity.Product;
import com.example.modulecommon.model.entity.ProductQnA;

public record ProductQnAPostDTO(
        String productId,
        String content
) {

    public ProductQnA toProductQnAEntity(Member member, Product product) {
        return ProductQnA.builder()
                .member(member)
                .product(product)
                .qnaContent(this.content)
                .build();
    }
}
