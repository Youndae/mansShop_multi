package com.example.moduleapi.controller.admin;


import com.example.moduleapi.ModuleApiApplication;
import com.example.moduleapi.config.exception.ExceptionEntity;
import com.example.moduleapi.fixture.TokenFixture;
import com.example.moduleapi.model.response.PagingResponseDTO;
import com.example.modulecommon.fixture.MemberAndAuthFixture;
import com.example.modulecommon.model.dto.MemberAndAuthFixtureDTO;
import com.example.modulecommon.model.entity.Auth;
import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.enumuration.ErrorCode;
import com.example.modulecommon.utils.PaginationUtils;
import com.example.moduleuser.model.dto.admin.in.AdminPostPointDTO;
import com.example.moduleuser.model.dto.admin.out.AdminMemberDTO;
import com.example.moduleuser.model.dto.admin.page.AdminMemberPageDTO;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

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
public class AdminMemberControllerIT {

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

    private Member member;

    private static final int ALL_MEMBER_COUNT = 40;

    private static final String URL_PREFIX = "/api/admin/";


    @BeforeEach
    void init() {
        MemberAndAuthFixtureDTO memberAndAuthFixtureDTO = MemberAndAuthFixture.createDefaultMember(ALL_MEMBER_COUNT);
        MemberAndAuthFixtureDTO adminFixture = MemberAndAuthFixture.createAdmin();
        List<Member> saveMemberList = new ArrayList<>(memberAndAuthFixtureDTO.memberList());
        saveMemberList.addAll(adminFixture.memberList());
        List<Auth> saveAuthList = new ArrayList<>(adminFixture.authList());
        saveAuthList.addAll(adminFixture.authList());
        memberRepository.saveAll(saveMemberList);
        authRepository.saveAll(saveAuthList);

        member = memberAndAuthFixtureDTO.memberList().get(0);
        Member admin = adminFixture.memberList().get(0);

        tokenMap = tokenFixture.createAndSaveAllToken(admin);
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
    @DisplayName(value = "회원 목록 조회")
    void getMemberList() throws Exception {
        AdminMemberPageDTO pageDTOFixture = new AdminMemberPageDTO(null, null, 1);
        int contentSize = Math.min(ALL_MEMBER_COUNT + 1, pageDTOFixture.amount());
        int totalPages = PaginationUtils.getTotalPages(ALL_MEMBER_COUNT + 1, pageDTOFixture.amount());

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "member")
                        .header(accessHeader, accessTokenValue)
                        .cookie(new Cookie(refreshHeader, refreshTokenValue))
                        .cookie(new Cookie(inoHeader, inoValue)))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        PagingResponseDTO<AdminMemberDTO> response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertFalse(response.content().isEmpty());
        assertFalse(response.empty());
        assertEquals(contentSize, response.content().size());
        assertEquals(totalPages, response.totalPages());
    }

    @Test
    @DisplayName(value = "회원 목록 조회. 사용자 아이디 기반 검색. 검색은 like가 아닌 equal")
    void getMemberListSearchId() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "member")
                        .param("keyword", member.getUserId())
                        .param("searchType", "userId")
                        .header(accessHeader, accessTokenValue)
                        .cookie(new Cookie(refreshHeader, refreshTokenValue))
                        .cookie(new Cookie(inoHeader, inoValue)))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        PagingResponseDTO<AdminMemberDTO> response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertFalse(response.content().isEmpty());
        assertFalse(response.empty());
        assertEquals(1, response.content().size());
        assertEquals(1, response.totalPages());

        AdminMemberDTO responseDTO = response.content().get(0);
        assertEquals(member.getUserId(), responseDTO.userId());
        assertEquals(member.getUserName(), responseDTO.userName());
        assertEquals(member.getNickname(), responseDTO.nickname());
        assertEquals(member.getPhone(), responseDTO.phone());
        assertEquals(member.getUserEmail(), responseDTO.email());
        assertEquals(member.getBirth(), responseDTO.birth());
        assertEquals(member.getMemberPoint(), responseDTO.point());
    }

    @Test
    @DisplayName(value = "회원 목록 조회. 사용자 이름 기반 검색. 검색은 like가 아닌 equal")
    void getMemberListSearchUsername() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "member")
                        .param("keyword", member.getUserName())
                        .param("searchType", "userName")
                        .header(accessHeader, accessTokenValue)
                        .cookie(new Cookie(refreshHeader, refreshTokenValue))
                        .cookie(new Cookie(inoHeader, inoValue)))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        PagingResponseDTO<AdminMemberDTO> response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertFalse(response.content().isEmpty());
        assertFalse(response.empty());
        assertEquals(1, response.content().size());
        assertEquals(1, response.totalPages());

        AdminMemberDTO responseDTO = response.content().get(0);
        assertEquals(member.getUserId(), responseDTO.userId());
        assertEquals(member.getUserName(), responseDTO.userName());
        assertEquals(member.getNickname(), responseDTO.nickname());
        assertEquals(member.getPhone(), responseDTO.phone());
        assertEquals(member.getUserEmail(), responseDTO.email());
        assertEquals(member.getBirth(), responseDTO.birth());
        assertEquals(member.getMemberPoint(), responseDTO.point());
    }

    @Test
    @DisplayName(value = "회원 목록 조회. 사용자 닉네임 기반 검색. 검색은 like가 아닌 equal")
    void getMemberListSearchNickname() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "member")
                        .param("keyword", member.getNickname())
                        .param("searchType", "nickname")
                        .header(accessHeader, accessTokenValue)
                        .cookie(new Cookie(refreshHeader, refreshTokenValue))
                        .cookie(new Cookie(inoHeader, inoValue)))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        PagingResponseDTO<AdminMemberDTO> response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertFalse(response.content().isEmpty());
        assertFalse(response.empty());
        assertEquals(1, response.content().size());
        assertEquals(1, response.totalPages());

        AdminMemberDTO responseDTO = response.content().get(0);
        assertEquals(member.getUserId(), responseDTO.userId());
        assertEquals(member.getUserName(), responseDTO.userName());
        assertEquals(member.getNickname(), responseDTO.nickname());
        assertEquals(member.getPhone(), responseDTO.phone());
        assertEquals(member.getUserEmail(), responseDTO.email());
        assertEquals(member.getBirth(), responseDTO.birth());
        assertEquals(member.getMemberPoint(), responseDTO.point());
    }

    @Test
    @DisplayName(value = "포인트 지급")
    void postPoint() throws Exception {
        AdminPostPointDTO pointDTO = new AdminPostPointDTO(member.getUserId(), 1000);
        long pointFixture = member.getMemberPoint() + pointDTO.point();
        String requestDTO = om.writeValueAsString(pointDTO);
        mockMvc.perform(patch(URL_PREFIX + "member/point")
                        .header(accessHeader, accessTokenValue)
                        .cookie(new Cookie(refreshHeader, refreshTokenValue))
                        .cookie(new Cookie(inoHeader, inoValue))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestDTO))
                .andExpect(status().isOk())
                .andReturn();

        Member patchData = memberRepository.findByUserId(member.getUserId());
        assertNotNull(patchData);
        assertEquals(pointFixture, patchData.getMemberPoint());
    }

    @Test
    @DisplayName(value = "포인트 지급. 사용자 아이디가 잘못된 경우")
    void postPointWrongUserId() throws Exception {
        AdminPostPointDTO pointDTO = new AdminPostPointDTO("noneMemberId", 1000);
        String requestDTO = om.writeValueAsString(pointDTO);
        MvcResult result = mockMvc.perform(patch(URL_PREFIX + "member/point")
                        .header(accessHeader, accessTokenValue)
                        .cookie(new Cookie(refreshHeader, refreshTokenValue))
                        .cookie(new Cookie(inoHeader, inoValue))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestDTO))
                .andExpect(status().isBadRequest())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.BAD_REQUEST.getMessage(), response.errorMessage());
    }
}
