package com.example.modulemypage.service;

import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.entity.MemberQnA;
import com.example.modulecommon.model.entity.MemberQnAReply;
import com.example.modulecommon.model.entity.QnAClassification;
import com.example.modulemypage.model.dto.memberQnA.in.MemberQnAInsertDTO;
import org.springframework.stereotype.Service;

@Service
public class MemberQnADomainService {

    public MemberQnA buildInsertMemberQnA(MemberQnAInsertDTO insertDTO,
                                          QnAClassification qnAClassification,
                                          Member member) {
        return MemberQnA.builder()
                .member(member)
                .qnAClassification(qnAClassification)
                .memberQnATitle(insertDTO.title())
                .memberQnAContent(insertDTO.content())
                .build();
    }

    public MemberQnAReply buildInsertMemberQnaReply(Member member,
                                                    MemberQnA memberQnA,
                                                    String content) {
        return MemberQnAReply.builder()
                .member(member)
                .memberQnA(memberQnA)
                .replyContent(content)
                .build();
    }
}
