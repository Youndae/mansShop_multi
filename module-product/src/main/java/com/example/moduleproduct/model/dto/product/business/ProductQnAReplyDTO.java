package com.example.moduleproduct.model.dto.product.business;

import com.example.modulecommon.model.entity.ProductQnAReply;

import java.time.LocalDate;

public record ProductQnAReplyDTO(
        String writer,
        String content,
        LocalDate createdAt
) {

    public ProductQnAReplyDTO (ProductQnAReply qnaReply) {
        this(
                qnaReply.getMember().getNickname() == null ? qnaReply.getMember().getUserName() : qnaReply.getMember().getNickname(),
                qnaReply.getReplyContent(),
                qnaReply.getCreatedAt().toLocalDate()
        );
    }
}
