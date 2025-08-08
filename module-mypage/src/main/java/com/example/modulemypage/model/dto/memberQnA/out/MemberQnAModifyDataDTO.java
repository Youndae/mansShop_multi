package com.example.modulemypage.model.dto.memberQnA.out;

import com.example.modulecommon.model.entity.MemberQnA;

import java.util.List;

public record MemberQnAModifyDataDTO(
        long qnaId,
        String qnaTitle,
        String qnaContent,
        long qnaClassificationId,
        List<QnAClassificationDTO> classificationList
) {

    public MemberQnAModifyDataDTO(MemberQnA memberQnA, List<QnAClassificationDTO> classificationList) {
        this(
                memberQnA.getId(),
                memberQnA.getMemberQnATitle(),
                memberQnA.getMemberQnAContent(),
                memberQnA.getQnAClassification().getId(),
                classificationList
        );
    }
}
