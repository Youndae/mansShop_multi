package com.example.moduleauth.fixture;

import com.example.modulecommon.model.entity.Auth;
import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.enumuration.OAuthProvider;

public class MemberFixture {

    public static Member createLocalMember() {
        Member member = Member.builder()
                .userId("testUser1")
                .userName("testUserName")
                .provider(OAuthProvider.LOCAL.getKey())
                .build();

        member.addMemberAuth(new Auth().toMemberAuth());

        return member;
    }

    public static Member createGoogleMember() {
        Member member = Member.builder()
                .userId("testUser2")
                .userName("testUserName2")
                .provider(OAuthProvider.GOOGLE.getKey())
                .build();

        member.addMemberAuth(new Auth().toMemberAuth());

        return member;
    }
}
