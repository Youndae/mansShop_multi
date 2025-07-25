package com.example.moduleuser.repository;

import com.example.moduleauth.model.dto.member.UserSearchDTO;
import com.example.moduleauth.model.dto.member.UserSearchPwDTO;
import com.example.modulecommon.model.entity.Member;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import static com.example.modulecommon.model.entity.QAuth.auth1;
import static com.example.modulecommon.model.entity.QMember.member;


@Repository
@RequiredArgsConstructor
public class MemberDSLRepositoryImpl implements MemberDSLRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Member findByLocalUserId(String userId) {

        return jpaQueryFactory.selectFrom(member)
                .innerJoin(member.auths, auth1).fetchJoin()
                .where(member.userId.eq(userId).and(member.provider.eq("local")))
                .fetchOne();
    }

    @Override
    public Member findByUserId(String userId) {

        return jpaQueryFactory.selectFrom(member)
                .leftJoin(member.auths, auth1).fetchJoin()
                .where(member.userId.eq(userId))
                .fetchOne();
    }

    @Override
    public String searchId(UserSearchDTO searchDTO) {
        return jpaQueryFactory.select(member.userId)
                .from(member)
                .where(memberSearchId(searchDTO))
                .fetchOne();
    }

    private BooleanExpression memberSearchId(UserSearchDTO searchDTO) {
        if(searchDTO.userPhone() == null)
            return member.userName.eq(searchDTO.userName()).and(member.userEmail.eq(searchDTO.userEmail()));
        else if(searchDTO.userEmail() == null)
            return member.userName.eq(searchDTO.userName()).and(member.phone.eq(searchDTO.userPhone()));

        return member.userName.eq("");
    }

    @Override
    public Long findByPassword(UserSearchPwDTO searchDTO) {
        return jpaQueryFactory.select(member.countDistinct())
                .from(member)
                .where(
                        member.userId.eq(searchDTO.userId())
                                .and(member.userName.eq(searchDTO.userName()))
                                .and(member.userEmail.eq(searchDTO.userEmail()))
                )
                .fetchOne();
    }
}
