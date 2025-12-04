package com.example.moduleapi.controller.admin;

import com.example.moduleapi.ModuleApiApplication;
import com.example.moduleapi.config.exception.ExceptionEntity;
import com.example.moduleapi.fixture.TokenFixture;
import com.example.moduleapi.model.response.PagingResponseDTO;
import com.example.modulecommon.fixture.ClassificationFixture;
import com.example.modulecommon.fixture.MemberAndAuthFixture;
import com.example.modulecommon.fixture.ProductFixture;
import com.example.modulecommon.fixture.ProductReviewFixture;
import com.example.modulecommon.model.dto.MemberAndAuthFixtureDTO;
import com.example.modulecommon.model.entity.*;
import com.example.modulecommon.model.enumuration.ErrorCode;
import com.example.modulecommon.model.enumuration.NotificationType;
import com.example.modulecommon.model.enumuration.RedisCaching;
import com.example.modulecommon.utils.PaginationUtils;
import com.example.moduleconfig.properties.CookieProperties;
import com.example.moduleconfig.properties.TokenProperties;
import com.example.modulenotification.repository.NotificationRepository;
import com.example.moduleproduct.model.dto.admin.review.in.AdminReviewReplyRequestDTO;
import com.example.moduleproduct.model.dto.admin.review.out.AdminReviewDTO;
import com.example.moduleproduct.model.dto.admin.review.out.AdminReviewDetailDTO;
import com.example.moduleproduct.model.dto.page.AdminReviewPageDTO;
import com.example.moduleproduct.repository.classification.ClassificationRepository;
import com.example.moduleproduct.repository.product.ProductRepository;
import com.example.moduleproduct.repository.productOption.ProductOptionRepository;
import com.example.moduleproduct.repository.productReview.ProductReviewRepository;
import com.example.moduleproduct.repository.productReviewReply.ProductReviewReplyRepository;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest(classes = ModuleApiApplication.class)
@EntityScan(basePackages = "com.example.modulecommon")
@EnableJpaRepositories(basePackages = "com.example")
@ComponentScan(basePackages = {"com.example.moduleconfig", "com.example.modulecommon"})
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class AdminReviewControllerIT {

    @Autowired
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
    private ProductReviewRepository productReviewRepository;

    @Autowired
    private ProductReviewReplyRepository productReviewReplyRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private TokenProperties tokenProperties;

    @Autowired
    private CookieProperties cookieProperties;

    private Map<String, String> tokenMap;

    private String accessTokenValue;

    private String refreshTokenValue;

    private String inoValue;

    private Member admin;

    private Member member;

    private List<ProductReview> answerCompleteReviewList;

    private List<ProductReview> newReviewList;

    private List<ProductReview> allReviewList;

    private List<ProductReviewReply> reviewReplyList;

    private Product product;

    private static final String REVIEW_CACHING_KEY = RedisCaching.ADMIN_REVIEW_COUNT.getKey();

    private static final String URL_PREFIX = "/api/admin/";

    @BeforeEach
    void init() {
        MemberAndAuthFixtureDTO memberAndAuthFixtureDTO = MemberAndAuthFixture.createDefaultMember(10);
        MemberAndAuthFixtureDTO adminFixture = MemberAndAuthFixture.createAdmin();
        List<Member> memberList = memberAndAuthFixtureDTO.memberList();
        List<Member> saveMemberList = new ArrayList<>(memberList);
        saveMemberList.addAll(adminFixture.memberList());
        List<Auth> saveAuthList = new ArrayList<>(adminFixture.authList());
        saveAuthList.addAll(adminFixture.authList());
        memberRepository.saveAll(saveMemberList);
        authRepository.saveAll(saveAuthList);

        member = memberList.get(0);
        admin = adminFixture.memberList().get(0);

        tokenMap = tokenFixture.createAndSaveAllToken(admin);
        accessTokenValue = tokenMap.get(tokenProperties.getAccess().getHeader());
        refreshTokenValue = tokenMap.get(tokenProperties.getRefresh().getHeader());
        inoValue = tokenMap.get(cookieProperties.getIno().getHeader());

        List<Classification> classificationList = ClassificationFixture.createClassifications();
        classificationRepository.saveAll(classificationList);

        List<Product> productList = ProductFixture.createProductFixtureList(10, classificationList.get(0));
        List<ProductOption> productOptionList = productList.stream()
                .flatMap(v ->
                        v.getProductOptions().stream()
                )
                .toList();
        productRepository.saveAll(productList);
        productOptionRepository.saveAll(productOptionList);
        product = productList.get(0);

        answerCompleteReviewList = ProductReviewFixture.createReviewWithCompletedAnswer(memberList, productOptionList);
        newReviewList = ProductReviewFixture.createDefaultReview(memberList, productOptionList);
        allReviewList = new ArrayList<>(answerCompleteReviewList);
        allReviewList.addAll(newReviewList);
        productReviewRepository.saveAll(allReviewList);
        reviewReplyList = ProductReviewFixture.createDefaultReviewReply(answerCompleteReviewList, admin);
        productReviewReplyRepository.saveAll(reviewReplyList);
    }

    @AfterEach
    void cleanUP() {
        notificationRepository.deleteAll();
        productReviewReplyRepository.deleteAll();
        productReviewRepository.deleteAll();
        productOptionRepository.deleteAll();
        productRepository.deleteAll();
        classificationRepository.deleteAll();
        authRepository.deleteAll();
        memberRepository.deleteAll();

        String accessKey = tokenMap.get("accessKey");
        String refreshKey = tokenMap.get("refreshKey");

        redisTemplate.delete(accessKey);
        redisTemplate.delete(refreshKey);

        cacheRedisTemplate.delete(REVIEW_CACHING_KEY);
    }

    @Test
    @DisplayName(value = "미처리 리뷰 목록 조회")
    void getNewReviewList() throws Exception {
        AdminReviewPageDTO pageDTO = new AdminReviewPageDTO(null, null, 1);
        List<ProductReview> fixture = newReviewList.stream()
                .limit(pageDTO.amount())
                .toList();
        int contentSize = Math.min(fixture.size(), pageDTO.amount());
        int totalPages = PaginationUtils.getTotalPages(newReviewList.size(), pageDTO.amount());

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "review")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        PagingResponseDTO<AdminReviewDTO> response = om.readValue(
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
    @DisplayName(value = "미처리 리뷰 목록 조회. 데이터가 없는 경우")
    void getNewReviewListEmpty() throws Exception {
        productReviewRepository.deleteAll();

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "review")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        PagingResponseDTO<AdminReviewDTO> response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertTrue(response.content().isEmpty());
        assertTrue(response.empty());
        assertEquals(0, response.totalPages());
    }

    @Test
    @DisplayName(value = "미처리 리뷰 목록 조회. 상품명 기반 검색")
    void getNewReviewListSearchProductName() throws Exception {
        AdminReviewPageDTO pageDTO = new AdminReviewPageDTO(null, null, 1);
        List<ProductReview> filterFixtureList = newReviewList.stream()
                .filter(v ->
                        v.getProduct().getProductName().contains(product.getProductName())
                )
                .toList();
        List<ProductReview> contentFixture = filterFixtureList.stream()
                .limit(pageDTO.amount())
                .toList();
        int contentSize = Math.min(contentFixture.size(), pageDTO.amount());
        int totalPages = PaginationUtils.getTotalPages(filterFixtureList.size(), pageDTO.amount());

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "review")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .param("keyword", product.getProductName())
                        .param("searchType", "product"))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        PagingResponseDTO<AdminReviewDTO> response = om.readValue(
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
    @DisplayName(value = "미처리 리뷰 목록 조회. 사용자 이름 또는 닉네임 기반 검색")
    void getNewReviewListSearchUser() throws Exception {
        AdminReviewPageDTO pageDTO = new AdminReviewPageDTO(null, null, 1);
        List<ProductReview> filterFixtureList = newReviewList.stream()
                .filter(v ->
                        v.getMember().getNickname().contains(member.getNickname()) ||
                                v.getMember().getUserName().contains(member.getNickname())
                )
                .toList();
        List<ProductReview> contentFixture = filterFixtureList.stream()
                .limit(pageDTO.amount())
                .toList();
        int contentSize = Math.min(contentFixture.size(), pageDTO.amount());
        int totalPages = PaginationUtils.getTotalPages(filterFixtureList.size(), pageDTO.amount());

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "review")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .param("keyword", member.getNickname())
                        .param("searchType", "user"))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        PagingResponseDTO<AdminReviewDTO> response = om.readValue(
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
    @DisplayName(value = "전체 리뷰 목록 조회")
    void getAllReviewList() throws Exception {
        AdminReviewPageDTO pageDTO = new AdminReviewPageDTO(null, null, 1);
        List<ProductReview> fixture = allReviewList.stream()
                .limit(pageDTO.amount())
                .toList();
        int contentSize = Math.min(fixture.size(), pageDTO.amount());
        int totalPages = PaginationUtils.getTotalPages(allReviewList.size(), pageDTO.amount());

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "review/all")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        PagingResponseDTO<AdminReviewDTO> response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertFalse(response.content().isEmpty());
        assertFalse(response.empty());
        assertEquals(contentSize, response.content().size());
        assertEquals(totalPages, response.totalPages());

        Long cachingResult = cacheRedisTemplate.opsForValue().get(REVIEW_CACHING_KEY);
        assertNotNull(cachingResult);
        assertEquals(allReviewList.size(), cachingResult);
    }

    @Test
    @DisplayName(value = "전체 리뷰 목록 조회. 데이터가 없는 경우")
    void getAllReviewListEmpty() throws Exception {
        productReviewRepository.deleteAll();

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "review/all")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        PagingResponseDTO<AdminReviewDTO> response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertTrue(response.content().isEmpty());
        assertTrue(response.empty());
        assertEquals(0, response.totalPages());

        Long cachingResult = cacheRedisTemplate.opsForValue().get(REVIEW_CACHING_KEY);
        assertEquals(0, cachingResult);
    }

    @Test
    @DisplayName(value = "전체 리뷰 목록 조회. 상품명 기반 검색")
    void getAllReviewListSearchProductName() throws Exception {
        AdminReviewPageDTO pageDTO = new AdminReviewPageDTO(null, null, 1);
        List<ProductReview> filterFixtureList = allReviewList.stream()
                .filter(v ->
                        v.getProduct().getProductName().contains(product.getProductName())
                )
                .toList();
        List<ProductReview> contentFixture = filterFixtureList.stream()
                .limit(pageDTO.amount())
                .toList();
        int contentSize = Math.min(contentFixture.size(), pageDTO.amount());
        int totalPages = PaginationUtils.getTotalPages(filterFixtureList.size(), pageDTO.amount());

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "review/all")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .param("keyword", product.getProductName())
                        .param("searchType", "product"))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        PagingResponseDTO<AdminReviewDTO> response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertFalse(response.content().isEmpty());
        assertFalse(response.empty());
        assertEquals(contentSize, response.content().size());
        assertEquals(totalPages, response.totalPages());

        Long cachingResult = cacheRedisTemplate.opsForValue().get(REVIEW_CACHING_KEY);
        assertNull(cachingResult);
    }

    @Test
    @DisplayName(value = "전체 리뷰 목록 조회. 사용자 이름 또는 닉네임 기반 검색")
    void getAllReviewListSearchUser() throws Exception {
        AdminReviewPageDTO pageDTO = new AdminReviewPageDTO(null, null, 1);
        List<ProductReview> filterFixtureList = allReviewList.stream()
                .filter(v ->
                        v.getMember().getNickname().contains(member.getNickname()) ||
                                v.getMember().getUserName().contains(member.getNickname())
                )
                .toList();
        List<ProductReview> contentFixture = filterFixtureList.stream()
                .limit(pageDTO.amount())
                .toList();
        int contentSize = Math.min(contentFixture.size(), pageDTO.amount());
        int totalPages = PaginationUtils.getTotalPages(filterFixtureList.size(), pageDTO.amount());

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "review/all")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .param("keyword", member.getNickname())
                        .param("searchType", "user"))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        PagingResponseDTO<AdminReviewDTO> response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertFalse(response.content().isEmpty());
        assertFalse(response.empty());
        assertEquals(contentSize, response.content().size());
        assertEquals(totalPages, response.totalPages());

        Long cachingResult = cacheRedisTemplate.opsForValue().get(REVIEW_CACHING_KEY);
        assertNull(cachingResult);
    }

    @Test
    @DisplayName(value = "답변 완료된 리뷰 상세 조회")
    void getAnswerReviewDetail() throws Exception {
        ProductReview fixture = answerCompleteReviewList.get(0);
        ProductReviewReply replyFixture = reviewReplyList.stream()
                .filter(v ->
                        v.getProductReview().getId().equals(fixture.getId())
                )
                .findFirst()
                .get();

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "review/detail/" + fixture.getId())
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        AdminReviewDetailDTO response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(fixture.getId(), response.reviewId());
        assertEquals(fixture.getProduct().getProductName(), response.productName());
        assertEquals(fixture.getProductOption().getSize(), response.size());
        assertEquals(fixture.getProductOption().getColor(), response.color());
        assertEquals(fixture.getMember().getNickname(), response.writer());
        assertEquals(fixture.getReviewContent(), response.content());
        assertEquals(replyFixture.getReplyContent(), response.replyContent());
    }

    @Test
    @DisplayName(value = "미답변의 리뷰 상세 조회")
    void getNewReviewDetail() throws Exception {
        ProductReview fixture = newReviewList.get(0);

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "review/detail/" + fixture.getId())
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        AdminReviewDetailDTO response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(fixture.getId(), response.reviewId());
        assertEquals(fixture.getProduct().getProductName(), response.productName());
        assertEquals(fixture.getProductOption().getSize(), response.size());
        assertEquals(fixture.getProductOption().getColor(), response.color());
        assertEquals(fixture.getMember().getNickname(), response.writer());
        assertEquals(fixture.getReviewContent(), response.content());
        assertNull(response.replyUpdatedAt());
        assertNull(response.replyContent());
    }

    @Test
    @DisplayName(value = "리뷰 상세 조회. 리뷰 아이디가 잘못된 경우")
    void getReviewDetailWrongId() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "review/detail/0")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
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

    @Test
    @DisplayName(value = "리뷰 답변 작성")
    void postReviewReply() throws Exception {
        ProductReview fixture = newReviewList.get(0);
        AdminReviewReplyRequestDTO insertDTO = new AdminReviewReplyRequestDTO(fixture.getId(), "test insert review reply content");
        String requestDTO = om.writeValueAsString(insertDTO);

        mockMvc.perform(post(URL_PREFIX + "review/reply")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestDTO))
                .andExpect(status().isNoContent())
                .andReturn();

        ProductReview patchData = productReviewRepository.findById(fixture.getId()).orElse(null);
        assertNotNull(patchData);
        assertTrue(patchData.isStatus());

        ProductReviewReply saveReply = productReviewReplyRepository.findByReviewId(fixture.getId());
        assertNotNull(saveReply);
        assertEquals(insertDTO.content(), saveReply.getReplyContent());

        await()
                .atMost(20, TimeUnit.SECONDS)
                .pollInterval(200, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    List<Notification> notificationList = notificationRepository.findAll();
                    assertFalse(notificationList.isEmpty());
                    assertEquals(1, notificationList.size());

                    Notification notification = notificationList.get(0);

                    assertEquals(fixture.getMember().getUserId(), notification.getMember().getUserId());
                    assertEquals(NotificationType.REVIEW_REPLY.getTitle(), notification.getTitle());
                });
    }

    @Test
    @DisplayName(value = "리뷰 답변 작성. 리뷰 아이디가 잘못된 경우")
    void postReviewReplyWrongId() throws Exception {
        AdminReviewReplyRequestDTO insertDTO = new AdminReviewReplyRequestDTO(0L, "test insert review reply content");
        String requestDTO = om.writeValueAsString(insertDTO);

        MvcResult result = mockMvc.perform(post(URL_PREFIX + "review/reply")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
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

    @Test
    @DisplayName(value = "리뷰 답변 작성. 이미 답변이 작성된 리뷰인 경우")
    void postReviewReplyAlreadyExist() throws Exception {
        ProductReview fixture = answerCompleteReviewList.get(0);
        AdminReviewReplyRequestDTO insertDTO = new AdminReviewReplyRequestDTO(fixture.getId(), "test insert review reply content");
        String requestDTO = om.writeValueAsString(insertDTO);

        MvcResult result = mockMvc.perform(post(URL_PREFIX + "review/reply")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
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

        ProductReview patchData = productReviewRepository.findById(fixture.getId()).orElse(null);
        assertNotNull(patchData);
        assertTrue(patchData.isStatus());

        ProductReviewReply originReply = productReviewReplyRepository.findByReviewId(fixture.getId());
        assertNotNull(originReply);
        assertNotEquals(insertDTO.content(), originReply.getReplyContent());
    }
}
