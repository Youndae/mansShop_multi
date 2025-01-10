package com.example.moduleuser.service;

import com.example.moduleauth.config.security.AuthConfig;
import com.example.moduleauth.model.dto.member.UserSearchDTO;
import com.example.moduleauth.model.dto.member.UserSearchPwDTO;
import com.example.moduleauth.repository.MemberRepository;
import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.enumuration.Result;
import com.example.moduleuser.ModuleUserApplication;
import com.example.moduleuser.fixture.MemberFixture;
import com.example.moduleuser.model.dto.member.in.JoinDTO;
import com.example.moduleuser.model.dto.member.in.UserCertificationDTO;
import com.example.moduleuser.model.dto.member.in.UserResetPwDTO;
import com.example.moduleuser.model.dto.member.out.UserSearchIdResponseDTO;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;


@SpringBootTest(classes = ModuleUserApplication.class)
@EntityScan(basePackages = "com.example.modulecommon")
@EnableJpaRepositories(basePackages = "com.example")
@ComponentScan(basePackages = {"com.example.moduleconfig", "com.example.modulecommon"})
@ActiveProfiles("integration-test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
@Import(AuthConfig.class)
public class MemberServiceIT {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private static final String checkDuplicatedResponseMessage = "duplicated";

    private static final String checkNoDuplicatesResponseMessage = "No duplicates";

    private Member member;

    @BeforeAll
    void init() {
        member = MemberFixture.createMember(0);
        memberRepository.save(member);
    }

    @Test
    @DisplayName("회원가입 성공")
    void joinProc() {
        JoinDTO joinDTO = MemberFixture.createJoinDTO();

        String result = Assertions.assertDoesNotThrow(() -> memberService.joinProc(joinDTO));

        Assertions.assertEquals(Result.OK.getResultKey(), result);
    }

    @Test
    @DisplayName("회원가입 시 동일한 사용자 아이디가 있는 경우.")
    void joinProcDuplicatedId() {
        JoinDTO joinDTO = new JoinDTO(member.getUserId(),
                                    member.getUserPw(),
                                    member.getUserName(),
                                    member.getNickname() + "1",
                                    member.getPhone(),
                                    member.getBirth().toString(),
                                    member.getUserEmail()
                            );

        Assertions.assertThrows(IllegalArgumentException.class, () -> memberService.joinProc(joinDTO));
    }

    @Test
    @DisplayName("회원가입 시 동일한 사용자 닉네임이 있는 경우.")
    void joinProcDuplicatedNickname() {
        JoinDTO joinDTO = new JoinDTO(member.getUserId() + "1",
                member.getUserPw(),
                member.getUserName(),
                member.getNickname(),
                member.getPhone(),
                member.getBirth().toString(),
                member.getUserEmail()
        );

        Assertions.assertThrows(IllegalArgumentException.class, () -> memberService.joinProc(joinDTO));
    }

    @Test
    @DisplayName("회원가입 시 아이디 중복 체크")
    void checkJoinId() {
        String userId = MemberFixture.createJoinDTO().userId();

        String result = Assertions.assertDoesNotThrow(() -> memberService.checkJoinId(userId));

        Assertions.assertEquals(checkNoDuplicatesResponseMessage, result);
    }

    @Test
    @DisplayName("회원가입 시 아이디 중복 체크. 중복된 아이디인 경우")
    void checkJoinIdDuplicated() {
        String result = Assertions.assertDoesNotThrow(() -> memberService.checkJoinId(member.getUserId()));

        Assertions.assertEquals(checkDuplicatedResponseMessage, result);
    }

    @Test
    @DisplayName("회원가입 시 닉네임 중복 체크")
    void checkJoinNickname() {
        JoinDTO fixture = MemberFixture.createJoinDTO();

        String result = Assertions.assertDoesNotThrow(() -> memberService.checkNickname(fixture.nickname(), fixture.userId()));

        Assertions.assertEquals(checkNoDuplicatesResponseMessage, result);
    }

    @Test
    @DisplayName("회원가입 시 닉네임 중복 체크. 중복된 닉네임인 경우")
    void checkJoinNicknameDuplicated() {
        String userId = MemberFixture.createJoinDTO().userId();
        String result = Assertions.assertDoesNotThrow(() -> memberService.checkNickname(member.getNickname(), userId));

        Assertions.assertEquals(checkDuplicatedResponseMessage, result);
    }

    @Test
    @DisplayName("회원가입 시 닉네임 중복 체크. 사용자가 현재 사용중인 닉네임인 경우.")
    void checkJoinSameNickname() {
        String result = Assertions.assertDoesNotThrow(() -> memberService.checkNickname(member.getNickname(), member.getUserId()));

        Assertions.assertEquals(checkNoDuplicatesResponseMessage, result);
    }

    @Test
    @DisplayName("아이디 찾기 요청. 연락처 기반")
    void searchIdByPhone() {
        UserSearchDTO searchDTO = MemberFixture.createUserSearchDTOByPhone(0);
        UserSearchIdResponseDTO result = Assertions.assertDoesNotThrow(() -> memberService.searchId(searchDTO));

        Assertions.assertEquals(member.getUserId(), result.userId());
        Assertions.assertEquals(Result.OK.getResultKey(), result.message());
    }

    @Test
    @DisplayName("아이디 찾기 요청. 연락처 기반. 정보가 없는 경우")
    void searchIdByPhoneNotFound() {
        UserSearchDTO searchDTO = MemberFixture.createUserSearchDTOByPhone(1);
        UserSearchIdResponseDTO result = Assertions.assertDoesNotThrow(() -> memberService.searchId(searchDTO));

        Assertions.assertNull(result.userId());
        Assertions.assertEquals(Result.NOTFOUND.getResultKey(), result.message());
    }

    @Test
    @DisplayName("아이디 찾기 요청. 이메일 기반")
    void searchIdByEmail() {
        UserSearchDTO searchDTO = MemberFixture.createUserSearchDTOByEmail(0);
        UserSearchIdResponseDTO result = Assertions.assertDoesNotThrow(() -> memberService.searchId(searchDTO));

        Assertions.assertEquals(member.getUserId(), result.userId());
        Assertions.assertEquals(Result.OK.getResultKey(), result.message());
    }

    @Test
    @DisplayName("아이디 찾기 요청. 이메일 기반. 정보가 없는 경우")
    void searchIdByEmailNotFound() {
        UserSearchDTO searchDTO = MemberFixture.createUserSearchDTOByEmail(1);
        UserSearchIdResponseDTO result = Assertions.assertDoesNotThrow(() -> memberService.searchId(searchDTO));

        Assertions.assertNull(result.userId());
        Assertions.assertEquals(Result.NOTFOUND.getResultKey(), result.message());
    }

    @Test
    @DisplayName("비밀번호 찾기 요청")
    void searchPw() {
        UserSearchPwDTO searchDTO = MemberFixture.createUserSearchPwDTO(0);
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();

        String result = Assertions.assertDoesNotThrow(() -> memberService.searchPw(searchDTO));
        String certificationNo = valueOperations.get(searchDTO.userId());

        Assertions.assertEquals(Result.OK.getResultKey(), result);
        Assertions.assertNotNull(certificationNo);

        redisTemplate.delete(searchDTO.userId());
    }

    @Test
    @DisplayName("인증번호 확인")
    void checkCertificationNo() {
        String userId = MemberFixture.createMember(0).getUserId();
        String certification = "123456";
        UserCertificationDTO certificationDTO = new UserCertificationDTO(userId, certification);
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        valueOperations.set(userId, certification);

        String result = Assertions.assertDoesNotThrow(() -> memberService.checkCertificationNo(certificationDTO));

        Assertions.assertEquals(Result.OK.getResultKey(), result);

        redisTemplate.delete(userId);
    }

    @Test
    @DisplayName("인증번호가 일치하지 않는 경우")
    void checkCertificationNoIsInvalid() {
        String userId = MemberFixture.createMember(0).getUserId();
        String certification = "123456";
        UserCertificationDTO certificationDTO = new UserCertificationDTO(userId, "123457");
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        valueOperations.set(userId, certification);

        String result = Assertions.assertDoesNotThrow(() -> memberService.checkCertificationNo(certificationDTO));

        Assertions.assertEquals(Result.FAIL.getResultKey(), result);

        redisTemplate.delete(userId);
    }

    @Test
    @DisplayName("인증번호 인증 이후 비밀번호 수정 요청")
    void resetPw() {
        String userId = MemberFixture.createMember(0).getUserId();
        String certification = "123456";
        String userPw = "testPw";
        UserResetPwDTO resetDTO = new UserResetPwDTO(userId, certification, userPw);
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        valueOperations.set(userId, certification);

        String result = Assertions.assertDoesNotThrow(() -> memberService.resetPw(resetDTO));

        Assertions.assertEquals(Result.OK.getResultKey(), result);

        Member memberData = memberRepository.findById(userId).orElseThrow(IllegalAccessError::new);

        Assertions.assertTrue(passwordEncoder.matches(userPw, memberData.getUserPw()));
    }

    @Test
    @DisplayName("인증번호 인증 이후 비밀번호 수정 요청시 인증번호가 일치하지 않는 경우")
    void resetPwFail() {
        String userId = MemberFixture.createMember(0).getUserId();
        String certification = "123456";
        String userPw = "testPw";
        UserResetPwDTO resetDTO = new UserResetPwDTO(userId, "123457", userPw);
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        valueOperations.set(userId, certification);

        String result = Assertions.assertDoesNotThrow(() -> memberService.resetPw(resetDTO));

        Assertions.assertEquals(Result.FAIL.getResultKey(), result);

        Member memberData = memberRepository.findById(userId).orElseThrow(IllegalAccessError::new);

        Assertions.assertFalse(passwordEncoder.matches(userPw, memberData.getUserPw()));
    }
}
