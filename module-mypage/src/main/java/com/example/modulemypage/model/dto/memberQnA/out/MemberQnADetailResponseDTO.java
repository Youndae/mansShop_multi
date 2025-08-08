package com.example.modulemypage.model.dto.memberQnA.out;

import com.example.modulecommon.model.dto.qna.out.QnADetailReplyDTO;
import com.example.modulemypage.model.dto.memberQnA.business.MemberQnADetailDTO;

import java.time.LocalDate;
import java.util.List;

public record MemberQnADetailResponseDTO(
        long memberQnAId,
        String qnaClassification,
        String qnaTitle,
        String writer,
        String qnaContent,
        LocalDate updatedAt,
        boolean memberQnAStat,
        List<QnADetailReplyDTO> replyList
) {

    public MemberQnADetailResponseDTO(MemberQnADetailDTO memberQnADTO, List<QnADetailReplyDTO> replyList) {
        this(
                memberQnADTO.memberQnAId(),
                memberQnADTO.qnaClassification(),
                memberQnADTO.qnaTitle(),
                memberQnADTO.writer(),
                memberQnADTO.qnaContent(),
                memberQnADTO.updatedAt().toLocalDate(),
                memberQnADTO.memberQnAStat(),
                replyList
        );
    }
}
