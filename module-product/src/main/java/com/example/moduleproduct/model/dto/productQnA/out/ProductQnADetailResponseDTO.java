package com.example.moduleproduct.model.dto.productQnA.out;

import com.example.modulecommon.model.dto.qna.out.QnADetailReplyDTO;
import com.example.moduleproduct.model.dto.productQnA.business.ProductQnADetailDTO;

import java.time.LocalDate;
import java.util.List;

public record ProductQnADetailResponseDTO(
        long productQnAId,
        String productName,
        String writer,
        String qnaContent,
        LocalDate createdAt,
        boolean productQnAStat,
        List<QnADetailReplyDTO> replyList
) {

    public ProductQnADetailResponseDTO(ProductQnADetailDTO qnaDTO,
                                        List<QnADetailReplyDTO> replyList) {
        this(
                qnaDTO.productQnAId(),
                qnaDTO.productName(),
                qnaDTO.writer(),
                qnaDTO.qnaContent(),
                qnaDTO.createdAt().toLocalDate(),
                qnaDTO.productQnAStat(),
                replyList
        );
    }
}
