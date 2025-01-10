package com.example.moduleuser.service;

import com.example.moduleauth.model.dto.member.UserSearchDTO;
import com.example.moduleauth.model.dto.member.UserSearchPwDTO;
import com.example.moduleauth.repository.MemberRepository;
import com.example.modulecommon.model.entity.Auth;
import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.enumuration.Result;
import com.example.moduleuser.ModuleUserApplication;
import com.example.moduleuser.fixture.MemberFixture;
import com.example.moduleuser.model.dto.member.in.JoinDTO;
import com.example.moduleuser.model.dto.member.in.UserCertificationDTO;
import com.example.moduleuser.model.dto.member.in.UserResetPwDTO;
import com.example.moduleuser.model.dto.member.out.UserSearchIdResponseDTO;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.mockito.Mockito.when;

@SpringBootTest(classes = ModuleUserApplication.class)
@EntityScan(basePackages = "com.example.modulecommon")
@EnableJpaRepositories(basePackages = "com.example")
@ComponentScan(basePackages = {"com.example.moduleconfig", "com.example.modulecommon"})
@ActiveProfiles("test")
public class MemberServiceUnitTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private MemberService memberService;

    private static final String checkDuplicatedResponseMessage = "duplicated";

    private static final String checkNoDuplicatesResponseMessage = "No duplicates";

    @Test
    @DisplayName("회원가입 성공")
    void joinProc() {
        JoinDTO joinDTO = MemberFixture.createJoinDTO();
        Member member = joinDTO.toEntity();
        member.addMemberAuth(new Auth().toMemberAuth());

        when(memberRepository.save(member)).thenReturn(member);

        String result = Assertions.assertDoesNotThrow(() -> memberService.joinProc(joinDTO));

        Assertions.assertEquals(Result.OK.getResultKey(), result);
    }

    @Test
    @DisplayName("가입 시 아이디 중복여부. 사용 가능한 아이디인 경우")
    void checkJoinId() {
        String userId = "testUser";
        
        when(memberRepository.findById(userId)).thenReturn(Optional.empty());
        
        String result = Assertions.assertDoesNotThrow(() -> memberService.checkJoinId(userId));
        
        Assertions.assertEquals(checkNoDuplicatesResponseMessage, result);
    }

    @Test
    @DisplayName("가입 시 아이디 중복 여부. 이미 존재하는 아이디인 경우")
    void checkJoinIdDuplicated() {
        Member member = MemberFixture.createMember(0);
        
        when(memberRepository.findById(member.getUserId())).thenReturn(Optional.of(member));
        
        String result = Assertions.assertDoesNotThrow(() -> memberService.checkJoinId(member.getUserId()));
        
        Assertions.assertEquals(checkDuplicatedResponseMessage, result);
    }

    @Test
    @DisplayName("가입 또는 정보 수정 시 닉네임 중복 여부. 사용가능한 닉네임인 경우")
    void checkNickname() {
        String nickname = "testNickname";
        
        when(memberRepository.findByNickname(nickname)).thenReturn(null);
        
        String result = Assertions.assertDoesNotThrow(() -> memberService.checkNickname(nickname, null));
        
        Assertions.assertEquals(checkNoDuplicatesResponseMessage, result);
    }

    @Test
    @DisplayName("가입 또는 정보 수정 시 닉네임 중복 여부. 자신의 수정 전 닉네임인 경우")
    void checkNicknameByUser() {
        Member member = MemberFixture.createMember(0);
        
        when(memberRepository.findByNickname(member.getNickname())).thenReturn(member);

        String result = Assertions.assertDoesNotThrow(() -> memberService.checkNickname(member.getNickname(), member.getUserId()));

        Assertions.assertEquals(checkNoDuplicatesResponseMessage, result);
    }

    @Test
    @DisplayName("가입 또는 정보 수정 시 닉네임 중복 여부. 사용할 수 없는 닉네임인 경우")
    void checkNicknameDuplicated() {
        Member member = MemberFixture.createMember(0);
        
        when(memberRepository.findByNickname(member.getNickname())).thenReturn(member);

        String result = Assertions.assertDoesNotThrow(() -> memberService.checkNickname(member.getNickname(), "tester"));

        Assertions.assertEquals(checkDuplicatedResponseMessage, result);
    }

    @Test
    @DisplayName("아이디 찾기. 연락처 기반")
    void searchIdByPhone() {
        UserSearchDTO searchDTO = MemberFixture.createUserSearchDTOByPhone(0);
        String userId = MemberFixture.createMember(0).getUserId();
        
        when(memberRepository.searchId(searchDTO)).thenReturn(userId);
        
        UserSearchIdResponseDTO result = Assertions.assertDoesNotThrow(() -> memberService.searchId(searchDTO));
        
        Assertions.assertEquals(userId, result.userId());
        Assertions.assertEquals(Result.OK.getResultKey(), result.message());
    }

    @Test
    @DisplayName("아이디 찾기. 연락처 기반. 정보가 존재하지 않는경우")
    void searchIdByPhoneNotFound() {
        UserSearchDTO searchDTO = MemberFixture.createUserSearchDTOByPhone(0);

        when(memberRepository.searchId(searchDTO)).thenReturn(null);

        UserSearchIdResponseDTO result = Assertions.assertDoesNotThrow(() -> memberService.searchId(searchDTO));
        
        Assertions.assertEquals(Result.NOTFOUND.getResultKey(), result.message());
    }

    @Test
    @DisplayName("아이디 찾기. 이메일 기반")
    void searchIdByEmail() {
        UserSearchDTO searchDTO = MemberFixture.createUserSearchDTOByEmail(0);
        String userId = MemberFixture.createMember(0).getUserId();

        when(memberRepository.searchId(searchDTO)).thenReturn(userId);

        UserSearchIdResponseDTO result = Assertions.assertDoesNotThrow(() -> memberService.searchId(searchDTO));

        Assertions.assertEquals(userId, result.userId());
        Assertions.assertEquals(Result.OK.getResultKey(), result.message());
    }

    @Test
    @DisplayName("아이디 찾기. 이메일 기반. 정보가 존재하지 않는 경우")
    void searchIdByEmailNotFound() {
        UserSearchDTO searchDTO = MemberFixture.createUserSearchDTOByEmail(0);

        when(memberRepository.searchId(searchDTO)).thenReturn(null);

        UserSearchIdResponseDTO result = Assertions.assertDoesNotThrow(() -> memberService.searchId(searchDTO));
        
        Assertions.assertEquals(Result.NOTFOUND.getResultKey(), result.message());
    }

    @Test
    @DisplayName("비밀번호 찾기 요청 시 정보 확인 후 인증번호 메일 전송")
    void searchPw() throws MessagingException {
        UserSearchPwDTO searchDTO = MemberFixture.createUserSearchPwDTO(0);
        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(memberRepository.findByPassword(searchDTO)).thenReturn(1L);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(memberService.createEmailForm(searchDTO.userEmail(), 123456)).thenReturn(mimeMessage);

        String result = Assertions.assertDoesNotThrow(() -> memberService.searchPw(searchDTO));

        Assertions.assertEquals(Result.OK.getResultKey(), result);
    }

    @Test
    @DisplayName("비밀번호 찾기 요청 시 정보 확인 후 인증번호 메일 전송. 사용자 정보가 존재하지 않는 경우")
    void searchPwNotFound() {
        UserSearchPwDTO searchDTO = MemberFixture.createUserSearchPwDTO(0);
        when(memberRepository.findByPassword(searchDTO)).thenReturn(0L);

        String result = Assertions.assertDoesNotThrow(() -> memberService.searchPw(searchDTO));

        Assertions.assertEquals(Result.NOTFOUND.getResultKey(), result);
    }

    @Test
    @DisplayName("비밀번호 찾기 요청 시 정보 확인 후 인증번호 메일 전송. 메일 전송 실패")
    void searchPwThrowMessagingException() throws MessagingException {
        UserSearchPwDTO searchDTO = MemberFixture.createUserSearchPwDTO(0);
        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(memberRepository.findByPassword(searchDTO)).thenReturn(1L);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(memberService.createEmailForm(searchDTO.userEmail(), 123456)).thenAnswer(invocation -> { throw new MessagingException();});

        String result = Assertions.assertDoesNotThrow(() -> memberService.searchPw(searchDTO));

        Assertions.assertEquals(Result.FAIL.getResultKey(), result);
    }

    @Test
    @DisplayName("비밀번호 찾기 과정 중 인증번호 입력 시 검증 처리.")
    void checkCertificationNo() {
        UserCertificationDTO certificationDTO = new UserCertificationDTO("testUser", "123456");

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(certificationDTO.userId())).thenReturn(certificationDTO.certification());

        String result = Assertions.assertDoesNotThrow(() -> memberService.checkCertificationNo(certificationDTO));

        Assertions.assertEquals(Result.OK.getResultKey(), result);
    }

    @Test
    @DisplayName("비밀번호 찾기 과정 중 인증번호 입력 시 검증 처리. Redis 조회 과정 중 예외 발생")
    void checkCertificationNoError() {
        UserCertificationDTO certificationDTO = new UserCertificationDTO("testUser", "123456");

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(certificationDTO.userId())).thenAnswer(invocation -> {throw new Exception();});

        String result = Assertions.assertDoesNotThrow(() -> memberService.checkCertificationNo(certificationDTO));

        Assertions.assertEquals(Result.ERROR.getResultKey(), result);
    }

    @Test
    @DisplayName("비밀번호 찾기 과정 중 인증번호 입력 시 검증 처리. Redis 데이터와 인증번호 불일치")
    void checkCertificationNoInvalidRedisData() {
        UserCertificationDTO certificationDTO = new UserCertificationDTO("testUser", "123456");

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(certificationDTO.userId())).thenReturn("123457");

        String result = Assertions.assertDoesNotThrow(() -> memberService.checkCertificationNo(certificationDTO));

        Assertions.assertEquals(Result.FAIL.getResultKey(), result);
    }

    @Test
    @DisplayName("인증번호 조회 과정에서 오류가 발생하는 경우")
    void checkCertificationError() {
        String userId = MemberFixture.createMember(0).getUserId();
        String certification = "123456";
        UserCertificationDTO certificationDTO = new UserCertificationDTO(userId, certification);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(userId)).thenThrow(new RuntimeException());

        String result = memberService.checkCertificationNo(certificationDTO);

        Assertions.assertEquals(Result.ERROR.getResultKey(), result);
    }

    @Test
    @DisplayName("비밀번호 변경 요청")
    void resetPw() {
        UserResetPwDTO resetDTO = new UserResetPwDTO("testUser", "123456", "testUserPw");
        Member member = MemberFixture.createMember(0);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(resetDTO.userId())).thenReturn(resetDTO.certification());
        when(redisTemplate.delete(resetDTO.userId())).thenReturn(true);
        when(memberRepository.findById(resetDTO.userId())).thenReturn(Optional.of(member));
        when(memberRepository.save(member)).thenReturn(member);

        String result = Assertions.assertDoesNotThrow(() -> memberService.resetPw(resetDTO));

        Assertions.assertEquals(Result.OK.getResultKey(), result);
    }

    @Test
    @DisplayName("비밀번호 변경 요청. Redis 데이터와 인증번호 불일치")
    void resetPwInvalidRedisData() {
        UserResetPwDTO resetDTO = new UserResetPwDTO("testUser", "123456", "testUserPw");
        Member member = MemberFixture.createMember(0);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(resetDTO.userId())).thenReturn("123457");
        when(redisTemplate.delete(resetDTO.userId())).thenReturn(true);

        String result = Assertions.assertDoesNotThrow(() -> memberService.resetPw(resetDTO));

        Assertions.assertEquals(Result.FAIL.getResultKey(), result);
    }

    @Test
    @DisplayName("비밀번호 변경 요청. 사용자 정보가 존재하지 않는 경우")
    void resetPwNotFoundUser() {
        UserResetPwDTO resetDTO = new UserResetPwDTO("testUser", "123456", "testUserPw");
        Member member = MemberFixture.createMember(0);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(resetDTO.userId())).thenReturn(resetDTO.certification());
        when(redisTemplate.delete(resetDTO.userId())).thenReturn(true);
        when(memberRepository.findById(resetDTO.userId())).thenAnswer(invocation -> {throw new IllegalAccessError();});

        Assertions.assertThrows(IllegalAccessError.class, () -> memberService.resetPw(resetDTO));
    }
}
