package com.example.moduleauth.repository;

import com.example.moduleauth.fixture.MemberFixture;
import com.example.moduleauth.model.dto.member.UserSearchDTO;
import com.example.moduleauth.model.dto.member.UserSearchPwDTO;
import com.example.modulecommon.model.entity.Member;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@EntityScan(basePackages = "com.example.modulecommon")
@EnableJpaRepositories(basePackages = "com.example.moduleauth.repository")
@ComponentScan(basePackages = {"com.example.moduleconfig", "com.example.modulecommon"})
@ActiveProfiles("test")
public class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    private Member member;

    @BeforeEach
    void init() {
        member = MemberFixture.createLocalMember();
        memberRepository.save(member);
    }

    @Test
    @DisplayName(value = "로컬 가입 유저 조회")
    void findLocalMemberSuccess() {
        Member result = memberRepository.findByLocalUserId(member.getUserId());

        Assertions.assertNotNull(result);
        Assertions.assertEquals(member.getUserId(), result.getUserId());
        Assertions.assertEquals(member.getProvider(), result.getProvider());
    }

    @Test
    @DisplayName(value = "로컬 가입 유저 조회 실패. null 반환")
    void findLocalMemberFail() {
        String userId = "testUser2";

        Assertions.assertNull(memberRepository.findByLocalUserId(userId));
    }

    @Test
    @DisplayName("아이디 찾기. 정상적인 정보인 경우. 연락처 기반 조회")
    void searchIdByPhone() {
        UserSearchDTO userSearchDTO = MemberFixture.createUserSearchDTOByPhone();

        Assertions.assertEquals(member.getUserId(), memberRepository.searchId(userSearchDTO));
    }

    @Test
    @DisplayName("아이디 찾기. 정상적인 정보인 경우. 이메일 기반 조회")
    void searchIdByEmail() {
        UserSearchDTO userSearchDTO = MemberFixture.createUserSearchDTOByEmail();

        Assertions.assertEquals(member.getUserId(), memberRepository.searchId(userSearchDTO));
    }

    @Test
    @DisplayName("아이디 찾기. 저장되지 않은 정보인 경우")
    void searchIdNotFound() {
        UserSearchDTO wrongSearchDTO = MemberFixture.createWrongUserSearchDTO();

        String result = Assertions.assertDoesNotThrow(() -> memberRepository.searchId(wrongSearchDTO));

        Assertions.assertNull(result);
    }

    @Test
    @DisplayName("비밀번호 수정 이전 사용자 정보 조회. 정상적인 정보인 경우")
    void searchPw() {
        UserSearchPwDTO searchDTO = MemberFixture.createUserSearchPwDTO();

        Assertions.assertEquals(1L, memberRepository.findByPassword(searchDTO));
    }

    @Test
    @DisplayName("비밀번호 수정 이전 사용자 정보 조회. 저장되지 않은 정보인 경우")
    void searchPwNotFound() {
        UserSearchPwDTO wrongSearchDTO = MemberFixture.createWrongUserSearchPwDTO();
        Long result = Assertions.assertDoesNotThrow(() -> memberRepository.findByPassword(wrongSearchDTO));

        Assertions.assertEquals(0L, result);
    }
}
