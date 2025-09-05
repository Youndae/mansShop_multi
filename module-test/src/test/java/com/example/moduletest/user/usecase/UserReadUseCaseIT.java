package com.example.moduletest.user.usecase;

import com.example.modulecommon.customException.CustomConflictException;
import com.example.modulecommon.customException.CustomDuplicateException;
import com.example.modulecommon.customException.CustomNotFoundException;
import com.example.moduleuser.model.dto.member.in.UserSearchDTO;
import com.example.modulecommon.fixture.MemberAndAuthFixture;
import com.example.modulecommon.model.dto.MemberAndAuthFixtureDTO;
import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.enumuration.Result;
import com.example.moduletest.ModuleTestApplication;
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

@SpringBootTest(classes = ModuleTestApplication.class)
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
        assertDoesNotThrow(() -> userReadUseCase.checkJoinUserId("newUserId"));
    }

    @Test
    @DisplayName(value = "아이디 중복 체크. 중복인 경우")
    void checkJoinUserIdDuplicated() {
        Member member = memberList.get(0);
        assertThrows(CustomDuplicateException.class, () -> userReadUseCase.checkJoinUserId(member.getUserId()));
    }

    @Test
    @DisplayName(value = "닉네임 중복 체크")
    void checkNickname() {
        assertDoesNotThrow(() -> userReadUseCase.checkNickname("newUserNickname", null));
    }

    @Test
    @DisplayName(value = "닉네임 중복 체크. 닉네임이 존재하는 사용자가 자신의 닉네임을 그대로 체크하는 경우")
    void checkNicknameOriginNicknameCheck() {
        Member member = memberList.get(0);
        Principal principal = member::getUserId;
        assertDoesNotThrow(() -> userReadUseCase.checkNickname(member.getNickname(), principal));
    }

    @Test
    @DisplayName(value = "닉네임 중복 체크. 중복인 경우")
    void checkNicknameDuplicated() {
        Member member = memberList.get(0);
        assertThrows(CustomDuplicateException.class, () -> userReadUseCase.checkNickname(member.getNickname(), null));
    }

    @Test
    @DisplayName(value = "아이디 찾기. 연락처 기반")
    void searchIdByPhone() {
        Member member = memberList.get(0);
        String memberPhone = member.getPhone().replaceAll("-", "");
        UserSearchDTO searchDTO = new UserSearchDTO(member.getUserName(), memberPhone, null);

        String result = assertDoesNotThrow(() -> userReadUseCase.searchId(searchDTO));

        assertNotNull(result);
        assertEquals(member.getUserId(), result);
    }

    @Test
    @DisplayName(value = "아이디 찾기. 이메일 기반")
    void searchIdByEmail() {
        Member member = memberList.get(0);
        UserSearchDTO searchDTO = new UserSearchDTO(member.getUserName(), null, member.getUserEmail());

        String result = assertDoesNotThrow(() -> userReadUseCase.searchId(searchDTO));

        assertNotNull(result);
        assertEquals(member.getUserId(), result);
    }

    @Test
    @DisplayName(value = "아이디 찾기. 연락처 기반. 데이터가 없는 경우")
    void searchIdByPhoneNotFound() {
        UserSearchDTO searchDTO = new UserSearchDTO("noneUser", "01011119999", null);

        assertThrows(CustomNotFoundException.class, () -> userReadUseCase.searchId(searchDTO));
    }

    @Test
    @DisplayName(value = "아이디 찾기. 이메일 기반. 데이터가 없는 경우")
    void searchIdByEmailNotFound() {
        UserSearchDTO searchDTO = new UserSearchDTO("noneUser", null, "noneUser@none.com");

        assertThrows(CustomNotFoundException.class, () -> userReadUseCase.searchId(searchDTO));
    }
}
