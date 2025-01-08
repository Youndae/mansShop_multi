package com.example.moduleauth.fixture;

import com.example.moduleauth.model.dto.member.UserSearchDTO;
import com.example.moduleauth.model.dto.member.UserSearchPwDTO;
import com.example.modulecommon.model.entity.Auth;
import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.enumuration.OAuthProvider;

public class MemberFixture {

    public static Member createLocalMember() {
        Member member = Member.builder()
                .userId("testUser1")
                .userName("testUserName")
                .phone("010-1234-5678")
                .userEmail("testuser1@test.com")
                .provider(OAuthProvider.LOCAL.getKey())
                .build();

        member.addMemberAuth(new Auth().toMemberAuth());

        return member;
    }

    public static Member createGoogleMember() {
        Member member = Member.builder()
                .userId("testUser2")
                .userName("testUserName2")
                .phone("01012345678")
                .userEmail("testuser2@test.com")
                .provider(OAuthProvider.GOOGLE.getKey())
                .build();

        member.addMemberAuth(new Auth().toMemberAuth());

        return member;
    }

    public static UserSearchDTO createUserSearchDTOByPhone() {
        Member member = createLocalMember();

        return new UserSearchDTO(member.getUserName(), member.getPhone(), null);
    }

    public static UserSearchDTO createUserSearchDTOByEmail() {
        Member member = createLocalMember();

        return new UserSearchDTO(member.getUserName(), null, member.getUserEmail());
    }

    public static UserSearchDTO createWrongUserSearchDTO() {
        return new UserSearchDTO("wrongUser", "01011112222", "wronguser@user.com");
    }

    public static UserSearchPwDTO createUserSearchPwDTO() {
        Member member = createLocalMember();

        return new UserSearchPwDTO(member.getUserId(), member.getUserName(), member.getUserEmail());
    }

    public static UserSearchPwDTO createWrongUserSearchPwDTO() {
        return new UserSearchPwDTO("wrongUserId", "wrongUserName", "wronguser@user.com");
    }
}
