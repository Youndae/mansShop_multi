package com.example.modulemypage.repository;


import com.example.modulecommon.model.dto.qna.out.QnADetailReplyDTO;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.example.modulecommon.model.entity.QMemberQnAReply.memberQnAReply;

@Repository
@RequiredArgsConstructor
public class MemberQnAReplyDSLRepositoryImpl implements MemberQnAReplyDSLRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<QnADetailReplyDTO> findAllByQnAId(long qnaId) {
        return jpaQueryFactory.select(
                        Projections.constructor(
                                QnADetailReplyDTO.class,
                                memberQnAReply.id.as("replyId"),
                                new CaseBuilder()
                                        .when(memberQnAReply.member.nickname.isNull())
                                        .then(memberQnAReply.member.userName)
                                        .otherwise(memberQnAReply.member.nickname)
                                        .as("writer"),
                                memberQnAReply.replyContent,
                                memberQnAReply.updatedAt
                        )
                )
                .from(memberQnAReply)
                .where(memberQnAReply.memberQnA.id.eq(qnaId))
                .orderBy(memberQnAReply.id.asc())
                .fetch();
    }
}
