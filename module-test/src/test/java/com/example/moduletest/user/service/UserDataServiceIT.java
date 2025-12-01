package com.example.moduletest.user.service;

import com.example.modulecommon.fixture.MemberAndAuthFixture;
import com.example.modulecommon.model.dto.MemberAndAuthFixtureDTO;
import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.enumuration.Result;
import com.example.moduletest.ModuleTestApplication;
import com.example.moduleuser.model.dto.member.in.LogoutDTO;
import com.example.moduleuser.model.dto.member.in.UserSearchPwDTO;
import com.example.moduleuser.repository.AuthRepository;
import com.example.moduleuser.repository.MemberRepository;
import com.example.moduleuser.service.UserDataService;
import jakarta.persistence.EntityManager;
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
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = ModuleTestApplication.class)
@EntityScan(basePackages = "com.example.modulecommon")
@EnableJpaRepositories(basePackages = "com.example")
@ComponentScan(basePackages = {"com.example.moduleconfig", "com.example.modulecommon"})
@ActiveProfiles("test")
@Transactional
public class UserDataServiceIT {

    @Autowired
    private UserDataService userDataService;

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
    @DisplayName(value = "임시토큰을 발행받은 OAuth2 사용자 정식 토큰 발행")
    void deleteTemporaryTokenAndCookie() {
        MockHttpServletResponse temporaryResponse = new MockHttpServletResponse();
        redisTemplate.opsForValue().set(member.getUserId(), "testTemporaryTokenValue");

        assertDoesNotThrow(() -> userDataService.deleteTemporaryTokenAndCookie(member.getUserId(), temporaryResponse));

        List<String> cookies = temporaryResponse.getHeaders("Set-Cookie");

        boolean temporaryCookie = cookies.stream()
                .anyMatch(v ->
                        v.startsWith(temporaryHeader + "=") && v.contains("Max-Age=0")
                );

        assertTrue(temporaryCookie);

        String valueOfRedis = redisTemplate.opsForValue().get(member.getUserId());
        assertNull(valueOfRedis);
    }

    @Test
    @DisplayName(value = "로그아웃 요청. 토큰과 쿠키 제거")
    void deleteTokenAndCookieByLogout() {
        String inoValue = UUID.randomUUID().toString().replace("-", "");
        String accessToken = "testAccessTokenValue";
        String refreshToken = "testRefreshTokenValue";
        String accessKey = "at" + inoValue + member.getUserId();
        String refreshKey = "rt" + inoValue + member.getUserId();

        redisTemplate.opsForValue().set(accessKey, accessToken);
        redisTemplate.opsForValue().set(refreshKey, refreshToken);

        LogoutDTO dto = new LogoutDTO(accessToken, inoValue, member.getUserId());
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertDoesNotThrow(() -> userDataService.deleteTokenAndCookieByLogout(dto, response));

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

        String accessRedisValue = redisTemplate.opsForValue().get(accessKey);
        String refreshRedisValue = redisTemplate.opsForValue().get(refreshKey);

        assertNull(accessRedisValue);
        assertNull(refreshRedisValue);
    }

    @Test
    @DisplayName(value = "Redis에 인증번호 저장")
    void saveCertificationNumber() {
        UserSearchPwDTO searchDTO = new UserSearchPwDTO(member.getUserId(), member.getUserName(), member.getUserEmail());
        int certificationNumber = 123456;

        assertDoesNotThrow(() -> userDataService.saveCertificationNumberToRedis(searchDTO, certificationNumber));

        String saveCertification = redisTemplate.opsForValue().get(member.getUserId());
        assertNotNull(saveCertification);
        assertEquals(String.valueOf(certificationNumber), saveCertification);

        redisTemplate.delete(member.getUserId());
    }

    @Test
    @DisplayName(value = "Redis에서 인증번호 조회")
    void getCertificationNumberFromRedis() throws Exception {
        String certificationNumber = "456123";
        redisTemplate.opsForValue().set(member.getUserId(), certificationNumber);

        String result = userDataService.getCertificationNumberFromRedis(member.getUserId());

        assertNotNull(result);
        assertEquals(certificationNumber, result);

        redisTemplate.delete(member.getUserId());
    }

    @Test
    @DisplayName(value = "Redis에서 인증번호 제거")
    void deleteCertificationFromRedis() throws Exception {
        String certificationNumber = "456123";
        redisTemplate.opsForValue().set(member.getUserId(), certificationNumber);

        userDataService.deleteCertificationNumberFromRedis(member.getUserId());

        String deleteCertification = redisTemplate.opsForValue().get(member.getUserId());
        assertNull(deleteCertification);
    }
}
