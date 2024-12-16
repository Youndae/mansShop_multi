package com.example.moduleauth.repository;

import com.example.modulecommon.model.entity.Member;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import static com.example.modulecommon.model.entity.QMember.member;


@Repository
@RequiredArgsConstructor
public class MemberDSLRepositoryImpl implements MemberDSLRepository{

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Member findByLocalUserId(String userId) {
        return jpaQueryFactory.select(member)
                .from(member)
                .where(member.userId.eq(userId).and(member.provider.eq("local")))
                .fetchOne();
    }
}
