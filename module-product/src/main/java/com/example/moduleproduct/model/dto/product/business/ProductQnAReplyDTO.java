package com.example.moduleproduct.model.dto.product.business;

import com.example.moduleproduct.model.dto.product.out.ProductDetailQnAReplyListDTO;

import java.time.LocalDate;

public record ProductQnAReplyDTO(
        String writer,
        String content,
        LocalDate createdAt
) {

    public ProductQnAReplyDTO (ProductDetailQnAReplyListDTO qnaReply) {
        this(
                qnaReply.writer(),
                qnaReply.replyContent(),
                qnaReply.createdAt().toLocalDate()
        );
    }
}
