package com.example.modulemypage.repository;

import com.example.modulecommon.model.dto.qna.out.QnADetailReplyDTO;

import java.util.List;

public interface MemberQnAReplyDSLRepository {

    List<QnADetailReplyDTO> findAllByQnAId(long qnaId);
}
