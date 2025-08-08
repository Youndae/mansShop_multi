package com.example.modulemypage.model.dto.memberQnA.business;

import java.time.LocalDateTime;

public record MemberQnADetailDTO(
        long memberQnAId,
        String qnaClassification,
        String qnaTitle,
        String writer,
        String qnaContent,
        LocalDateTime updatedAt,
        boolean memberQnAStat
) {
}
