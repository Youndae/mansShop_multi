package com.example.modulecommon.model.dto.admin.qna.out;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AdminQnAListResponseDTO(
        long qnaId,
        String classification,
        String title,
        String writer,
        LocalDate createdAt,
        boolean answerStatus
) {
    public AdminQnAListResponseDTO(long qnaId,
                                   String classification,
                                   String title,
                                   String writer,
                                   LocalDateTime createdAt,
                                   boolean answerStatus) {
        this(
                qnaId,
                classification,
                title,
                writer,
                createdAt.toLocalDate(),
                answerStatus
        );
    }
}
