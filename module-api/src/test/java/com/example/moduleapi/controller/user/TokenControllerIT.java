package com.example.moduleapi.controller.user;

import com.example.moduleapi.ModuleApiApplication;
import com.example.moduleapi.config.exception.ExceptionEntity;
import com.example.moduleapi.fixture.TokenFixture;
import com.example.modulecommon.fixture.MemberAndAuthFixture;
import com.example.modulecommon.model.dto.MemberAndAuthFixtureDTO;
import com.example.modulecommon.model.dto.response.ResponseMessageDTO;
import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.enumuration.ErrorCode;
import com.example.modulecommon.model.enumuration.Result;
import com.example.moduleuser.repository.AuthRepository;
import com.example.moduleuser.repository.MemberRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ModuleApiApplication.class)
@EntityScan(basePackages = "com.example.modulecommon")
@EnableJpaRepositories(basePackages = "com.example")
@ComponentScan(basePackages = {"com.example.moduleconfig", "com.example.modulecommon"})
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class TokenControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private EntityManager em;

    @Autowired
    private TokenFixture tokenFixture;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private AuthRepository authRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Value("#{jwt['token.access.header']}")
    private String accessHeader;

    @Value("#{jwt['token.refresh.header']}")
    private String refreshHeader;

    @Value("#{jwt['cookie.ino.header']}")
    private String inoHeader;

    private Map<String, String> tokenMap;

    private String accessTokenValue;

    private String refreshTokenValue;

    private String inoValue;

    private static final String URL_PREFIX = "/api/";

    @BeforeEach
    void init() {
        MemberAndAuthFixtureDTO memberAndAuthFixtureDTO = MemberAndAuthFixture.createDefaultMember(1);
        memberRepository.saveAll(memberAndAuthFixtureDTO.memberList());
        authRepository.saveAll(memberAndAuthFixtureDTO.authList());
        Member member = memberAndAuthFixtureDTO.memberList().get(0);

        tokenMap = tokenFixture.createAndSaveAllToken(member);
        accessTokenValue = tokenMap.get(accessHeader);
        refreshTokenValue = tokenMap.get(refreshHeader);
        inoValue = tokenMap.get(inoHeader);

        em.flush();
        em.clear();
    }

    @AfterEach
    void cleanUP() {
        String accessKey = tokenMap.get("accessKey");
        String refreshKey = tokenMap.get("refreshKey");

        redisTemplate.delete(accessKey);
        redisTemplate.delete(refreshKey);
    }

    @Test
    @DisplayName(value = "토큰 재발급 요청")
    void reIssue() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "reissue")
                        .header(accessHeader, accessTokenValue)
                        .cookie(new Cookie(refreshHeader, refreshTokenValue))
                        .cookie(new Cookie(inoHeader, inoValue)))
                .andExpect(status().isOk())
                .andReturn();

        String accessToken = tokenFixture.getResponseAuthorization(result);
        Map<String, String> cookieMap = tokenFixture.getCookieMap(result);
        String refreshToken = cookieMap.get(refreshHeader).substring(6);
        String ino = cookieMap.get(inoHeader);

        assertNotNull(accessToken);
        assertNotNull(refreshToken);
        assertNull(ino);

        String accessKey = tokenMap.get("accessKey");
        String refreshKey = tokenMap.get("refreshKey");

        String redisAccessValue = redisTemplate.opsForValue().get(accessKey);
        String redisRefreshValue = redisTemplate.opsForValue().get(refreshKey);

        assertNotNull(redisAccessValue);
        assertNotNull(redisRefreshValue);

        assertNotEquals(accessTokenValue, redisAccessValue);
        assertNotEquals(refreshTokenValue, redisRefreshValue);
    }

    @Test
    @DisplayName(value = "토큰 재발급 요청. ino가 없는 경우 탈취로 판단")
    void reIssueNotExistsIno() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "reissue")
                        .header(accessHeader, accessTokenValue)
                        .cookie(new Cookie(refreshHeader, refreshTokenValue)))
                .andExpect(status().isUnauthorized())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.TOKEN_STEALING.getMessage(), response.errorMessage());

        List<String> cookies = result.getResponse().getHeaders("Set-Cookie");

        boolean refreshCookie = cookies.stream()
                .anyMatch(v ->
                        v.startsWith(refreshHeader + "=")
                                && v.contains("Max-Age=0")
                );

        boolean inoCookie = cookies.stream()
                .anyMatch(v ->
                        v.startsWith(inoHeader + "=")
                                && v.contains("Max-Age=0")
                );

        assertTrue(refreshCookie);
        assertTrue(inoCookie);
    }

    @Test
    @DisplayName(value = "토큰 재발급 요청. AccessToken이 잘못된 토큰인 경우. Redis 저장 토큰까지 제거.")
    void reIssueWrongAccessToken() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "reissue")
                        .header(accessHeader, "WrongAccessTokenValue")
                        .cookie(new Cookie(refreshHeader, refreshTokenValue))
                        .cookie(new Cookie(inoHeader, inoValue)))
                .andExpect(status().isUnauthorized())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.TOKEN_STEALING.getMessage(), response.errorMessage());

        List<String> cookies = result.getResponse().getHeaders("Set-Cookie");

        boolean refreshCookie = cookies.stream()
                .anyMatch(v ->
                        v.startsWith(refreshHeader + "=")
                                && v.contains("Max-Age=0")
                );

        boolean inoCookie = cookies.stream()
                .anyMatch(v ->
                        v.startsWith(inoHeader + "=")
                                && v.contains("Max-Age=0")
                );

        assertTrue(refreshCookie);
        assertTrue(inoCookie);

        String accessKey = tokenMap.get("accessKey");
        String refreshKey = tokenMap.get("refreshKey");

        String redisAccessValue = redisTemplate.opsForValue().get(accessKey);
        String redisRefreshValue = redisTemplate.opsForValue().get(refreshKey);

        assertNull(redisAccessValue);
        assertNull(redisRefreshValue);
    }

    @Test
    @DisplayName(value = "토큰 재발급 요청. AccessToken, RefreshToken이 잘못된 토큰인 경우. Redis는 유지, cookie는 제거.")
    void reIssueWrongAccessTokenAndRefreshToken() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "reissue")
                        .header(accessHeader, "WrongAccessTokenValue")
                        .cookie(new Cookie(refreshHeader, "WrongRefreshTokenValue"))
                        .cookie(new Cookie(inoHeader, inoValue)))
                .andExpect(status().isUnauthorized())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.TOKEN_STEALING.getMessage(), response.errorMessage());

        List<String> cookies = result.getResponse().getHeaders("Set-Cookie");

        boolean refreshCookie = cookies.stream()
                .anyMatch(v ->
                        v.startsWith(refreshHeader + "=")
                                && v.contains("Max-Age=0")
                );

        boolean inoCookie = cookies.stream()
                .anyMatch(v ->
                        v.startsWith(inoHeader + "=")
                                && v.contains("Max-Age=0")
                );

        assertTrue(refreshCookie);
        assertTrue(inoCookie);

        String accessKey = tokenMap.get("accessKey");
        String refreshKey = tokenMap.get("refreshKey");

        String redisAccessValue = redisTemplate.opsForValue().get(accessKey);
        String redisRefreshValue = redisTemplate.opsForValue().get(refreshKey);

        assertNotNull(redisAccessValue);
        assertNotNull(redisRefreshValue);
    }

    @Test
    @DisplayName(value = "토큰 재발급 요청. RefreshToken이 잘못된 토큰인 경우. Redis 데이터까지 제거.")
    void reIssueWrongRefreshToken() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "reissue")
                        .header(accessHeader, accessTokenValue)
                        .cookie(new Cookie(refreshHeader, "WrongRefreshTokenValue"))
                        .cookie(new Cookie(inoHeader, inoValue)))
                .andExpect(status().isUnauthorized())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.TOKEN_STEALING.getMessage(), response.errorMessage());

        List<String> cookies = result.getResponse().getHeaders("Set-Cookie");

        boolean refreshCookie = cookies.stream()
                .anyMatch(v ->
                        v.startsWith(refreshHeader + "=")
                                && v.contains("Max-Age=0")
                );

        boolean inoCookie = cookies.stream()
                .anyMatch(v ->
                        v.startsWith(inoHeader + "=")
                                && v.contains("Max-Age=0")
                );

        assertTrue(refreshCookie);
        assertTrue(inoCookie);

        String accessKey = tokenMap.get("accessKey");
        String refreshKey = tokenMap.get("refreshKey");

        String redisAccessValue = redisTemplate.opsForValue().get(accessKey);
        String redisRefreshValue = redisTemplate.opsForValue().get(refreshKey);

        assertNull(redisAccessValue);
        assertNull(redisRefreshValue);
    }
}
