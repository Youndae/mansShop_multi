package com.example.moduleproduct.fixture;

import com.example.modulecommon.model.entity.Auth;
import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.enumuration.OAuthProvider;

public class MemberFixture {

    /**
     * Create MemberEntity
     *
     * @return Member
     */
    public static Member createMember() {
        Member member = Member.builder()
                .userId("testUser1")
                .userName("testUserName")
                .provider(OAuthProvider.LOCAL.getKey())
                .build();

        member.addMemberAuth(new Auth().toMemberAuth());

        return member;
    }
}
