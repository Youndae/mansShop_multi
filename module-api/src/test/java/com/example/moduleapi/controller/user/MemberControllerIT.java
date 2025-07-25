package com.example.moduleapi.controller.user;

import com.example.moduleapi.ModuleApiApplication;
import com.example.moduleapi.config.exception.ExceptionEntity;
import com.example.moduleapi.fixture.TokenFixture;
import com.example.moduleapi.utils.MailHogUtils;
import com.example.modulecommon.fixture.MemberAndAuthFixture;
import com.example.modulecommon.model.dto.MemberAndAuthFixtureDTO;
import com.example.modulecommon.model.dto.response.ResponseMessageDTO;
import com.example.modulecommon.model.entity.Auth;
import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.enumuration.ErrorCode;
import com.example.modulecommon.model.enumuration.Result;
import com.example.modulecommon.model.enumuration.Role;
import com.example.moduleuser.model.dto.member.in.JoinDTO;
import com.example.moduleuser.model.dto.member.in.LoginDTO;
import com.example.moduleuser.model.dto.member.in.UserCertificationDTO;
import com.example.moduleuser.model.dto.member.in.UserResetPwDTO;
import com.example.moduleuser.model.dto.member.out.UserSearchIdResponseDTO;
import com.example.moduleuser.model.dto.member.out.UserStatusResponseDTO;
import com.example.moduleuser.repository.AuthRepository;
import com.example.moduleuser.repository.MemberRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.Cookie;
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
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest(classes = ModuleApiApplication.class)
@EntityScan(basePackages = "com.example.modulecommon")
@EnableJpaRepositories(basePackages = "com.example")
@ComponentScan(basePackages = {"com.example.moduleconfig", "com.example.modulecommon"})
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class MemberControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private TokenFixture tokenFixture;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private AuthRepository authRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private EntityManager em;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private MailHogUtils mailHogUtils;

    @Value("#{jwt['token.access.header']}")
    private String accessHeader;

    @Value("#{jwt['token.refresh.header']}")
    private String refreshHeader;

    @Value("#{jwt['cookie.ino.header']}")
    private String inoHeader;

    @Value("#{jwt['token.all.prefix']}")
    private String allTokenPrefix;

    @Value("#{jwt['token.temporary.header']}")
    private String temporaryHeader;

    private Map<String, String> tokenMap;

    private String accessTokenValue;

    private String refreshTokenValue;

    private String inoValue;

    private Member member;

    private Member oAuthMember;

    private static final String CERTIFICATION_FIXTURE = "113355";

    private static final String URL_PREFIX = "/api/member/";

    @BeforeEach
    void init() {
        MemberAndAuthFixtureDTO memberAndAuthFixture = MemberAndAuthFixture.createDefaultMember(1);
        MemberAndAuthFixtureDTO oAuthFixture = MemberAndAuthFixture.createDefaultMember(1);

        List<Member> saveMemberList = new ArrayList<>(memberAndAuthFixture.memberList());
        saveMemberList.addAll(oAuthFixture.memberList());
        List<Auth> saveAuthList = new ArrayList<>(memberAndAuthFixture.authList());
        saveAuthList.addAll(oAuthFixture.authList());

        memberRepository.saveAll(saveMemberList);
        authRepository.saveAll(saveAuthList);

        member = memberAndAuthFixture.memberList().get(0);
        oAuthMember = oAuthFixture.memberList().get(0);

        em.flush();
        em.clear();
    }

    private void setRedisByCertification() {
        redisTemplate.opsForValue().set(member.getUserId(), CERTIFICATION_FIXTURE);
    }

    private void setJWT() {
        tokenMap = tokenFixture.createAndSaveAllToken(member);
        accessTokenValue = tokenMap.get(accessHeader);
        refreshTokenValue = tokenMap.get(refreshHeader);
        inoValue = tokenMap.get(inoHeader);
    }

    private void cleanUp() {
        String accessKey = tokenMap.get("accessKey");
        String refreshKey = tokenMap.get("refreshKey");

        redisTemplate.delete(accessKey);
        redisTemplate.delete(refreshKey);
        redisTemplate.delete(member.getUserId());
    }

    @Test
    @DisplayName(value = "로그인 요청")
    void loginProc() throws Exception {
        LoginDTO loginDTO = new LoginDTO(member.getUserId(), "1234");
        String loginRequestBody = om.writeValueAsString(loginDTO);

        MvcResult result = mockMvc.perform(post(URL_PREFIX + "login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestBody))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();

        UserStatusResponseDTO response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(member.getUserId(), response.getUserId());
        assertEquals(Role.MEMBER.getRole(), response.getRole());

        String accessToken = tokenFixture.getResponseAuthorization(result);
        Map<String, String> cookieMap = tokenFixture.getCookieMap(result);

        String refreshToken = cookieMap.get(refreshHeader).substring(6);
        String ino = cookieMap.get(inoHeader);

        assertNotNull(accessToken);
        assertNotNull(refreshToken);
        assertNotNull(ino);

        Map<String, String> keyMap = tokenFixture.getRedisKeyMap(member, ino);

        String accessKey = keyMap.get("accessKey");
        String refreshKey = keyMap.get("refreshKey");

        String redisAccessValue = redisTemplate.opsForValue().get(accessKey);
        String redisRefreshValue = redisTemplate.opsForValue().get(refreshKey);

        assertNotNull(redisAccessValue);
        assertNotNull(redisRefreshValue);

        assertEquals(accessToken.replace(allTokenPrefix, ""), redisAccessValue);
        assertEquals(refreshToken.replace(allTokenPrefix, ""), redisRefreshValue);

        redisTemplate.delete(accessKey);
        redisTemplate.delete(refreshKey);
    }

    @Test
    @DisplayName(value = "로그인 요청. 아이디 또는 비밀번호가 일치하지 않는 경우")
    void loginProcFail() throws Exception {
        LoginDTO loginDTO = new LoginDTO("noneMember", "1234");
        String loginRequestBody = om.writeValueAsString(loginDTO);

        mockMvc.perform(post(URL_PREFIX + "login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestBody))
                .andExpect(status().is4xxClientError())
                .andReturn();
    }

    @Test
    @DisplayName(value = "로그아웃 요청")
    void logoutProc() throws Exception {
        setJWT();
        MvcResult result = mockMvc.perform(post(URL_PREFIX + "logout")
                        .header(accessHeader, accessTokenValue)
                        .cookie(new Cookie(refreshHeader, refreshTokenValue))
                        .cookie(new Cookie(inoHeader, inoValue)))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();

        ResponseMessageDTO response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(Result.OK.getResultKey(), response.message());

        String redisAccessKey = tokenMap.get("accessKey");
        String redisRefreshKey = tokenMap.get("refreshKey");

        String redisAccessToken = redisTemplate.opsForValue().get(redisAccessKey);
        String redisRefreshToken = redisTemplate.opsForValue().get(redisRefreshKey);

        assertNull(redisAccessToken);
        assertNull(redisRefreshToken);

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
    @DisplayName(value = "회원가입 요청")
    void joinProc() throws Exception {
        JoinDTO joinDTO = new JoinDTO(
                "joinTestUserId",
                "join1234!@",
                "joinUserName",
                "joinUserNickname",
                "01001012020",
                "2000/01/01",
                "joinUser@join.com"
        );
        LocalDate birth = LocalDate.of(2000, 1, 1);

        String joinRequestBody = om.writeValueAsString(joinDTO);

        MvcResult result = mockMvc.perform(post(URL_PREFIX + "join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(joinRequestBody))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();

        ResponseMessageDTO response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        em.flush();
        em.clear();

        assertNotNull(response);
        assertEquals(Result.OK.getResultKey(), response.message());

        Member saveMember = memberRepository.findByUserId(joinDTO.userId());

        assertNotNull(saveMember);
        assertTrue(passwordEncoder.matches(joinDTO.userPw(), saveMember.getUserPw()));
        assertEquals(joinDTO.userName(), saveMember.getUserName());
        assertEquals(joinDTO.nickname(), saveMember.getNickname());
        assertEquals(joinDTO.phone(), saveMember.getPhone().replaceAll("-", ""));
        assertEquals(birth, saveMember.getBirth());
        assertEquals(joinDTO.userEmail(), saveMember.getUserEmail());
        assertEquals(1, saveMember.getAuths().size());
        assertEquals(Role.MEMBER.getKey(), saveMember.getAuths().get(0).getAuth());
    }

    @Test
    @DisplayName(value = "oAuth 사용자의 정식 토큰 발급 요청")
    void oAuthIssueToken() throws Exception {
        String temporaryToken = tokenFixture.createAndRedisSaveTemporaryToken(oAuthMember);

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "oAuth/token")
                        .cookie(new Cookie(temporaryHeader, temporaryToken)))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ResponseMessageDTO response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(Result.OK.getResultKey(), response.message());

        String accessToken = tokenFixture.getResponseAuthorization(result);
        Map<String, String> tokenMap = tokenFixture.getCookieMap(result);

        String refreshToken = tokenMap.get(refreshHeader).substring(6);
        String ino = tokenMap.get(inoHeader);

        assertNotNull(accessToken);
        assertNotNull(refreshToken);
        assertNotNull(ino);

        Map<String, String> keyMap = tokenFixture.getRedisKeyMap(oAuthMember, ino);

        String accessKey = keyMap.get("accessKey");
        String refreshKey = keyMap.get("refreshKey");

        String redisAccessValue = redisTemplate.opsForValue().get(accessKey);
        String redisRefreshValue = redisTemplate.opsForValue().get(refreshKey);

        assertNotNull(redisAccessValue);
        assertNotNull(redisRefreshValue);

        assertEquals(accessToken.replace(allTokenPrefix, ""), redisAccessValue);
        assertEquals(refreshToken.replace(allTokenPrefix, ""), redisRefreshValue);

        redisTemplate.delete(accessKey);
        redisTemplate.delete(refreshKey);
    }

    @Test
    @DisplayName(value = "oAuth 사용자의 정식 토큰 발급 요청. 임시 토큰이 없는 경우")
    void oAuthIssueTokenNotExistTemporaryToken() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "oAuth/token"))
                .andExpect(status().is(403))
                .andReturn();

        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.BAD_CREDENTIALS.getMessage(), response.errorMessage());
    }

    @Test
    @DisplayName(value = "oAuth 사용자의 정식 토큰 발급 요청. 잘못된 임시 토큰인 경우")
    void oAuthIssueTokenWrongTemporaryToken() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "oAuth/token")
                        .cookie(new Cookie(temporaryHeader, "wrongTokenValue")))
                .andExpect(status().is(403))
                .andReturn();
        String content = result.getResponse().getContentAsString();

        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.ACCESS_DENIED.getMessage(), response.errorMessage());
    }

    @Test
    @DisplayName(value = "oAuth 사용자의 정식 토큰 발급 요청. 임시 토큰이 만료된 경우")
    void oAuthIssueTokenExpirationTemporaryToken() throws Exception {
        String temporaryToken = tokenFixture.createAndRedisSaveExpirationTemporaryToken(oAuthMember);

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "oAuth/token")
                        .cookie(new Cookie(temporaryHeader, temporaryToken)))
                .andExpect(status().is(403))
                .andReturn();
        String content = result.getResponse().getContentAsString();

        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.ACCESS_DENIED.getMessage(), response.errorMessage());

        redisTemplate.delete(oAuthMember.getUserId());
    }

    @Test
    @DisplayName(value = "oAuth 사용자의 정식 토큰 발급 요청. 임시 토큰이 탈취된 것으로 판단 된 경우")
    void oAuthIssueTokenStealingTemporaryToken() throws Exception {
        tokenFixture.createAndRedisSaveTemporaryToken(oAuthMember);
        Thread.sleep(3000);

        String notSaveTemporaryToken = tokenFixture.createTemporaryToken(oAuthMember);

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "oAuth/token")
                        .cookie(new Cookie(temporaryHeader, notSaveTemporaryToken)))
                .andExpect(status().is(800))
                .andReturn();

        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.TOKEN_STEALING.getMessage(), response.errorMessage());

        redisTemplate.delete(oAuthMember.getUserId());
    }

    @Test
    @DisplayName(value = "회원 가입 시 아이디 중복 체크")
    void checkJoinId() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "check-id")
                        .param("userId", "newUserId"))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        ResponseMessageDTO response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(Result.NO_DUPLICATE.getResultKey(), response.message());
    }

    @Test
    @DisplayName(value = "회원 가입 시 아이디 중복 체크. 중복인 경우")
    void checkJoinIdExists() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "check-id")
                        .param("userId", member.getUserId()))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        ResponseMessageDTO response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(Result.DUPLICATE.getResultKey(), response.message());
    }

    @Test
    @DisplayName(value = "닉네임 중복 체크")
    void checkNickname() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "check-nickname")
                        .param("nickname", "newNickname"))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        ResponseMessageDTO response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(Result.NO_DUPLICATE.getResultKey(), response.message());
    }

    @Test
    @DisplayName(value = "닉네임 중복 체크. 중복인 경우")
    void checkNicknameExists() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "check-nickname")
                        .param("nickname", member.getNickname()))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        ResponseMessageDTO response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(Result.DUPLICATE.getResultKey(), response.message());
    }

    @Test
    @DisplayName(value = "닉네임 중복 체크. 중복이지만 자신의 닉네임과 동일한 경우 ( 회원 정보 수정 )")
    void checkNicknameExistsThenOriginNickname() throws Exception {
        setJWT();
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "check-nickname")
                        .param("nickname", member.getNickname())
                        .header(accessHeader, accessTokenValue)
                        .cookie(new Cookie(refreshHeader, refreshTokenValue))
                        .cookie(new Cookie(inoHeader, inoValue)))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        ResponseMessageDTO response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(Result.NO_DUPLICATE.getResultKey(), response.message());
        cleanUp();
    }

    @Test
    @DisplayName(value = "로그인 상태 체크")
    void checkLoginStatus() throws Exception {
        setJWT();
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "status")
                        .header(accessHeader, accessTokenValue)
                        .cookie(new Cookie(refreshHeader, refreshTokenValue))
                        .cookie(new Cookie(inoHeader, inoValue)))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        UserStatusResponseDTO response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals(member.getUserId(), response.getUserId());
        assertEquals(Role.MEMBER.getRole(), response.getRole());

        cleanUp();
    }

    @Test
    @DisplayName(value = "로그인 상태 체크. 로그인 상태가 아닌 경우")
    void checkLoginStatusNotLogin() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "status"))
                .andExpect(status().is(403))
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals("AccessDeniedException", response.errorMessage());
    }

    @Test
    @DisplayName(value = "아이디 찾기. 연락처 기반")
    void searchIdByPhone() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "search-id")
                        .param("userName", member.getUserName())
                        .param("userPhone", member.getPhone().replaceAll("-", "")))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        UserSearchIdResponseDTO response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals(member.getUserId(), response.userId());
        assertEquals(Result.OK.getResultKey(), response.message());
    }

    @Test
    @DisplayName(value = "아이디 찾기. 이메일 기반")
    void searchIdByEmail() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "search-id")
                        .param("userName", member.getUserName())
                        .param("userEmail", member.getUserEmail()))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        UserSearchIdResponseDTO response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals(member.getUserId(), response.userId());
        assertEquals(Result.OK.getResultKey(), response.message());
    }

    @Test
    @DisplayName(value = "아이디 찾기. 정보가 없는 경우")
    void searchIdNotFound() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "search-id")
                        .param("userName", "noneUserName")
                        .param("userEmail", "none@none.com"))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        UserSearchIdResponseDTO response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertNull(response.userId());
        assertEquals(Result.NOTFOUND.getResultKey(), response.message());
    }

    @Test
    @DisplayName(value = "비밀번호 찾기")
    void searchPw() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "search-pw")
                        .param("id", member.getUserId())
                        .param("name", member.getUserName())
                        .param("email", member.getUserEmail()))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ResponseMessageDTO response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals(Result.OK.getResultKey(), response.message());

        String redisCertificationValue = redisTemplate.opsForValue().get(member.getUserId());
        assertNotNull(redisCertificationValue);

        String mailCertification = mailHogUtils.getCertificationNumberByMailHog();

        assertEquals(redisCertificationValue, mailCertification);

        mailHogUtils.deleteMailHog();

        redisTemplate.delete(member.getUserId());
    }

    @Test
    @DisplayName(value = "비밀번호 찾기. 정보가 없는 경우")
    void searchPwNotFound() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "search-pw")
                        .param("id", "noneUserId")
                        .param("name", "noneUsername")
                        .param("email", "none@none.com"))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ResponseMessageDTO response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals(Result.NOTFOUND.getResultKey(), response.message());
    }

    @Test
    @DisplayName(value = "비밀번호 찾기 인증번호 확인")
    void checkCertification() throws Exception {
        setRedisByCertification();
        UserCertificationDTO certificationDTO = new UserCertificationDTO(member.getUserId(), CERTIFICATION_FIXTURE);
        String requestDTO = om.writeValueAsString(certificationDTO);

        MvcResult result = mockMvc.perform(post(URL_PREFIX + "certification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDTO))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ResponseMessageDTO response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals(Result.OK.getResultKey(), response.message());

        redisTemplate.delete(member.getUserId());
    }

    @Test
    @DisplayName(value = "비밀번호 찾기 인증번호 확인. 인증번호가 일치하지 않는 경우")
    void checkCertificationNotEquals() throws Exception {
        setRedisByCertification();
        UserCertificationDTO certificationDTO = new UserCertificationDTO(member.getUserId(), "000000");
        String requestDTO = om.writeValueAsString(certificationDTO);

        MvcResult result = mockMvc.perform(post(URL_PREFIX + "certification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDTO))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ResponseMessageDTO response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals(Result.FAIL.getResultKey(), response.message());

        redisTemplate.delete(member.getUserId());
    }

    @Test
    @DisplayName(value = "비밀번호 찾기 인증번호 확인 이후 비밀번호 수정")
    void resetPassword() throws Exception {
        setRedisByCertification();
        UserResetPwDTO resetDTO = new UserResetPwDTO(member.getUserId(), CERTIFICATION_FIXTURE, "5678!@");
        String requestDTO = om.writeValueAsString(resetDTO);

        MvcResult result = mockMvc.perform(patch(URL_PREFIX + "reset-pw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDTO))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ResponseMessageDTO response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals(Result.OK.getResultKey(), response.message());

        String redisCertification = redisTemplate.opsForValue().get(member.getUserId());
        assertNull(redisCertification);

        Member patchMember = memberRepository.findByUserId(member.getUserId());
        assertTrue(passwordEncoder.matches(resetDTO.userPw(), patchMember.getUserPw()));
    }

    @Test
    @DisplayName(value = "비밀번호 찾기 인증번호 확인 이후 비밀번호 수정. Redis에 인증번호 데이터가 없는 경우")
    void resetPasswordNotExistCertificationNumber() throws Exception {
        UserResetPwDTO resetDTO = new UserResetPwDTO(member.getUserId(), "000000", "5678!@");
        String requestDTO = om.writeValueAsString(resetDTO);

        MvcResult result = mockMvc.perform(patch(URL_PREFIX + "reset-pw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDTO))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();

        ResponseMessageDTO response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(Result.FAIL.getResultKey(), response.message());
    }

    @Test
    @DisplayName(value = "비밀번호 찾기 인증번호 확인 이후 비밀번호 수정. 인증번호가 일치하지 않는 경우")
    void resetPasswordNotEqualsCertificationNumber() throws Exception {
        setRedisByCertification();
        UserResetPwDTO resetDTO = new UserResetPwDTO(member.getUserId(), "000000", "5678!@");
        String requestDTO = om.writeValueAsString(resetDTO);

        MvcResult result = mockMvc.perform(patch(URL_PREFIX + "reset-pw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDTO))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();

        ResponseMessageDTO response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(Result.FAIL.getResultKey(), response.message());
    }
}
