package com.example.modulecommon.model.dto.qna.out;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record QnADetailReplyDTO(
        long replyId,
        String writer,
        String replyContent,
        LocalDate updatedAt
) {
    public QnADetailReplyDTO(long replyId,
                             String writer,
                             String replyContent,
                             LocalDateTime updatedAt) {
        this(
                replyId,
                writer,
                replyContent,
                updatedAt.toLocalDate()
        );
    }
}
