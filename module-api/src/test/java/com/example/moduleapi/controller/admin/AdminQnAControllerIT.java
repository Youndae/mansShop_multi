package com.example.moduleapi.controller.admin;

import com.example.moduleapi.ModuleApiApplication;
import com.example.moduleapi.config.exception.ExceptionEntity;
import com.example.moduleapi.fixture.TokenFixture;
import com.example.moduleapi.model.response.PagingResponseDTO;
import com.example.modulecommon.fixture.*;
import com.example.modulecommon.model.dto.MemberAndAuthFixtureDTO;
import com.example.modulecommon.model.dto.admin.qna.out.AdminQnAListResponseDTO;
import com.example.modulecommon.model.dto.page.AdminQnAPageDTO;
import com.example.modulecommon.model.dto.qna.in.QnAReplyInsertDTO;
import com.example.modulecommon.model.dto.qna.in.QnAReplyPatchDTO;
import com.example.modulecommon.model.dto.qna.out.QnADetailReplyDTO;
import com.example.modulecommon.model.dto.response.ResponseMessageDTO;
import com.example.modulecommon.model.entity.*;
import com.example.modulecommon.model.enumuration.*;
import com.example.modulecommon.utils.PaginationUtils;
import com.example.modulemypage.model.dto.memberQnA.out.MemberQnADetailResponseDTO;
import com.example.modulemypage.model.dto.memberQnA.out.QnAClassificationDTO;
import com.example.modulemypage.repository.MemberQnAReplyRepository;
import com.example.modulemypage.repository.MemberQnARepository;
import com.example.modulemypage.repository.QnAClassificationRepository;
import com.example.modulenotification.repository.NotificationRepository;
import com.example.moduleproduct.model.dto.productQnA.out.ProductQnADetailResponseDTO;
import com.example.moduleproduct.repository.classification.ClassificationRepository;
import com.example.moduleproduct.repository.product.ProductRepository;
import com.example.moduleproduct.repository.productOption.ProductOptionRepository;
import com.example.moduleproduct.repository.productQnA.ProductQnARepository;
import com.example.moduleproduct.repository.productQnAReply.ProductQnAReplyRepository;
import com.example.moduleuser.repository.AuthRepository;
import com.example.moduleuser.repository.MemberRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest(classes = ModuleApiApplication.class)
@EntityScan(basePackages = "com.example.modulecommon")
@EnableJpaRepositories(basePackages = "com.example")
@ComponentScan(basePackages = {"com.example.moduleconfig", "com.example.modulecommon"})
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class AdminQnAControllerIT {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private TokenFixture tokenFixture;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private RedisTemplate<String, Long> cacheRedisTemplate;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private AuthRepository authRepository;

    @Autowired
    private ClassificationRepository classificationRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductOptionRepository productOptionRepository;

    @Autowired
    private ProductQnARepository productQnARepository;

    @Autowired
    private ProductQnAReplyRepository productQnAReplyRepository;

    @Autowired
    private QnAClassificationRepository qnAClassificationRepository;

    @Autowired
    private MemberQnARepository memberQnARepository;

    @Autowired
    private MemberQnAReplyRepository memberQnAReplyRepository;

    @Autowired
    private NotificationRepository notificationRepository;

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

    private List<ProductQnA> answerCompleteProductQnAList;

    private List<ProductQnA> newProductQnAList;

    private List<ProductQnA> allProductQnAList;

    private List<ProductQnAReply> productQnaReplyList;

    private List<QnAClassification> qnAClassificationList;

    private List<MemberQnA> answerCompleteMemberQnAList;

    private List<MemberQnA> newMemberQnAList;

    private List<MemberQnA> allMemberQnAList;

    private List<MemberQnAReply> memberQnAReplyList;

    private Member admin;

    private static final String PRODUCT_QNA_CACHING_KEY = RedisCaching.ADMIN_PRODUCT_QNA_COUNT.getKey();

    private static final String MEMBER_QNA_CACHING_KEY = RedisCaching.ADMIN_MEMBER_QNA_COUNT.getKey();

    private static final String ALL_LIST_TYPE = "all";

    private static final String NEW_LIST_TYPE = "new";

    private static final String URL_PREFIX = "/api/admin/";

    @BeforeEach
    void init() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .build();

        MemberAndAuthFixtureDTO memberAndAuthFixtureDTO = MemberAndAuthFixture.createDefaultMember(40);
        MemberAndAuthFixtureDTO adminFixture = MemberAndAuthFixture.createAdmin();
        List<Member> memberList = memberAndAuthFixtureDTO.memberList();
        List<Member> saveMemberList = new ArrayList<>(memberList);
        saveMemberList.addAll(adminFixture.memberList());
        List<Auth> saveAuthList = new ArrayList<>(adminFixture.authList());
        saveAuthList.addAll(adminFixture.authList());
        memberRepository.saveAll(saveMemberList);
        authRepository.saveAll(saveAuthList);

        admin = adminFixture.memberList().get(0);

        tokenMap = tokenFixture.createAndSaveAllToken(admin);
        accessTokenValue = tokenMap.get(accessHeader);
        refreshTokenValue = tokenMap.get(refreshHeader);
        inoValue = tokenMap.get(inoHeader);

        List<Classification> classificationList = ClassificationFixture.createClassifications();
        classificationRepository.saveAll(classificationList);

        List<Product> productList = ProductFixture.createProductFixtureList(10, classificationList.get(0));
        List<ProductOption> productOptionList = productList.stream()
                .flatMap(v -> v.getProductOptions().stream())
                .toList();
        productRepository.saveAll(productList);
        productOptionRepository.saveAll(productOptionList);

        answerCompleteProductQnAList = ProductQnAFixture.createProductQnACompletedAnswer(memberList, productList);
        newProductQnAList = ProductQnAFixture.createDefaultProductQnA(memberList, productList);
        allProductQnAList = new ArrayList<>(answerCompleteProductQnAList);
        allProductQnAList.addAll(newProductQnAList);
        productQnARepository.saveAll(allProductQnAList);
        productQnaReplyList = ProductQnAFixture.createDefaultProductQnaReply(admin, answerCompleteProductQnAList);
        productQnAReplyRepository.saveAll(productQnaReplyList);

        qnAClassificationList = QnAClassificationFixture.createQnAClassificationList();
        qnAClassificationRepository.saveAll(qnAClassificationList);

        answerCompleteMemberQnAList = MemberQnAFixture.createMemberQnACompletedAnswer(qnAClassificationList, memberList);
        newMemberQnAList = MemberQnAFixture.createDefaultMemberQnA(qnAClassificationList, memberList);
        allMemberQnAList = new ArrayList<>(answerCompleteMemberQnAList);
        allMemberQnAList.addAll(newMemberQnAList);
        memberQnARepository.saveAll(allMemberQnAList);
        memberQnAReplyList = MemberQnAFixture.createMemberQnAReply(answerCompleteMemberQnAList, admin);
        memberQnAReplyRepository.saveAll(memberQnAReplyList);
    }

    @AfterEach
    void cleanUP() {
        notificationRepository.deleteAll();
        memberQnAReplyRepository.deleteAll();
        memberQnARepository.deleteAll();
        qnAClassificationRepository.deleteAll();
        productQnAReplyRepository.deleteAll();
        productQnARepository.deleteAll();
        productOptionRepository.deleteAll();
        productRepository.deleteAll();
        classificationRepository.deleteAll();
        authRepository.deleteAll();
        memberRepository.deleteAll();

        String accessKey = tokenMap.get("accessKey");
        String refreshKey = tokenMap.get("refreshKey");

        redisTemplate.delete(accessKey);
        redisTemplate.delete(refreshKey);

        cacheRedisTemplate.delete(PRODUCT_QNA_CACHING_KEY);
        cacheRedisTemplate.delete(MEMBER_QNA_CACHING_KEY);
    }

    @Test
    @DisplayName(value = "상품 문의 전체 목록 조회")
    void getAllProductQnA() throws Exception {
        AdminQnAPageDTO pageDTO = new AdminQnAPageDTO(null, null, 1);
        List<ProductQnA> fixtureList = allProductQnAList.stream()
                .limit(pageDTO.amount())
                .toList();
        int contentSize = Math.min(fixtureList.size(), pageDTO.amount());
        int totalPages = PaginationUtils.getTotalPages(allProductQnAList.size(), pageDTO.amount());

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "qna/product")
                        .header(accessHeader, accessTokenValue)
                        .cookie(new Cookie(refreshHeader, refreshTokenValue))
                        .cookie(new Cookie(inoHeader, inoValue))
                        .param("type", ALL_LIST_TYPE))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        PagingResponseDTO<AdminQnAListResponseDTO> response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertFalse(response.content().isEmpty());
        assertFalse(response.empty());
        assertEquals(contentSize, response.content().size());
        assertEquals(totalPages, response.totalPages());

        Long cachingCount = cacheRedisTemplate.opsForValue().get(PRODUCT_QNA_CACHING_KEY);
        assertNotNull(cachingCount);
        assertEquals(allProductQnAList.size(), cachingCount);
    }

    @Test
    @DisplayName(value = "상품 문의 미처리 목록 조회")
    void getNewProductQnA() throws Exception {
        AdminQnAPageDTO pageDTO = new AdminQnAPageDTO(null, null, 1);
        List<ProductQnA> fixtureList = newProductQnAList.stream()
                .limit(pageDTO.amount())
                .toList();
        int contentSize = Math.min(fixtureList.size(), pageDTO.amount());
        int totalPages = PaginationUtils.getTotalPages(newProductQnAList.size(), pageDTO.amount());

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "qna/product")
                        .header(accessHeader, accessTokenValue)
                        .cookie(new Cookie(refreshHeader, refreshTokenValue))
                        .cookie(new Cookie(inoHeader, inoValue))
                        .param("type", NEW_LIST_TYPE))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        PagingResponseDTO<AdminQnAListResponseDTO> response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertFalse(response.content().isEmpty());
        assertFalse(response.empty());
        assertEquals(contentSize, response.content().size());
        assertEquals(totalPages, response.totalPages());

        Long cachingCount = cacheRedisTemplate.opsForValue().get(PRODUCT_QNA_CACHING_KEY);
        assertNull(cachingCount);
    }

    @Test
    @DisplayName(value = "상품 문의 전체 목록 조회. 아이디 또는 닉네임 기반 검색")
    void getAllProductQnASearch() throws Exception {
        AdminQnAPageDTO pageDTO = new AdminQnAPageDTO(null, null, 1);
        ProductQnA fixture = allProductQnAList.get(0);
        List<ProductQnA> fixtureList = allProductQnAList.stream()
                .filter(v ->
                        v.getMember().getUserId().contains(fixture.getMember().getUserId()) ||
                                v.getMember().getNickname().contains(fixture.getMember().getUserId())
                )
                .toList();
        List<ProductQnA> fixtureContentList = fixtureList.stream().limit(pageDTO.amount()).toList();
        int contentSize = Math.min(fixtureContentList.size(), pageDTO.amount());
        int totalPages = PaginationUtils.getTotalPages(fixtureList.size(), pageDTO.amount());

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "qna/product")
                        .header(accessHeader, accessTokenValue)
                        .cookie(new Cookie(refreshHeader, refreshTokenValue))
                        .cookie(new Cookie(inoHeader, inoValue))
                        .param("type", ALL_LIST_TYPE)
                        .param("keyword", fixture.getMember().getUserId()))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        PagingResponseDTO<AdminQnAListResponseDTO> response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertFalse(response.content().isEmpty());
        assertFalse(response.empty());
        assertEquals(contentSize, response.content().size());
        assertEquals(totalPages, response.totalPages());

        Long cachingCount = cacheRedisTemplate.opsForValue().get(PRODUCT_QNA_CACHING_KEY);
        assertNull(cachingCount);
    }

    @Test
    @DisplayName(value = "상품 문의 전체 목록 조회. 데이터가 없는 경우")
    void getAllProductQnAEmpty() throws Exception {
        productQnARepository.deleteAll();

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "qna/product")
                        .header(accessHeader, accessTokenValue)
                        .cookie(new Cookie(refreshHeader, refreshTokenValue))
                        .cookie(new Cookie(inoHeader, inoValue))
                        .param("type", ALL_LIST_TYPE))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        PagingResponseDTO<AdminQnAListResponseDTO> response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertTrue(response.content().isEmpty());
        assertTrue(response.empty());
        assertEquals(0, response.totalPages());

        Long cachingCount = cacheRedisTemplate.opsForValue().get(PRODUCT_QNA_CACHING_KEY);
        assertEquals(0, cachingCount);
    }

    @Test
    @DisplayName(value = "상품 문의 미처리 목록 조회. 데이터가 없는 경우")
    void getNewProductQnAEmpty() throws Exception {
        productQnARepository.deleteAll();

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "qna/product")
                        .header(accessHeader, accessTokenValue)
                        .cookie(new Cookie(refreshHeader, refreshTokenValue))
                        .cookie(new Cookie(inoHeader, inoValue))
                        .param("type", NEW_LIST_TYPE))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        PagingResponseDTO<AdminQnAListResponseDTO> response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertTrue(response.content().isEmpty());
        assertTrue(response.empty());
        assertEquals(0, response.totalPages());

        Long cachingCount = cacheRedisTemplate.opsForValue().get(PRODUCT_QNA_CACHING_KEY);
        assertNull(cachingCount);
    }

    @Test
    @DisplayName(value = "답변 완료 된 상품 문의 상세 조회")
    void getAnswerProductQnADetail() throws Exception {
        ProductQnA fixture = answerCompleteProductQnAList.get(0);
        ProductQnAReply fixtureReply = productQnaReplyList.stream()
                .filter(v ->
                        v.getProductQnA().getId().equals(fixture.getId())
                )
                .toList()
                .get(0);

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "qna/product/" + fixture.getId())
                        .header(accessHeader, accessTokenValue)
                        .cookie(new Cookie(refreshHeader, refreshTokenValue))
                        .cookie(new Cookie(inoHeader, inoValue)))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ProductQnADetailResponseDTO response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(fixture.getId(), response.productQnAId());
        assertEquals(fixture.getProduct().getProductName(), response.productName());
        assertEquals(fixture.getMember().getNickname(), response.writer());
        assertEquals(fixture.getQnaContent(), response.qnaContent());
        assertEquals(fixture.isProductQnAStat(), response.productQnAStat());
        assertEquals(1, response.replyList().size());

        QnADetailReplyDTO replyResponse = response.replyList().get(0);
        assertEquals(fixtureReply.getId(), replyResponse.replyId());
        assertEquals(fixtureReply.getMember().getNickname(), replyResponse.writer());
        assertEquals(fixtureReply.getReplyContent(), replyResponse.replyContent());
    }

    @Test
    @DisplayName(value = "미답변 상품 문의 상세 조회")
    void getNewProductQnADetail() throws Exception {
        ProductQnA fixture = newProductQnAList.get(0);

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "qna/product/" + fixture.getId())
                        .header(accessHeader, accessTokenValue)
                        .cookie(new Cookie(refreshHeader, refreshTokenValue))
                        .cookie(new Cookie(inoHeader, inoValue)))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ProductQnADetailResponseDTO response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(fixture.getId(), response.productQnAId());
        assertEquals(fixture.getProduct().getProductName(), response.productName());
        assertEquals(fixture.getMember().getNickname(), response.writer());
        assertEquals(fixture.getQnaContent(), response.qnaContent());
        assertEquals(fixture.isProductQnAStat(), response.productQnAStat());
        assertTrue(response.replyList().isEmpty());
    }

    @Test
    @DisplayName(value = "상품 문의 상세 조회. 상품 문의 아이디가 잘못된 경우")
    void getProductQnADetailWrongId() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "qna/product/0")
                        .header(accessHeader, accessTokenValue)
                        .cookie(new Cookie(refreshHeader, refreshTokenValue))
                        .cookie(new Cookie(inoHeader, inoValue)))
                .andExpect(status().is(400))
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.NOT_FOUND.getMessage(), response.errorMessage());
    }

    @Test
    @DisplayName(value = "상품 문의 답변 상태를 완료로 수정")
    void patchProductQnAComplete() throws Exception {
        ProductQnA fixture = newProductQnAList.get(0);

        MvcResult result = mockMvc.perform(patch(URL_PREFIX + "qna/product/" + fixture.getId())
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

        ProductQnA patchData = productQnARepository.findById(fixture.getId()).orElse(null);
        assertNotNull(patchData);
        assertTrue(patchData.isProductQnAStat());
    }

    @Test
    @DisplayName(value = "상품 문의 답변 상태를 완료로 수정. 문의 아이디가 잘못된 경우")
    void patchProductQnACompleteWrongId() throws Exception {
        MvcResult result = mockMvc.perform(patch(URL_PREFIX + "qna/product/0")
                        .header(accessHeader, accessTokenValue)
                        .cookie(new Cookie(refreshHeader, refreshTokenValue))
                        .cookie(new Cookie(inoHeader, inoValue)))
                .andExpect(status().is(400))
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.NOT_FOUND.getMessage(), response.errorMessage());
    }

    @Test
    @DisplayName(value = "상품 문의 답변 작성.")
    void postProductQnAReply() throws Exception {
        ProductQnA fixture = newProductQnAList.get(0);
        QnAReplyInsertDTO insertDTO = new QnAReplyInsertDTO(fixture.getId(), "test insert productQnA Reply content");
        String requestDTO = om.writeValueAsString(insertDTO);

        MvcResult result = mockMvc.perform(post(URL_PREFIX + "qna/product/reply")
                        .header(accessHeader, accessTokenValue)
                        .cookie(new Cookie(refreshHeader, refreshTokenValue))
                        .cookie(new Cookie(inoHeader, inoValue))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestDTO))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ResponseMessageDTO response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(Result.OK.getResultKey(), response.message());

        List<QnADetailReplyDTO> saveReplyList = productQnAReplyRepository.findAllByQnAId(fixture.getId());
        assertFalse(saveReplyList.isEmpty());
        assertEquals(1, saveReplyList.size());

        QnADetailReplyDTO replyResponse = saveReplyList.get(0);

        assertEquals(admin.getNickname(), replyResponse.writer());
        assertEquals(insertDTO.content(), replyResponse.replyContent());

        await()
                .atMost(20, TimeUnit.SECONDS)
                .pollInterval(200, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    List<Notification> notificationList = notificationRepository.findAll();
                    assertFalse(notificationList.isEmpty());
                    assertEquals(1, notificationList.size());
                    Notification notification = notificationList.get(0);
                    String notificationTitle = fixture.getProduct().getProductName() + NotificationType.PRODUCT_QNA_REPLY.getTitle();
                    assertEquals(fixture.getMember().getUserId(), notification.getMember().getUserId());
                    assertEquals(notificationTitle, notification.getTitle());
                    assertEquals(fixture.getId(), notification.getRelatedId());
                });
    }

    @Test
    @DisplayName(value = "상품 문의 답변 작성. 문의 아이디가 잘못된 경우")
    void postProductQnAReplyWrongId() throws Exception {
        QnAReplyInsertDTO insertDTO = new QnAReplyInsertDTO(0L, "test insert productQnA Reply content");
        String requestDTO = om.writeValueAsString(insertDTO);

        MvcResult result = mockMvc.perform(post(URL_PREFIX + "qna/product/reply")
                        .header(accessHeader, accessTokenValue)
                        .cookie(new Cookie(refreshHeader, refreshTokenValue))
                        .cookie(new Cookie(inoHeader, inoValue))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestDTO))
                .andExpect(status().is(400))
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.NOT_FOUND.getMessage(), response.errorMessage());
    }

    @Test
    @DisplayName(value = "상품 문의 답변 수정")
    void patchProductQnAReply() throws Exception {
        ProductQnAReply fixture = productQnaReplyList.get(0);
        QnAReplyPatchDTO replyDTO = new QnAReplyPatchDTO(fixture.getId(), "test patch productQnA Reply content");
        String requestDTO = om.writeValueAsString(replyDTO);

        MvcResult result = mockMvc.perform(patch(URL_PREFIX + "qna/product/reply")
                        .header(accessHeader, accessTokenValue)
                        .cookie(new Cookie(refreshHeader, refreshTokenValue))
                        .cookie(new Cookie(inoHeader, inoValue))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestDTO))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ResponseMessageDTO response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(Result.OK.getResultKey(), response.message());

        ProductQnAReply patchData = productQnAReplyRepository.findById(fixture.getId()).orElse(null);
        assertNotNull(patchData);
        assertEquals(replyDTO.content(), patchData.getReplyContent());
    }

    @Test
    @DisplayName(value = "상품 문의 답변 수정. 답변 아이디가 잘못된 경우")
    void patchProductQnAReplyWrongId() throws Exception {
        QnAReplyPatchDTO replyDTO = new QnAReplyPatchDTO(0L, "test patch productQnA Reply content");
        String requestDTO = om.writeValueAsString(replyDTO);

        MvcResult result = mockMvc.perform(patch(URL_PREFIX + "qna/product/reply")
                        .header(accessHeader, accessTokenValue)
                        .cookie(new Cookie(refreshHeader, refreshTokenValue))
                        .cookie(new Cookie(inoHeader, inoValue))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestDTO))
                .andExpect(status().is(400))
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.NOT_FOUND.getMessage(), response.errorMessage());
    }

    @Test
    @DisplayName(value = "전체 회원 문의 목록 조회")
    void getAllMemberQnAList() throws Exception {
        AdminQnAPageDTO pageDTO = new AdminQnAPageDTO(null, null, 1);
        List<MemberQnA> fixtureList = allMemberQnAList.stream()
                .limit(pageDTO.amount())
                .toList();
        int contentSize = Math.min(fixtureList.size(), pageDTO.amount());
        int totalPages = PaginationUtils.getTotalPages(allMemberQnAList.size(), pageDTO.amount());

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "qna/member")
                        .header(accessHeader, accessTokenValue)
                        .cookie(new Cookie(refreshHeader, refreshTokenValue))
                        .cookie(new Cookie(inoHeader, inoValue))
                        .param("type", ALL_LIST_TYPE))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        PagingResponseDTO<AdminQnAListResponseDTO> response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertFalse(response.content().isEmpty());
        assertFalse(response.empty());
        assertEquals(contentSize, response.content().size());
        assertEquals(totalPages, response.totalPages());

        Long cachingCount = cacheRedisTemplate.opsForValue().get(MEMBER_QNA_CACHING_KEY);
        assertNotNull(cachingCount);
        assertEquals(allMemberQnAList.size(), cachingCount);
    }

    @Test
    @DisplayName(value = "미처리 회원 문의 목록 조회")
    void getNewMemberQnAList() throws Exception {
        AdminQnAPageDTO pageDTO = new AdminQnAPageDTO(null, null, 1);
        List<MemberQnA> fixtureList = newMemberQnAList.stream()
                .limit(pageDTO.amount())
                .toList();
        int contentSize = Math.min(fixtureList.size(), pageDTO.amount());
        int totalPages = PaginationUtils.getTotalPages(newMemberQnAList.size(), pageDTO.amount());

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "qna/member")
                        .header(accessHeader, accessTokenValue)
                        .cookie(new Cookie(refreshHeader, refreshTokenValue))
                        .cookie(new Cookie(inoHeader, inoValue))
                        .param("type", NEW_LIST_TYPE))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        PagingResponseDTO<AdminQnAListResponseDTO> response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertFalse(response.content().isEmpty());
        assertFalse(response.empty());
        assertEquals(contentSize, response.content().size());
        assertEquals(totalPages, response.totalPages());

        Long cachingCount = cacheRedisTemplate.opsForValue().get(MEMBER_QNA_CACHING_KEY);
        assertNull(cachingCount);
    }

    @Test
    @DisplayName(value = "전체 회원 문의 목록 조회. 아이디 또는 닉네임 기반 검색")
    void getAllMemberQnAListSearch() throws Exception {
        MemberQnA fixture = allMemberQnAList.get(0);
        AdminQnAPageDTO pageDTO = new AdminQnAPageDTO(null, 1);
        List<MemberQnA> fixtureList = allMemberQnAList.stream()
                .filter(v ->
                        v.getMember().getUserId().contains(fixture.getMember().getUserId()) ||
                                v.getMember().getNickname().contains(fixture.getMember().getUserId())
                )
                .toList();
        List<MemberQnA> fixtureContentList = fixtureList.stream().limit(pageDTO.amount()).toList();
        int contentSize = Math.min(fixtureContentList.size(), pageDTO.amount());
        int totalPages = PaginationUtils.getTotalPages(fixtureList.size(), pageDTO.amount());

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "qna/member")
                        .header(accessHeader, accessTokenValue)
                        .cookie(new Cookie(refreshHeader, refreshTokenValue))
                        .cookie(new Cookie(inoHeader, inoValue))
                        .param("type", ALL_LIST_TYPE)
                        .param("keyword", fixture.getMember().getUserId()))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        PagingResponseDTO<AdminQnAListResponseDTO> response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertFalse(response.content().isEmpty());
        assertFalse(response.empty());
        assertEquals(contentSize, response.content().size());
        assertEquals(totalPages, response.totalPages());

        Long cachingCount = cacheRedisTemplate.opsForValue().get(MEMBER_QNA_CACHING_KEY);
        assertNull(cachingCount);
    }

    @Test
    @DisplayName(value = "회원 문의 전체 목록 조회. 데이터가 없는 경우")
    void getAllMemberQnAEmpty() throws Exception {
        memberQnARepository.deleteAll();

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "qna/member")
                        .header(accessHeader, accessTokenValue)
                        .cookie(new Cookie(refreshHeader, refreshTokenValue))
                        .cookie(new Cookie(inoHeader, inoValue))
                        .param("type", ALL_LIST_TYPE))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        PagingResponseDTO<AdminQnAListResponseDTO> response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertTrue(response.content().isEmpty());
        assertTrue(response.empty());
        assertEquals(0, response.totalPages());

        Long cachingCount = cacheRedisTemplate.opsForValue().get(MEMBER_QNA_CACHING_KEY);
        assertEquals(0, cachingCount);
    }

    @Test
    @DisplayName(value = "회원 문의 미처리 목록 조회. 데이터가 없는 경우")
    void getNewMemberQnAEmpty() throws Exception {
        memberQnARepository.deleteAll();

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "qna/member")
                        .header(accessHeader, accessTokenValue)
                        .cookie(new Cookie(refreshHeader, refreshTokenValue))
                        .cookie(new Cookie(inoHeader, inoValue))
                        .param("type", NEW_LIST_TYPE))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        PagingResponseDTO<AdminQnAListResponseDTO> response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertTrue(response.content().isEmpty());
        assertTrue(response.empty());
        assertEquals(0, response.totalPages());

        Long cachingCount = cacheRedisTemplate.opsForValue().get(MEMBER_QNA_CACHING_KEY);
        assertNull(cachingCount);
    }

    @Test
    @DisplayName(value = "답변이 있는 회원 문의 상세 조회")
    void getAnswerMemberQnADetail() throws Exception {
        MemberQnA fixture = answerCompleteMemberQnAList.get(0);
        List<QnADetailReplyDTO> replyFixtureList = memberQnAReplyList.stream()
                .filter(v ->
                        v.getMemberQnA().getId().equals(fixture.getId())
                )
                .map(v ->
                        new QnADetailReplyDTO(
                                v.getId(),
                                v.getMember().getNickname(),
                                v.getReplyContent(),
                                v.getUpdatedAt()
                        )
                )
                .toList();

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "qna/member/" + fixture.getId())
                        .header(accessHeader, accessTokenValue)
                        .cookie(new Cookie(refreshHeader, refreshTokenValue))
                        .cookie(new Cookie(inoHeader, inoValue)))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        MemberQnADetailResponseDTO response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(fixture.getId(), response.memberQnAId());
        assertEquals(fixture.getQnAClassification().getQnaClassificationName(), response.qnaClassification());
        assertEquals(fixture.getMemberQnATitle(), response.qnaTitle());
        assertEquals(fixture.getMember().getNickname(), response.writer());
        assertEquals(fixture.getMemberQnAContent(), response.qnaContent());
        assertEquals(fixture.isMemberQnAStat(), response.memberQnAStat());
        assertEquals(replyFixtureList.size(), response.replyList().size());

        replyFixtureList.forEach(v -> assertTrue(response.replyList().contains(v)));
    }

    @Test
    @DisplayName(value = "답변이 없는 회원 문의 상세 조회")
    void getNewMemberQnADetail() throws Exception {
        MemberQnA fixture = newMemberQnAList.get(0);

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "qna/member/" + fixture.getId())
                        .header(accessHeader, accessTokenValue)
                        .cookie(new Cookie(refreshHeader, refreshTokenValue))
                        .cookie(new Cookie(inoHeader, inoValue)))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        MemberQnADetailResponseDTO response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(fixture.getId(), response.memberQnAId());
        assertEquals(fixture.getQnAClassification().getQnaClassificationName(), response.qnaClassification());
        assertEquals(fixture.getMemberQnATitle(), response.qnaTitle());
        assertEquals(fixture.getMember().getNickname(), response.writer());
        assertEquals(fixture.getMemberQnAContent(), response.qnaContent());
        assertEquals(fixture.isMemberQnAStat(), response.memberQnAStat());
        assertTrue(response.replyList().isEmpty());
    }

    @Test
    @DisplayName(value = "회원 문의 상세 조회. 문의 아이디가 잘못된 경우")
    void getMemberQnADetailWrongId() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "qna/member/0")
                        .header(accessHeader, accessTokenValue)
                        .cookie(new Cookie(refreshHeader, refreshTokenValue))
                        .cookie(new Cookie(inoHeader, inoValue)))
                .andExpect(status().is(400))
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.NOT_FOUND.getMessage(), response.errorMessage());
    }

    @Test
    @DisplayName(value = "회원 문의 답변 완료 상태로 수정")
    void patchMemberQnAStatusComplete() throws Exception {
        MemberQnA fixture = newMemberQnAList.get(0);

        MvcResult result = mockMvc.perform(patch(URL_PREFIX + "qna/member/" + fixture.getId())
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

        MemberQnA patchData = memberQnARepository.findById(fixture.getId()).orElse(null);
        assertNotNull(patchData);
        assertTrue(patchData.isMemberQnAStat());
    }

    @Test
    @DisplayName(value = "회원 문의 답변 완료 상태로 수정. 문의 아이디가 잘못된 경우")
    void patchMemberQnAStatusCompleteWrongId() throws Exception {
        MvcResult result = mockMvc.perform(patch(URL_PREFIX + "qna/member/0")
                        .header(accessHeader, accessTokenValue)
                        .cookie(new Cookie(refreshHeader, refreshTokenValue))
                        .cookie(new Cookie(inoHeader, inoValue)))
                .andExpect(status().is(400))
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.NOT_FOUND.getMessage(), response.errorMessage());
    }

    @Test
    @DisplayName(value = "회원 문의 답변 작성")
    void postMemberQnAReply() throws Exception {
        MemberQnA fixture = newMemberQnAList.get(0);
        QnAReplyInsertDTO insertDTO = new QnAReplyInsertDTO(fixture.getId(), "test insert MemberQnA reply content");
        String requestDTO = om.writeValueAsString(insertDTO);

        MvcResult result = mockMvc.perform(post(URL_PREFIX + "qna/member/reply")
                        .header(accessHeader, accessTokenValue)
                        .cookie(new Cookie(refreshHeader, refreshTokenValue))
                        .cookie(new Cookie(inoHeader, inoValue))
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
        assertEquals(Result.OK.getResultKey(), response.message());

        MemberQnA patchData = memberQnARepository.findById(fixture.getId()).orElse(null);
        assertNotNull(patchData);
        assertTrue(patchData.isMemberQnAStat());

        List<QnADetailReplyDTO> insertReplyList = memberQnAReplyRepository.findAllByQnAId(fixture.getId());
        assertNotNull(insertReplyList);
        assertFalse(insertReplyList.isEmpty());
        assertEquals(1, insertReplyList.size());

        QnADetailReplyDTO replyDTO = insertReplyList.get(0);
        assertEquals(admin.getNickname(), replyDTO.writer());
        assertEquals(insertDTO.content(), replyDTO.replyContent());

        await()
                .atMost(20, TimeUnit.SECONDS)
                .pollInterval(200, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    List<Notification> notificationList = notificationRepository.findAll();
                    assertFalse(notificationList.isEmpty());
                    assertEquals(1, notificationList.size());

                    Notification notification = notificationList.get(0);
                    String notificationTitle = fixture.getMemberQnATitle() + NotificationType.MEMBER_QNA_REPLY.getTitle();
                    assertEquals(fixture.getMember().getUserId(), notification.getMember().getUserId());
                    assertEquals(notificationTitle, notification.getTitle());
                    assertEquals(fixture.getId(), notification.getRelatedId());
                });
    }

    @Test
    @DisplayName(value = "회원 문의 답변 작성. 회원 문의 아이디가 잘못된 경우")
    void postMemberQnAReplyWrongId() throws Exception {
        QnAReplyInsertDTO insertDTO = new QnAReplyInsertDTO(0L, "test insert MemberQnA reply content");
        String requestDTO = om.writeValueAsString(insertDTO);

        MvcResult result = mockMvc.perform(post(URL_PREFIX + "qna/member/reply")
                        .header(accessHeader, accessTokenValue)
                        .cookie(new Cookie(refreshHeader, refreshTokenValue))
                        .cookie(new Cookie(inoHeader, inoValue))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDTO))
                .andExpect(status().is(400))
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.NOT_FOUND.getMessage(), response.errorMessage());
    }

    @Test
    @DisplayName(value = "회원 문의 답변 수정")
    void patchMemberQnAReply() throws Exception {
        MemberQnAReply fixture = memberQnAReplyList.get(0);
        QnAReplyPatchDTO replyDTO = new QnAReplyPatchDTO(fixture.getId(), "test patch MemberQnA reply content");
        String requestDTO = om.writeValueAsString(replyDTO);

        MvcResult result = mockMvc.perform(patch(URL_PREFIX + "qna/member/reply")
                        .header(accessHeader, accessTokenValue)
                        .cookie(new Cookie(refreshHeader, refreshTokenValue))
                        .cookie(new Cookie(inoHeader, inoValue))
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
        assertEquals(Result.OK.getResultKey(), response.message());

        MemberQnAReply patchData = memberQnAReplyRepository.findById(fixture.getId()).orElse(null);
        assertNotNull(patchData);
        assertEquals(replyDTO.content(), patchData.getReplyContent());
    }

    @Test
    @DisplayName(value = "회원 문의 답변 수정. 답변 아이디가 잘못된 경우")
    void patchMemberQnAReplyWrongId() throws Exception {
        QnAReplyPatchDTO replyDTO = new QnAReplyPatchDTO(0L, "test patch MemberQnA reply content");
        String requestDTO = om.writeValueAsString(replyDTO);

        MvcResult result = mockMvc.perform(patch(URL_PREFIX + "qna/member/reply")
                        .header(accessHeader, accessTokenValue)
                        .cookie(new Cookie(refreshHeader, refreshTokenValue))
                        .cookie(new Cookie(inoHeader, inoValue))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDTO))
                .andExpect(status().is(400))
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.NOT_FOUND.getMessage(), response.errorMessage());
    }

    @Test
    @DisplayName(value = "회원 문의 분류 조회")
    void getQnAClassificationList() throws Exception {
        List<QnAClassificationDTO> fixture = qnAClassificationList.stream()
                .map(v ->
                        new QnAClassificationDTO(v.getId(), v.getQnaClassificationName())
                )
                .toList();

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "qna/classification")
                        .header(accessHeader, accessTokenValue)
                        .cookie(new Cookie(refreshHeader, refreshTokenValue))
                        .cookie(new Cookie(inoHeader, inoValue)))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        List<QnAClassificationDTO> response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertFalse(response.isEmpty());
        assertEquals(fixture.size(), response.size());

        fixture.forEach(v -> assertTrue(response.contains(v)));
    }

    @Test
    @DisplayName(value = "회원 문의 분류 조회. 데이터가 없는 경우")
    void getQnAClassificationListEmpty() throws Exception {
        qnAClassificationRepository.deleteAll();
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "qna/classification")
                        .header(accessHeader, accessTokenValue)
                        .cookie(new Cookie(refreshHeader, refreshTokenValue))
                        .cookie(new Cookie(inoHeader, inoValue)))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        List<QnAClassificationDTO> response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertTrue(response.isEmpty());
    }

    @Test
    @DisplayName(value = "회원 문의 분류 추가")
    void postQnAClassification() throws Exception {
        String classification = "testClassificationName";
        int classificationSize = qnAClassificationList.size() + 1;
        MvcResult result = mockMvc.perform(post(URL_PREFIX + "qna/classification")
                        .header(accessHeader, accessTokenValue)
                        .cookie(new Cookie(refreshHeader, refreshTokenValue))
                        .cookie(new Cookie(inoHeader, inoValue))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(classification))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ResponseMessageDTO response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(Result.OK.getResultKey(), response.message());

        List<QnAClassification> qnAClassifications = qnAClassificationRepository.findAll();
        assertNotNull(qnAClassifications);
        assertFalse(qnAClassifications.isEmpty());
        assertEquals(classificationSize, qnAClassifications.size());

        QnAClassification lastIdClassification = qnAClassifications.get(qnAClassifications.size() - 1);
        assertEquals(classification, lastIdClassification.getQnaClassificationName());
    }

    @Test
    @DisplayName(value = "회원 문의 분류 삭제")
    void deleteQnAClassification() throws Exception {
        Long deleteId = qnAClassificationList.get(0).getId();
        int classificationSize = qnAClassificationList.size() - 1;
        MvcResult result = mockMvc.perform(delete(URL_PREFIX + "qna/classification/" + deleteId)
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

        List<QnAClassification> qnAClassifications = qnAClassificationRepository.findAll();
        assertNotNull(qnAClassifications);
        assertFalse(qnAClassifications.isEmpty());
        assertEquals(classificationSize, qnAClassifications.size());

        QnAClassification deleteCheck = qnAClassificationRepository.findById(deleteId).orElse(null);
        assertNull(deleteCheck);
    }

    @Test
    @DisplayName(value = "회원 문의 분류 삭제. 분류 아이디가 잘못된 경우")
    void deleteQnAClassificationWrongId() throws Exception {
        MvcResult result = mockMvc.perform(delete(URL_PREFIX + "qna/classification/0")
                        .header(accessHeader, accessTokenValue)
                        .cookie(new Cookie(refreshHeader, refreshTokenValue))
                        .cookie(new Cookie(inoHeader, inoValue)))
                .andExpect(status().is(400))
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.NOT_FOUND.getMessage(), response.errorMessage());
    }
}
