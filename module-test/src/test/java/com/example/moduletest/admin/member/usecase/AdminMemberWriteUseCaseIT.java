package com.example.moduletest.admin.member.usecase;

import com.example.modulecommon.fixture.MemberAndAuthFixture;
import com.example.modulecommon.model.dto.MemberAndAuthFixtureDTO;
import com.example.modulecommon.model.entity.Auth;
import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.enumuration.Result;
import com.example.moduletest.ModuleTestApplication;
import com.example.moduleuser.model.dto.admin.in.AdminPostPointDTO;
import com.example.moduleuser.repository.AuthRepository;
import com.example.moduleuser.repository.MemberRepository;
import com.example.moduleuser.usecase.admin.AdminMemberWriteUseCase;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = ModuleTestApplication.class)
@EntityScan(basePackages = "com.example.modulecommon")
@EnableJpaRepositories(basePackages = "com.example")
@ComponentScan(basePackages = {"com.example.moduleconfig", "com.example.modulecommon"})
@ActiveProfiles("test")
@Transactional
public class AdminMemberWriteUseCaseIT {

    @Autowired
    private AdminMemberWriteUseCase adminMemberWriteUseCase;

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
    @DisplayName(value = "회원 포인트 지급")
    void postPoint() {
        Member member = memberList.get(0);
        AdminPostPointDTO pointDTO = new AdminPostPointDTO(member.getUserId(), 100);

        String result = assertDoesNotThrow(() -> adminMemberWriteUseCase.postPoint(pointDTO));

        assertNotNull(result);
        assertEquals(Result.OK.getResultKey(), result);

        Member resultMember = memberRepository.findById(member.getUserId()).orElse(null);

        assertNotNull(resultMember);
        assertEquals(100, resultMember.getMemberPoint());
    }

    @Test
    @DisplayName(value = "회원 포인트 지급. 사용자가 존재하지 않는 경우")
    void postPointMemberNotFound() {
        AdminPostPointDTO pointDTO = new AdminPostPointDTO("noneMember", 100);

        assertThrows(IllegalArgumentException.class, () -> adminMemberWriteUseCase.postPoint(pointDTO));
    }
}
