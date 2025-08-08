package com.example.moduletest.admin.member.usecase;

import com.example.modulecommon.fixture.MemberAndAuthFixture;
import com.example.modulecommon.model.dto.MemberAndAuthFixtureDTO;
import com.example.modulecommon.model.entity.Auth;
import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.utils.PaginationUtils;
import com.example.moduletest.ModuleTestApplication;
import com.example.moduleuser.model.dto.admin.out.AdminMemberDTO;
import com.example.moduleuser.model.dto.admin.page.AdminMemberPageDTO;
import com.example.moduleuser.repository.AuthRepository;
import com.example.moduleuser.repository.MemberRepository;
import com.example.moduleuser.usecase.admin.AdminMemberReadUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = ModuleTestApplication.class)
@EntityScan(basePackages = "com.example.modulecommon")
@EnableJpaRepositories(basePackages = "com.example")
@ComponentScan(basePackages = {"com.example.moduleconfig", "com.example.modulecommon"})
@ActiveProfiles("test")
@Transactional
public class AdminMemberReadUseCaseIT {

    @Autowired
    private AdminMemberReadUseCase adminMemberReadUseCase;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private AuthRepository authRepository;

    private List<Member> memberList;

    @BeforeEach
    void init() {
        MemberAndAuthFixtureDTO memberFixture = MemberAndAuthFixture.createDefaultMember(30);
        memberList = memberFixture.memberList();
        List<Auth> authList = memberFixture.authList();

        for(int i = 0; i < memberList.size(); i++) {
            try {
                Thread.sleep(5);
                memberRepository.save(memberList.get(i));
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }

        authRepository.saveAll(authList);
    }

    @Test
    @DisplayName(value = "회원 목록 조회")
    void getMemberList() {
        AdminMemberPageDTO pageDTO = new AdminMemberPageDTO(1);
        int totalPages = PaginationUtils.getTotalPages(memberList.size(), pageDTO.amount());

        Page<AdminMemberDTO> result = assertDoesNotThrow(() -> adminMemberReadUseCase.getAdminMemberList(pageDTO));

        assertNotNull(result);
        assertFalse(result.getContent().isEmpty());
        assertFalse(result.getTotalElements() < 1);
        assertEquals(memberList.size(), result.getTotalElements());
        assertEquals(pageDTO.amount(), result.getContent().size());
        assertEquals(totalPages, result.getTotalPages());
        assertEquals(pageDTO.page() - 1, result.getNumber());
    }

    @Test
    @DisplayName(value = "회원 목록 조회. 사용자 아이디 기반 검색")
    void getMemberListSearchUserId() {
        Member searchMember = memberList.get(0);
        AdminMemberPageDTO pageDTO = new AdminMemberPageDTO(searchMember.getUserId(), "userId", 1);

        Page<AdminMemberDTO> result = assertDoesNotThrow(() -> adminMemberReadUseCase.getAdminMemberList(pageDTO));

        assertNotNull(result);
        assertFalse(result.getContent().isEmpty());
        assertFalse(result.getTotalElements() < 1);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals(1, result.getTotalPages());
        assertEquals(pageDTO.page() - 1, result.getNumber());

        AdminMemberDTO resultContent = result.getContent().get(0);

        assertEquals(searchMember.getUserId(), resultContent.userId());
        assertEquals(searchMember.getUserName(), resultContent.userName());
        assertEquals(searchMember.getNickname(), resultContent.nickname());
        assertEquals(searchMember.getUserEmail(), resultContent.email());
        assertEquals(searchMember.getBirth(), resultContent.birth());
    }

    @Test
    @DisplayName(value = "회원 목록 조회. 사용자 이름 기반 검색")
    void getMemberListSearchUserName() {
        Member searchMember = memberList.get(0);
        AdminMemberPageDTO pageDTO = new AdminMemberPageDTO(searchMember.getUserName(), "userName", 1);

        Page<AdminMemberDTO> result = assertDoesNotThrow(() -> adminMemberReadUseCase.getAdminMemberList(pageDTO));

        assertNotNull(result);
        assertFalse(result.getContent().isEmpty());
        assertFalse(result.getTotalElements() < 1);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals(1, result.getTotalPages());
        assertEquals(pageDTO.page() - 1, result.getNumber());

        AdminMemberDTO resultContent = result.getContent().get(0);

        assertEquals(searchMember.getUserId(), resultContent.userId());
        assertEquals(searchMember.getUserName(), resultContent.userName());
        assertEquals(searchMember.getNickname(), resultContent.nickname());
        assertEquals(searchMember.getUserEmail(), resultContent.email());
        assertEquals(searchMember.getBirth(), resultContent.birth());
    }

    @Test
    @DisplayName(value = "회원 목록 조회. 사용자 닉네임 기반 검색")
    void getMemberListSearchNickName() {
        Member searchMember = memberList.get(0);
        AdminMemberPageDTO pageDTO = new AdminMemberPageDTO(searchMember.getNickname(), "nickname", 1);

        Page<AdminMemberDTO> result = assertDoesNotThrow(() -> adminMemberReadUseCase.getAdminMemberList(pageDTO));

        assertNotNull(result);
        assertFalse(result.getContent().isEmpty());
        assertFalse(result.getTotalElements() < 1);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals(1, result.getTotalPages());
        assertEquals(pageDTO.page() - 1, result.getNumber());

        AdminMemberDTO resultContent = result.getContent().get(0);

        assertEquals(searchMember.getUserId(), resultContent.userId());
        assertEquals(searchMember.getUserName(), resultContent.userName());
        assertEquals(searchMember.getNickname(), resultContent.nickname());
        assertEquals(searchMember.getUserEmail(), resultContent.email());
        assertEquals(searchMember.getBirth(), resultContent.birth());
    }

    @Test
    @DisplayName(value = "회원 목록 조회. 데이터가 없는 경우")
    void getMemberListEmpty() {
        AdminMemberPageDTO pageDTO = new AdminMemberPageDTO("noneMember", "nickname", 1);

        Page<AdminMemberDTO> result = assertDoesNotThrow(() -> adminMemberReadUseCase.getAdminMemberList(pageDTO));

        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());
    }
}
