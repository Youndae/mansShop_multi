package com.example.moduleproduct.model.dto.product.out;

import java.time.LocalDateTime;

public record ProductDetailQnAReplyListDTO(
        String writer,
        String replyContent,
        Long qnaId,
        LocalDateTime createdAt
) {
}
