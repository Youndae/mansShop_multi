package com.example.moduletest.admin.productQnA.usecase;

import com.example.modulecommon.fixture.ClassificationFixture;
import com.example.modulecommon.fixture.MemberAndAuthFixture;
import com.example.modulecommon.fixture.ProductFixture;
import com.example.modulecommon.fixture.ProductQnAFixture;
import com.example.modulecommon.model.dto.MemberAndAuthFixtureDTO;
import com.example.modulecommon.model.dto.admin.qna.out.AdminQnAListResponseDTO;
import com.example.modulecommon.model.dto.page.AdminQnAPageDTO;
import com.example.modulecommon.model.dto.response.PagingListDTO;
import com.example.modulecommon.model.entity.*;
import com.example.modulecommon.model.enumuration.AdminListType;
import com.example.modulecommon.utils.TestPaginationUtils;
import com.example.moduleproduct.repository.classification.ClassificationRepository;
import com.example.moduleproduct.repository.product.ProductRepository;
import com.example.moduleproduct.repository.productQnA.ProductQnARepository;
import com.example.moduleproduct.repository.productQnAReply.ProductQnAReplyRepository;
import com.example.moduleproduct.usecase.admin.productQnA.AdminProductQnAReadUseCase;
import com.example.moduletest.ModuleTestApplication;
import com.example.moduleuser.repository.AuthRepository;
import com.example.moduleuser.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = ModuleTestApplication.class)
@EntityScan(basePackages = "com.example.modulecommon")
@EnableJpaRepositories(basePackages = "com.example")
@ComponentScan(basePackages = {"com.example.moduleconfig", "com.example.modulecommon"})
@ActiveProfiles("test")
@Transactional
public class AdminProductQnAReadUseCaseIT {

    @Autowired
    private AdminProductQnAReadUseCase adminProductQnAReadUseCase;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ClassificationRepository classificationRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private AuthRepository authRepository;

    @Autowired
    private ProductQnARepository productQnARepository;

    @Autowired
    private ProductQnAReplyRepository productQnAReplyRepository;

    private List<Product> productList;

    private List<Member> memberList;

    private Member admin;

    private List<ProductQnA> newProductQnAList;

    private List<ProductQnA> allProductQnA;

    private List<ProductQnAReply> productQnAReplyList;

    private static String ALL_LIST_TYPE = AdminListType.ALL.getType();

    private static String NEW_LIST_TYPE = AdminListType.NEW.getType();

    @BeforeEach
    void init() {
        MemberAndAuthFixtureDTO memberAndAuthFixture = MemberAndAuthFixture.createDefaultMember(30);
        memberList = memberAndAuthFixture.memberList();
        memberRepository.saveAll(memberList);
        authRepository.saveAll(memberAndAuthFixture.authList());
        List<Classification> classificationFixture = ClassificationFixture.createClassifications();
        classificationRepository.saveAll(classificationFixture);

        productList = ProductFixture.createProductFixtureList(10, classificationFixture.get(0));
        productRepository.saveAll(productList);

        MemberAndAuthFixtureDTO adminFixture = MemberAndAuthFixture.createAdmin();
        admin = adminFixture.memberList().get(0);
        memberRepository.save(admin);
        authRepository.save(adminFixture.authList().get(0));

        newProductQnAList = ProductQnAFixture.createDefaultProductQnA(memberList, productList);
        List<Member> completeQnAMemberFixture = memberList.stream().limit(5).toList();
        List<Product> completeQnAProductFixture = productList.stream().limit(5).toList();
        List<ProductQnA> completeProductQnA = ProductQnAFixture.createProductQnACompletedAnswer(completeQnAMemberFixture, completeQnAProductFixture);
        allProductQnA = new ArrayList<>(newProductQnAList);
        allProductQnA.addAll(completeProductQnA);
        productQnARepository.saveAll(allProductQnA);

        productQnAReplyList = ProductQnAFixture.createDefaultProductQnaReply(admin, completeProductQnA);
        productQnAReplyRepository.saveAll(productQnAReplyList);
    }

    @Test
    @DisplayName(value = "모든 상품 문의 목록 조회.")
    void getAllProductQnAList() {
        AdminQnAPageDTO pageDTO = new AdminQnAPageDTO(ALL_LIST_TYPE, 1);
        int totalPages = TestPaginationUtils.getTotalPages(allProductQnA.size(), pageDTO.amount());
        PagingListDTO<AdminQnAListResponseDTO> result = assertDoesNotThrow(() -> adminProductQnAReadUseCase.getProductQnAList(pageDTO, allProductQnA.size()));

        assertNotNull(result);
        assertFalse(result.content().isEmpty());
        assertFalse(result.pagingData().isEmpty());
        assertEquals(pageDTO.amount(), result.content().size());
        assertEquals(allProductQnA.size(), result.pagingData().getTotalElements());
        assertEquals(totalPages, result.pagingData().getTotalPages());
    }

    @Test
    @DisplayName(value = "모든 상품 문의 목록 조회. 데이터가 없는 경우.")
    void getAllProductQnAListEmpty() {
        productQnAReplyRepository.deleteAll();
        productQnARepository.deleteAll();
        AdminQnAPageDTO pageDTO = new AdminQnAPageDTO(null, ALL_LIST_TYPE, 1);

        PagingListDTO<AdminQnAListResponseDTO> result = assertDoesNotThrow(() -> adminProductQnAReadUseCase.getProductQnAList(pageDTO, 0L));

        assertNotNull(result);
        assertTrue(result.content().isEmpty());
        assertTrue(result.pagingData().isEmpty());
        assertEquals(0, result.pagingData().getTotalElements());
        assertEquals(0, result.pagingData().getTotalPages());
    }

    @Test
    @DisplayName(value = "모든 상품 문의 목록 조회. 검색")
    void getAllProductQnAListSearch() {
        Member searchMemberFixture = memberList.get(0);
        AdminQnAPageDTO pageDTO = new AdminQnAPageDTO(searchMemberFixture.getUserId(), ALL_LIST_TYPE, 1);
        int totalElements = allProductQnA.stream()
                .filter(v ->
                        v.getMember().getUserId().equals(searchMemberFixture.getUserId()))
                .toList()
                .size();
        int totalPages = TestPaginationUtils.getTotalPages(totalElements, pageDTO.amount());
        int resultContentSize = Math.min(totalElements, pageDTO.amount());
        PagingListDTO<AdminQnAListResponseDTO> result = assertDoesNotThrow(() -> adminProductQnAReadUseCase.getProductQnAList(pageDTO, 0L));

        assertNotNull(result);
        assertFalse(result.content().isEmpty());
        assertFalse(result.pagingData().isEmpty());
        assertEquals(resultContentSize, result.content().size());
        assertEquals(totalElements, result.pagingData().getTotalElements());
        assertEquals(totalPages, result.pagingData().getTotalPages());
    }

    @Test
    @DisplayName(value = "모든 상품 문의 목록 조회. 검색. 결과가 없는 경우")
    void getAllProductQnAListSearchEmpty() {
        AdminQnAPageDTO pageDTO = new AdminQnAPageDTO("NoneUser", ALL_LIST_TYPE, 1);
        PagingListDTO<AdminQnAListResponseDTO> result = assertDoesNotThrow(() -> adminProductQnAReadUseCase.getProductQnAList(pageDTO, 0L));

        assertNotNull(result);
        assertTrue(result.content().isEmpty());
        assertTrue(result.pagingData().isEmpty());
        assertEquals(0, result.pagingData().getTotalElements());
        assertEquals(0, result.pagingData().getTotalPages());
    }

    @Test
    @DisplayName(value = "새로운 상품 문의 목록 조회.")
    void getNewProductQnAList() {
        AdminQnAPageDTO pageDTO = new AdminQnAPageDTO(null, NEW_LIST_TYPE, 1);
        int totalPages = TestPaginationUtils.getTotalPages(newProductQnAList.size(), pageDTO.amount());
        PagingListDTO<AdminQnAListResponseDTO> result = assertDoesNotThrow(() -> adminProductQnAReadUseCase.getProductQnAList(pageDTO, 0L));

        assertNotNull(result);
        assertFalse(result.content().isEmpty());
        assertFalse(result.pagingData().isEmpty());
        assertEquals(pageDTO.amount(), result.content().size());
        assertEquals(newProductQnAList.size(), result.pagingData().getTotalElements());
        assertEquals(totalPages, result.pagingData().getTotalPages());
    }

    @Test
    @DisplayName(value = "미처리 상품 문의 목록 조회. 검색")
    void getNewProductQnAListSearch() {
        Member searchMemberFixture = memberList.get(0);
        AdminQnAPageDTO pageDTO = new AdminQnAPageDTO(searchMemberFixture.getUserId(), NEW_LIST_TYPE, 1);
        int totalElements = newProductQnAList.stream()
                .filter(v ->
                        v.getMember().getUserId().equals(searchMemberFixture.getUserId()))
                .toList()
                .size();
        int totalPages = TestPaginationUtils.getTotalPages(totalElements, pageDTO.amount());
        int resultContentSize = Math.min(totalElements, pageDTO.amount());
        PagingListDTO<AdminQnAListResponseDTO> result = assertDoesNotThrow(() -> adminProductQnAReadUseCase.getProductQnAList(pageDTO, 0L));

        assertNotNull(result);
        assertFalse(result.content().isEmpty());
        assertFalse(result.pagingData().isEmpty());
        assertEquals(resultContentSize, result.content().size());
        assertEquals(totalElements, result.pagingData().getTotalElements());
        assertEquals(totalPages, result.pagingData().getTotalPages());
    }
}
