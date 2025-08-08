package com.example.modulemypage.model.dto.memberQnA.out;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record MemberQnAListDTO(
        long memberQnAId,
        String memberQnATitle,
        boolean memberQnAStat,
        String qnaClassification,
        LocalDate updatedAt
) {

    public MemberQnAListDTO(long memberQnAId,
                            String memberQnATitle,
                            boolean memberQnAStat,
                            String qnaClassification,
                            LocalDateTime updatedAt) {
        this(
                memberQnAId,
                memberQnATitle,
                memberQnAStat,
                qnaClassification,
                updatedAt.toLocalDate()
        );
    }
}
