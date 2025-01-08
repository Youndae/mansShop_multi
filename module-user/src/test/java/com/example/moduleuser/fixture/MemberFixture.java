package com.example.moduleuser.fixture;

import com.example.moduleauth.model.dto.member.UserSearchDTO;
import com.example.moduleauth.model.dto.member.UserSearchPwDTO;
import com.example.modulecommon.model.entity.Auth;
import com.example.modulecommon.model.entity.Member;
import com.example.moduleuser.model.dto.member.in.JoinDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

public class MemberFixture {

    public static Member createMember(int i) {
        Member member = Member.builder()
                            .userId("testUser" + i)
                            .userPw("testPw")
                            .userName("testUserName" + i)
                            .nickname("testNickname" + i)
                            .userEmail("testUser" + i + "@test.com")
                            .phone("01012345678")
                            .birth(LocalDate.now())
                            .build();
        member.addMemberAuth(new Auth().toMemberAuth());

        return member;
    }

    public static List<Member> createMemberList() {
        return IntStream.range(0, 5)
                .mapToObj(MemberFixture::createMember)
                .toList();
    }

    public static JoinDTO createJoinDTO() {
        return new JoinDTO(
                "testUser",
                "testUserPw",
                "testUserName",
                "testNickname",
                "01012345678",
                "2025/01/09",
                "testUser@test.com"
        );
    }

    public static UserSearchDTO createUserSearchDTOByPhone() {
        return new UserSearchDTO("testUser", "01012345678", null);
    }

    public static UserSearchDTO createUserSearchDTOByEmail() {
        return new UserSearchDTO("testUser", null, "testUser@test.com");
    }

    public static UserSearchPwDTO createUserSearchPwDTO() {
        return new UserSearchPwDTO("testUser", "testUserName", "testUser@test.com");
    }
}
