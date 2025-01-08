package com.example.moduleproduct.fixture;

import com.example.modulecommon.model.entity.Auth;
import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.enumuration.OAuthProvider;
import com.example.modulecommon.model.enumuration.Role;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class MemberFixture {

    /**
     * Create MemberEntity
     *
     * @return Member
     */
    public static Member createOneMember() {
        return createMember("testUser1", "testUserName1", null);
    }

    public static List<Member> createMemberList(int count) {
        return IntStream.range(0, count)
                .mapToObj(v ->
                        createMember("testUser" + v, "testUserName" + v, null)
                )
                .toList();
    }

    public static Member createUseNicknameMember(int num) {
        return createMember("testUser" + num,
                            "testUserName" + num,
                            "testNickname" + num
                    );
    }

    public static Member createAdminMember(int num) {
        Member member = createMember("testUser" + num,
                "testUserName" + num,
                "testNickname" + num
        );
        member.addMemberAuth(Auth.builder().member(member).auth(Role.ADMIN.getKey()).build());

        return member;
    }

    public static Member createMember(String userId, String userName, String nickname) {
        Member member;
        if(nickname == null){
            member = Member.builder()
                    .userId(userId)
                    .userName(userName)
                    .provider(OAuthProvider.LOCAL.getKey())
                    .build();
        }else {
            member = Member.builder()
                    .userId(userId)
                    .userName(userName)
                    .nickname(nickname)
                    .provider(OAuthProvider.LOCAL.getKey())
                    .build();
        }

        member.addMemberAuth(new Auth().toMemberAuth());

        return member;
    }
}
