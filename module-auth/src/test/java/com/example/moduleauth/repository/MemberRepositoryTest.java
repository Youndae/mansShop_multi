package com.example.moduleauth.repository;

import com.example.moduleauth.fixture.MemberFixture;
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

    @BeforeEach
    void init() {
        memberRepository.save(MemberFixture.createLocalMember());
    }

    @Test
    @DisplayName(value = "로컬 가입 유저 조회")
    void findLocalMemberSuccess() {
        Member member = MemberFixture.createLocalMember();

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
}
