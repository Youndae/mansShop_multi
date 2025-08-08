package com.example.moduletest.user.service;

import com.example.moduleauthapi.service.JWTTokenProvider;
import com.example.modulecommon.customException.CustomAccessDeniedException;
import com.example.modulecommon.fixture.MemberAndAuthFixture;
import com.example.modulecommon.model.dto.MemberAndAuthFixtureDTO;
import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.enumuration.Result;
import com.example.moduletest.ModuleTestApplication;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = ModuleTestApplication.class)
@EntityScan(basePackages = "com.example.modulecommon")
@EnableJpaRepositories(basePackages = "com.example")
@ComponentScan(basePackages = {"com.example.moduleconfig", "com.example.modulecommon"})
@ActiveProfiles("test")
@Transactional
public class UseDomainServiceIT {

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
    @DisplayName(value = "userId를 통해 토큰 생성 후 반환")
    void getLoginUserStatusResponse() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        String result = assertDoesNotThrow(() -> userDomainService.getLoginUserStatusResponse(member.getUserId(), request, response));

        assertNotNull(result);
        assertEquals(Result.OK.getResultKey(), result);

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

        redisTemplate.delete(member.getUserId());
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
