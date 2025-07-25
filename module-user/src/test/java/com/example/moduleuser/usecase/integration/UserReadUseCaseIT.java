package com.example.moduleuser.usecase.integration;


import com.example.moduleauth.model.dto.member.UserSearchDTO;
import com.example.modulecommon.fixture.MemberAndAuthFixture;
import com.example.modulecommon.model.dto.MemberAndAuthFixtureDTO;
import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.enumuration.Result;
import com.example.moduleuser.ModuleUserApplication;
import com.example.moduleuser.model.dto.member.out.UserSearchIdResponseDTO;
import com.example.moduleuser.repository.AuthRepository;
import com.example.moduleuser.repository.MemberRepository;
import com.example.moduleuser.usecase.UserReadUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = ModuleUserApplication.class)
@EntityScan(basePackages = "com.example.modulecommon")
@EnableJpaRepositories(basePackages = "com.example")
@ComponentScan(basePackages = {"com.example.moduleconfig", "com.example.modulecommon"})
@ActiveProfiles("test")
@Transactional
public class UserReadUseCaseIT {

    @Autowired
    private UserReadUseCase userReadUseCase;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private AuthRepository authRepository;

    private List<Member> memberList;

    @BeforeEach
    void init() {
        MemberAndAuthFixtureDTO memberAndAuthFixture = MemberAndAuthFixture.createDefaultMember(1);
        memberList = memberAndAuthFixture.memberList();
        memberRepository.saveAll(memberList);
        authRepository.saveAll(memberAndAuthFixture.authList());
    }

    @Test
    @DisplayName(value = "아이디 중복 체크")
    void checkJoinUserId() {
        String result = assertDoesNotThrow(() -> userReadUseCase.checkJoinUserId("newUserId"));

        assertNotNull(result);
        assertEquals(Result.NO_DUPLICATE.getResultKey(), result);
    }

    @Test
    @DisplayName(value = "아이디 중복 체크. 중복인 경우")
    void checkJoinUserIdDuplicated() {
        Member member = memberList.get(0);
        String result = assertDoesNotThrow(() -> userReadUseCase.checkJoinUserId(member.getUserId()));

        assertNotNull(result);
        assertEquals(Result.DUPLICATE.getResultKey(), result);
    }

    @Test
    @DisplayName(value = "닉네임 중복 체크")
    void checkNickname() {
        String result = assertDoesNotThrow(() -> userReadUseCase.checkNickname("newUserNickname", null));

        assertNotNull(result);
        assertEquals(Result.NO_DUPLICATE.getResultKey(), result);
    }

    @Test
    @DisplayName(value = "닉네임 중복 체크. 닉네임이 존재하는 사용자가 자신의 닉네임을 그대로 체크하는 경우")
    void checkNicknameOriginNicknameCheck() {
        Member member = memberList.get(0);
        Principal principal = member::getUserId;
        String result = assertDoesNotThrow(() -> userReadUseCase.checkNickname(member.getNickname(), principal));

        assertNotNull(result);
        assertEquals(Result.NO_DUPLICATE.getResultKey(), result);
    }

    @Test
    @DisplayName(value = "닉네임 중복 체크. 중복인 경우")
    void checkNicknameDuplicated() {
        Member member = memberList.get(0);
        String result = assertDoesNotThrow(() -> userReadUseCase.checkNickname(member.getNickname(), null));

        assertNotNull(result);
        assertEquals(Result.DUPLICATE.getResultKey(), result);
    }

    @Test
    @DisplayName(value = "아이디 찾기. 연락처 기반")
    void searchIdByPhone() {
        Member member = memberList.get(0);
        String memberPhone = member.getPhone().replaceAll("-", "");
        UserSearchDTO searchDTO = new UserSearchDTO(member.getUserName(), memberPhone, null);

        UserSearchIdResponseDTO result = assertDoesNotThrow(() -> userReadUseCase.searchId(searchDTO));

        assertNotNull(result);
        assertEquals(member.getUserId(), result.userId());
        assertEquals(Result.OK.getResultKey(), result.message());
    }

    @Test
    @DisplayName(value = "아이디 찾기. 이메일 기반")
    void searchIdByEmail() {
        Member member = memberList.get(0);
        UserSearchDTO searchDTO = new UserSearchDTO(member.getUserName(), null, member.getUserEmail());

        UserSearchIdResponseDTO result = assertDoesNotThrow(() -> userReadUseCase.searchId(searchDTO));

        assertNotNull(result);
        assertEquals(member.getUserId(), result.userId());
        assertEquals(Result.OK.getResultKey(), result.message());
    }

    @Test
    @DisplayName(value = "아이디 찾기. 연락처 기반. 데이터가 없는 경우")
    void searchIdByPhoneNotFound() {
        UserSearchDTO searchDTO = new UserSearchDTO("noneUser", "01011119999", null);

        UserSearchIdResponseDTO result = assertDoesNotThrow(() -> userReadUseCase.searchId(searchDTO));

        assertNotNull(result);
        assertNull(result.userId());
        assertEquals(Result.NOTFOUND.getResultKey(), result.message());
    }

    @Test
    @DisplayName(value = "아이디 찾기. 이메일 기반. 데이터가 없는 경우")
    void searchIdByEmailNotFound() {
        UserSearchDTO searchDTO = new UserSearchDTO("noneUser", null, "noneUser@none.com");

        UserSearchIdResponseDTO result = assertDoesNotThrow(() -> userReadUseCase.searchId(searchDTO));

        assertNotNull(result);
        assertNull(result.userId());
        assertEquals(Result.NOTFOUND.getResultKey(), result.message());
    }
}
