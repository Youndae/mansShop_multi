package com.example.moduleuser.service.integration;


import com.example.moduleauth.config.jwt.JWTTokenProvider;
import com.example.moduleauth.config.user.CustomUser;
import com.example.modulecommon.customException.CustomAccessDeniedException;
import com.example.modulecommon.fixture.MemberAndAuthFixture;
import com.example.modulecommon.model.dto.MemberAndAuthFixtureDTO;
import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.enumuration.Role;
import com.example.moduleuser.ModuleUserApplication;
import com.example.moduleuser.model.dto.member.in.LoginDTO;
import com.example.moduleuser.model.dto.member.out.UserStatusResponseDTO;
import com.example.moduleuser.repository.AuthRepository;
import com.example.moduleuser.repository.MemberRepository;
import com.example.moduleuser.service.UserDomainService;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = ModuleUserApplication.class)
@EntityScan(basePackages = "com.example.modulecommon")
@EnableJpaRepositories(basePackages = "com.example")
@ComponentScan(basePackages = {"com.example.moduleconfig", "com.example.modulecommon"})
@ActiveProfiles("test")
@Transactional
public class UserDomainServiceIT {

    @Autowired
    private UserDomainService userDomainService;

    @Autowired
    private JWTTokenProvider tokenProvider;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private AuthRepository authRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private Member member;

    @Value("#{jwt['token.temporary.header']}")
    private String temporaryHeader;

    @BeforeEach
    void init() {
        MemberAndAuthFixtureDTO memberAndAuthFixture = MemberAndAuthFixture.createDefaultMember(1);
        member = memberAndAuthFixture.memberList().get(0);
        memberRepository.save(member);
        authRepository.saveAll(memberAndAuthFixture.authList());

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName(value = "로그인 요청 시 인증 이후 CustomUser 반환")
    void loginAuthenticated() {
        LoginDTO loginDTO = new LoginDTO(member.getUserId(), "1234");

        CustomUser result = assertDoesNotThrow(() -> userDomainService.loginAuthenticated(loginDTO));

        assertNotNull(result);
        assertEquals(member.getUserId(), result.getUsername());
    }

    @Test
    @DisplayName(value = "로그인 요청 시 인증 이후 CustomUser 반환. 정보가 일치하지 않는 경우")
    void loginAuthenticatedBadCredentials() {
        LoginDTO loginDTO = new LoginDTO("noneUser", "1234");

        assertThrows(
                InternalAuthenticationServiceException.class,
                () -> userDomainService.loginAuthenticated(loginDTO)
        );
    }

    @Test
    @DisplayName(value = "CustomUser를 통해 토큰 생성 후 반환")
    void getLoginUserStatusResponse() {
        CustomUser authenticateUser = new CustomUser(member);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        UserStatusResponseDTO result = assertDoesNotThrow(() -> userDomainService.getLoginUserStatusResponse(authenticateUser, request, response));

        assertNotNull(result);
        assertEquals(member.getUserId(), result.getUserId());
        assertEquals(Role.MEMBER.getRole(), result.getRole());

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
    @DisplayName(value = "oAuth2 사용자의 임시토큰 검증")
    void validateTemporaryToken() {
        MockHttpServletResponse temporaryResponse = new MockHttpServletResponse();

        tokenProvider.createTemporaryToken(member.getUserId(), temporaryResponse);
        String temporaryToken = temporaryResponse.getHeader("Set-Cookie").split(";", 2)[0].split("=", 2)[1];

        Cookie temporaryCookie = new Cookie(temporaryHeader, temporaryToken);

        String result = assertDoesNotThrow(() -> userDomainService.validateTemporaryClaimByUserId(temporaryCookie));

        assertNotNull(result);
        assertEquals(member.getUserId(), result);
    }

    @Test
    @DisplayName(value = "oAuth2 사용자의 임시토큰 검증. 잘못된 토큰인 경우")
    void validateTemporaryTokenIsWrong() {
        Cookie temporaryCookie = new Cookie(temporaryHeader, "WrongTokenValue");

        assertThrows(
                CustomAccessDeniedException.class,
                () -> userDomainService.validateTemporaryClaimByUserId(temporaryCookie)
        );
    }

    @Test
    @DisplayName(value = "oAuth 사용자의 토큰 발급")
    void issueOAuthUserToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = assertDoesNotThrow(() -> userDomainService.issueOAuthUserToken(member.getUserId(), request, response));

        assertTrue(result);

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
}
