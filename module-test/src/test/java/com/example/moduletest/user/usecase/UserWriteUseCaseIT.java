package com.example.moduletest.user.usecase;

import com.example.moduleauthapi.service.JWTTokenProvider;
import com.example.modulecommon.customException.CustomBadCredentialsException;
import com.example.modulecommon.customException.CustomNotFoundException;
import com.example.modulecommon.fixture.MemberAndAuthFixture;
import com.example.modulecommon.model.dto.MemberAndAuthFixtureDTO;
import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.enumuration.Result;
import com.example.moduletest.ModuleTestApplication;
import com.example.moduletest.utils.MailHogUtils;
import com.example.moduleuser.model.dto.member.in.*;
import com.example.moduleuser.repository.AuthRepository;
import com.example.moduleuser.repository.MemberRepository;
import com.example.moduleuser.usecase.UserWriteUseCase;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = ModuleTestApplication.class)
@EntityScan(basePackages = "com.example.modulecommon")
@EnableJpaRepositories(basePackages = "com.example")
@ComponentScan(basePackages = {"com.example.moduleconfig", "com.example.modulecommon"})
@ActiveProfiles("test")
@Transactional
public class UserWriteUseCaseIT {

    @Autowired
    private UserWriteUseCase userWriteUseCase;

    @Autowired
    private EntityManager em;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private AuthRepository authRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private JWTTokenProvider tokenProvider;

    @Autowired
    private MailHogUtils mailHogUtils;

    private List<Member> memberList;

    @BeforeEach
    void init() {
        MemberAndAuthFixtureDTO memberAndAuthFixture = MemberAndAuthFixture.createDefaultMember(1);
        memberList = memberAndAuthFixture.memberList();
        memberRepository.saveAll(memberList);
        authRepository.saveAll(memberAndAuthFixture.authList());

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName(value = "회원가입 요청")
    void postJoin() {
        JoinDTO joinDTO = new JoinDTO(
                "joinTestUser",
                "joinTestPassword1",
                "joinTester",
                "joinNickname",
                "01098765432",
                "2000/01/01",
                "joinTester@join.com"
        );
        LocalDate birth = LocalDate.of(2000, 1, 1);
        assertDoesNotThrow(() -> userWriteUseCase.joinProc(joinDTO));

        Member joinMember = memberRepository.findByLocalUserId(joinDTO.userId());

        assertNotNull(joinMember);
        assertEquals(joinDTO.userId(), joinMember.getUserId());
        assertEquals(joinDTO.userName(), joinMember.getUserName());
        assertEquals(joinDTO.nickname(), joinMember.getNickname());
        assertEquals(joinDTO.phone(), joinMember.getPhone().replaceAll("-", ""));
        assertEquals(birth, joinMember.getBirth());
        assertEquals(joinDTO.userEmail(), joinMember.getUserEmail());
    }

    @Test
    @DisplayName(value = "로그인 요청")
    void postLogin() {
        Member member = memberList.get(0);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertDoesNotThrow(() -> userWriteUseCase.loginProc(member.getUserId(), request, response));

        String accessToken = response.getHeader("Authorization").substring(6);
        Map<String, String> cookieMap = response.getHeaders("Set-Cookie").stream()
                .map(header -> header.split(";", 2)[0])
                .map(kv -> kv.split("=", 2))
                .filter(arr -> arr.length == 2)
                .collect(Collectors.toMap(arr -> arr[0], arr -> arr[1]));

        String refreshToken = cookieMap.get("Authorization_Refresh").substring(6);
        String ino = cookieMap.get("Authorization_ino");

        String redisAtKey = "at" + ino + member.getUserId();
        String redisRtKey = "rt" + ino + member.getUserId();

        String redisAtValue = redisTemplate.opsForValue().get(redisAtKey);
        String redisRtValue = redisTemplate.opsForValue().get(redisRtKey);

        assertNotNull(redisAtValue);
        assertNotNull(redisRtValue);
        assertEquals(accessToken, redisAtValue);
        assertEquals(refreshToken, redisRtValue);

        redisTemplate.delete(redisAtKey);
        redisTemplate.delete(redisRtKey);
    }

    @Test
    @DisplayName(value = "로그아웃 요청")
    void postLogout() {
        String ino = "12341234";
        String accessToken = "testaccesstokenvalue";
        String refreshToken = "testrefreshtokenvalue";
        String userId = "logoutTester";
        String atKey = "at" + ino + userId;
        String rtKey = "rt" + ino + userId;

        redisTemplate.opsForValue().set(atKey, accessToken);
        redisTemplate.opsForValue().set(rtKey, refreshToken);

        LogoutDTO logoutDTO = new LogoutDTO(accessToken, ino, userId);
        MockHttpServletResponse response = new MockHttpServletResponse();

        String result = assertDoesNotThrow(() -> userWriteUseCase.logoutProc(logoutDTO, response));

        assertNotNull(result);
        assertEquals(Result.OK.getResultKey(), result);

        List<String> cookies = response.getHeaders("Set-Cookie");

        boolean refreshCookie = cookies.stream()
                .anyMatch(v ->
                        v.startsWith("Authorization_Refresh=")
                                && v.contains("Max-Age=0")
                );

        boolean inoCookie = cookies.stream()
                .anyMatch(v ->
                        v.startsWith("Authorization_ino=")
                                && v.contains("Max-Age=0")
                );

        assertTrue(refreshCookie);
        assertTrue(inoCookie);

        String accessRedisValue = redisTemplate.opsForValue().get(atKey);
        String refreshRedisValue = redisTemplate.opsForValue().get(rtKey);

        assertNull(accessRedisValue);
        assertNull(refreshRedisValue);
    }

    @Test
    @DisplayName(value = "임시토큰을 발행받은 OAuth2 사용자 정식 토큰 발행")
    void oAuthUserIssueToken() {
        Member member = memberList.get(0);
        MockHttpServletResponse temporaryResponse = new MockHttpServletResponse();

        tokenProvider.createTemporaryToken(member.getUserId(), temporaryResponse);
        String temporaryToken = temporaryResponse.getHeader("Set-Cookie").split(";", 2)[0].split("=", 2)[1];

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("temporary", temporaryToken));
        MockHttpServletResponse newResponse = new MockHttpServletResponse();

        assertDoesNotThrow(() -> userWriteUseCase.issueOAuthUserToken(request, newResponse));

        String accessToken = newResponse.getHeader("Authorization").substring(6);
        Map<String, String> cookieMap = newResponse.getHeaders("Set-Cookie").stream()
                .map(header -> header.split(";", 2)[0])
                .map(kv -> kv.split("=", 2))
                .filter(arr -> arr.length == 2)
                .collect(Collectors.toMap(arr -> arr[0], arr -> arr[1]));

        String refreshToken = cookieMap.get("Authorization_Refresh").substring(6);
        String ino = cookieMap.get("Authorization_ino");

        assertNotNull(accessToken);
        assertNotNull(refreshToken);
        assertNotNull(ino);

        String redisAtKey = "at" + ino + member.getUserId();
        String redisRtKey = "rt" + ino + member.getUserId();

        String redisAtValue = redisTemplate.opsForValue().get(redisAtKey);
        String redisRtValue = redisTemplate.opsForValue().get(redisRtKey);

        assertEquals(accessToken, redisAtValue);
        assertEquals(refreshToken, redisRtValue);

        redisTemplate.delete(redisAtKey);
        redisTemplate.delete(redisRtKey);
    }

    @Test
    @DisplayName(value = "임시토큰을 발행받은 OAuth2 사용자 정식 토큰 발행. 임시 토큰 쿠키가 없는 경우")
    void oAuthUserIssueTokenTemporaryCookieIsNull() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockHttpServletRequest request = new MockHttpServletRequest();

        assertThrows(
                CustomBadCredentialsException.class,
                () -> userWriteUseCase.issueOAuthUserToken(request, response)
        );
    }

    @Test
    @DisplayName(value = "임시토큰을 발행받은 OAuth2 사용자 정식 토큰 발행. 임시 토큰 검증이 실패한 경우")
    void oAuthUserIssueTokenWrongToken() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("temporary", "wrongToken"));

        assertThrows(
                CustomBadCredentialsException.class,
                () -> userWriteUseCase.issueOAuthUserToken(request, response)
        );
    }

    //메일전송은 mailhog로 처리
    @Test
    @DisplayName(value = "비밀번호 찾기 요청")
    void searchPW() throws Exception {
        Member member = memberList.get(0);
        UserSearchPwDTO searchPwDTO = new UserSearchPwDTO(member.getUserId(), member.getUserName(), member.getUserEmail());

        assertDoesNotThrow(() -> userWriteUseCase.searchPassword(searchPwDTO));

        String redisCertificationValue = redisTemplate.opsForValue().get(member.getUserId());
        assertNotNull(redisCertificationValue);

        redisTemplate.delete(member.getUserId());

        String mailCertification = mailHogUtils.getCertificationNumberByMailHog();
        assertEquals(redisCertificationValue, mailCertification);
        mailHogUtils.deleteMailHog();
    }

    @Test
    @DisplayName(value = "비밀번호 찾기 요청. 데이터가 없는 경우")
    void searchPWUserNotFound() {
        UserSearchPwDTO searchPwDTO = new UserSearchPwDTO("noneUserId", "noneUserName", "noneUserEmail");

        assertThrows(CustomNotFoundException.class, () -> userWriteUseCase.searchPassword(searchPwDTO));
    }

    @Test
    @DisplayName(value = "비밀번호 찾기 인증번호 확인")
    void checkCertificationNo() {
        Member member = memberList.get(0);
        String certificationFixture = "102030";
        UserCertificationDTO certificationDTO = new UserCertificationDTO(member.getUserId(), certificationFixture);
        redisTemplate.opsForValue().set(member.getUserId(), certificationFixture);

        assertDoesNotThrow(() -> userWriteUseCase.checkCertificationNo(certificationDTO));

        redisTemplate.delete(member.getUserId());
    }

    @Test
    @DisplayName(value = "비밀번호 찾기 인증번호 확인. 인증번호가 일치하지 않는 경우")
    void checkCertificationNoNotEquals() {
        Member member = memberList.get(0);
        String certificationFixture = "102030";
        UserCertificationDTO certificationDTO = new UserCertificationDTO(member.getUserId(), "102031");
        redisTemplate.opsForValue().set(member.getUserId(), certificationFixture);

        assertThrows(CustomBadCredentialsException.class, () -> userWriteUseCase.checkCertificationNo(certificationDTO));

        redisTemplate.delete(member.getUserId());
    }

    @Test
    @DisplayName(value = "비밀번호 변경")
    void resetPw() {
        Member member = memberList.get(0);
        String certificationFixture = "102030";
        String newUserPw = "5678";
        UserResetPwDTO resetPwDTO = new UserResetPwDTO(member.getUserId(), certificationFixture, newUserPw);
        redisTemplate.opsForValue().set(member.getUserId(), certificationFixture);

        assertDoesNotThrow(() -> userWriteUseCase.resetPw(resetPwDTO));

        Member patchMember = memberRepository.findByLocalUserId(member.getUserId());
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        assertTrue(encoder.matches(newUserPw, patchMember.getUserPw()));

        String redisCertificationValue = redisTemplate.opsForValue().get(member.getUserId());

        assertNull(redisCertificationValue);
    }

    @Test
    @DisplayName(value = "비밀번호 변경. 인증번호가 일치하지 않는 경우")
    void resetPwCertificationNotEquals() {
        Member member = memberList.get(0);
        String certificationFixture = "102030";
        String newUserPw = "5678";
        UserResetPwDTO resetPwDTO = new UserResetPwDTO(member.getUserId(), "102031", newUserPw);
        redisTemplate.opsForValue().set(member.getUserId(), certificationFixture);

        assertThrows(CustomBadCredentialsException.class, () -> userWriteUseCase.resetPw(resetPwDTO));

        Member patchMember = memberRepository.findByLocalUserId(member.getUserId());

        assertEquals(member.getUserPw(), patchMember.getUserPw());

        String redisCertificationValue = redisTemplate.opsForValue().get(member.getUserId());

        assertNull(redisCertificationValue);
    }

    @Test
    @DisplayName(value = "비밀번호 변경. 인증번호가 Redis에 존재하지 않는 경우")
    void resetPwCertificationIsNull() {
        Member member = memberList.get(0);
        String certificationFixture = "102030";
        String newUserPw = "5678";
        UserResetPwDTO resetPwDTO = new UserResetPwDTO(member.getUserId(), certificationFixture, newUserPw);

        assertThrows(CustomBadCredentialsException.class, () -> userWriteUseCase.resetPw(resetPwDTO));

        Member patchMember = memberRepository.findByLocalUserId(member.getUserId());

        assertEquals(member.getUserPw(), patchMember.getUserPw());

        String redisCertificationValue = redisTemplate.opsForValue().get(member.getUserId());

        assertNull(redisCertificationValue);
    }

    @Test
    @DisplayName(value = "비밀번호 변경. 사용자 정보가 존재하지 않는 경우")
    void resetPwUserNotFound() {
        String noneUserId = "noneUser";
        String certificationFixture = "102030";
        String newUserPw = "5678";
        UserResetPwDTO resetPwDTO = new UserResetPwDTO(noneUserId, certificationFixture, newUserPw);
        redisTemplate.opsForValue().set(noneUserId, certificationFixture);

        assertThrows(
                IllegalArgumentException.class,
                () -> userWriteUseCase.resetPw(resetPwDTO)
        );

        String redisCertificationValue = redisTemplate.opsForValue().get(noneUserId);

        assertNull(redisCertificationValue);
    }
}
