package com.example.modulecommon.fixture;

import com.example.modulecommon.model.dto.MemberAndAuthFixtureDTO;
import com.example.modulecommon.model.entity.Auth;
import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.enumuration.Role;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MemberAndAuthFixture {

    public static MemberAndAuthFixtureDTO createDefaultMember (int count) {
        List<Member> memberList = new ArrayList<>();
        List<Auth> authList = new ArrayList<>();

        for(int i = 0; i < count; i++) {
            String userId = "tester" + i;
            Member member = createMember(userId);
            Auth auth = createAuthByAuth(userId, Role.MEMBER.getKey());
            member.addMemberAuth(auth);

            memberList.add(member);
            authList.add(auth);
        }

        return new MemberAndAuthFixtureDTO(memberList, authList);
    }

    public static MemberAndAuthFixtureDTO createAdmin () {
        String userId = "admin";
        Member member = createMember(userId);

        List<Auth> auths = List.of(
                createAuthByAuth(userId, Role.MEMBER.getKey()),
                createAuthByAuth(userId, Role.MANAGER.getKey()),
                createAuthByAuth(userId, Role.ADMIN.getKey())
        );

        auths.forEach(member::addMemberAuth);
        List<Member> admin = List.of(member);

        return new MemberAndAuthFixtureDTO(admin, auths);
    }

    public static MemberAndAuthFixtureDTO createAnonymous() {
        String userId = Role.ANONYMOUS.getRole();
        Member member = createMember(userId);
        Auth auth = createAuthByAuth(userId, Role.MEMBER.getKey());
        member.addMemberAuth(auth);
        List<Member> anonymous = List.of(member);
        List<Auth> auths = List.of(auth);

        return new MemberAndAuthFixtureDTO(anonymous, auths);
    }

    private static Member createMember(String userId) {
        return Member.builder()
                .userId(userId)
                .userPw("1234")
                .userName(userId + "Name")
                .nickname(userId + "nickname")
                .userEmail(userId + "@" + userId + ".com")
                .provider("local")
                .phone("01012341234")
                .birth(LocalDate.now())
                .build();
    }

    private static Member createMember(String userId, String provider) {
        return Member.builder()
                .userId(userId)
                .userPw("1234")
                .userName(userId + "Name")
                .nickname(userId + "nickname")
                .userEmail(userId + "@" + userId + ".com")
                .provider(provider)
                .phone("01012341234")
                .birth(LocalDate.now())
                .build();
    }

    private static Auth createAuthByAuth(String userId, String auth) {
        return Auth.builder()
                .auth(auth)
                .build();
    }
}
