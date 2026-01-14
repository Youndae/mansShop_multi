package com.example.moduletest.admin.memberQnA.usecase;

import com.example.modulecommon.fixture.MemberAndAuthFixture;
import com.example.modulecommon.fixture.MemberQnAFixture;
import com.example.modulecommon.fixture.QnAClassificationFixture;
import com.example.modulecommon.model.dto.MemberAndAuthFixtureDTO;
import com.example.modulecommon.model.dto.admin.qna.out.AdminQnAListResponseDTO;
import com.example.modulecommon.model.dto.page.AdminQnAPageDTO;
import com.example.modulecommon.model.dto.response.PagingListDTO;
import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.entity.MemberQnA;
import com.example.modulecommon.model.entity.MemberQnAReply;
import com.example.modulecommon.model.entity.QnAClassification;
import com.example.modulecommon.model.enumuration.AdminListType;
import com.example.modulecommon.utils.TestPaginationUtils;
import com.example.modulemypage.repository.MemberQnAReplyRepository;
import com.example.modulemypage.repository.MemberQnARepository;
import com.example.modulemypage.repository.QnAClassificationRepository;
import com.example.modulemypage.usecase.admin.AdminMemberQnAReadUseCase;
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
public class AdminMemberQnAReadUseCaseIT {

    @Autowired
    private AdminMemberQnAReadUseCase adminMemberQnAReadUseCase;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private AuthRepository authRepository;

    @Autowired
    private QnAClassificationRepository qnAClassificationRepository;

    @Autowired
    private MemberQnARepository memberQnARepository;

    @Autowired
    private MemberQnAReplyRepository memberQnAReplyRepository;

    private List<Member> memberList;

    private Member admin;

    private List<QnAClassification> qnAClassificationList;

    private List<MemberQnA> newMemberQnAList;

    private List<MemberQnA> allMemberQnA;

    private static String ALL_LIST_TYPE = AdminListType.ALL.getType();

    private static String NEW_LIST_TYPE = AdminListType.NEW.getType();

    @BeforeEach
    void init() {
        MemberAndAuthFixtureDTO memberAndAuthFixture = MemberAndAuthFixture.createDefaultMember(30);
        memberList = memberAndAuthFixture.memberList();
        memberRepository.saveAll(memberList);
        authRepository.saveAll(memberAndAuthFixture.authList());

        qnAClassificationList = QnAClassificationFixture.createQnAClassificationList();
        qnAClassificationRepository.saveAll(qnAClassificationList);

        MemberAndAuthFixtureDTO adminFixture = MemberAndAuthFixture.createAdmin();
        admin = adminFixture.memberList().get(0);
        memberRepository.save(admin);
        authRepository.save(adminFixture.authList().get(0));

        List<Member> completeQnAMemberFixture = memberList.stream().limit(5).toList();

        newMemberQnAList = MemberQnAFixture.createDefaultMemberQnA(qnAClassificationList, memberList);
        List<MemberQnA> completeMemberQnA = MemberQnAFixture.createMemberQnACompletedAnswer(qnAClassificationList, completeQnAMemberFixture);
        allMemberQnA = new ArrayList<>(newMemberQnAList);
        allMemberQnA.addAll(completeMemberQnA);
        memberQnARepository.saveAll(allMemberQnA);

        List<MemberQnAReply> memberQnAReplyList = MemberQnAFixture.createMemberQnAReply(completeMemberQnA, admin);
        memberQnAReplyRepository.saveAll(memberQnAReplyList);
    }

    @Test
    @DisplayName(value = "전체 회원 문의 목록 조회")
    void getAllMemberQnAList() {
        AdminQnAPageDTO pageDTO = new AdminQnAPageDTO(ALL_LIST_TYPE, 1);
        int totalPages = TestPaginationUtils.getTotalPages(allMemberQnA.size(), pageDTO.amount());
        PagingListDTO<AdminQnAListResponseDTO> result = assertDoesNotThrow(() -> adminMemberQnAReadUseCase.getMemberQnAList(pageDTO, allMemberQnA.size()));

        assertNotNull(result);
        assertFalse(result.content().isEmpty());
        assertFalse(result.pagingData().isEmpty());
        assertEquals(pageDTO.amount(), result.content().size());
        assertEquals(allMemberQnA.size(), result.pagingData().getTotalElements());
        assertEquals(totalPages, result.pagingData().getTotalPages());
    }

    @Test
    @DisplayName(value = "전체 회원 문의 목록 조회. 데이터가 없는 경우")
    void getAllMemberQnAListEmpty() {
        memberQnAReplyRepository.deleteAll();
        memberQnARepository.deleteAll();
        AdminQnAPageDTO pageDTO = new AdminQnAPageDTO(ALL_LIST_TYPE, 1);

        PagingListDTO<AdminQnAListResponseDTO> result = assertDoesNotThrow(() -> adminMemberQnAReadUseCase.getMemberQnAList(pageDTO, 0L));

        assertNotNull(result);
        assertTrue(result.content().isEmpty());
        assertTrue(result.pagingData().isEmpty());
        assertEquals(0, result.pagingData().getTotalElements());
        assertEquals(0, result.pagingData().getTotalPages());
    }

    @Test
    @DisplayName(value = "모든 회원 문의 목록 조회. 검색")
    void getAllMemberQnAListSearch() {
        Member searchMemberFixture = memberList.get(0);
        AdminQnAPageDTO pageDTO = new AdminQnAPageDTO(searchMemberFixture.getUserId(), ALL_LIST_TYPE, 1);
        int totalElements = allMemberQnA.stream()
                .filter(v ->
                        v.getMember().getUserId().equals(searchMemberFixture.getUserId()))
                .toList()
                .size();
        int totalPages = TestPaginationUtils.getTotalPages(totalElements, pageDTO.amount());
        int resultContentSize = Math.min(totalElements, pageDTO.amount());
        PagingListDTO<AdminQnAListResponseDTO> result = assertDoesNotThrow(() -> adminMemberQnAReadUseCase.getMemberQnAList(pageDTO, 0L));

        assertNotNull(result);
        assertFalse(result.content().isEmpty());
        assertFalse(result.pagingData().isEmpty());
        assertEquals(resultContentSize, result.content().size());
        assertEquals(totalElements, result.pagingData().getTotalElements());
        assertEquals(totalPages, result.pagingData().getTotalPages());
    }

    @Test
    @DisplayName(value = "모든 상품 문의 목록 조회. 검색. 결과가 없는 경우")
    void getAllMemberQnAListSearchEmpty() {
        AdminQnAPageDTO pageDTO = new AdminQnAPageDTO("NoneUser", ALL_LIST_TYPE, 1);
        PagingListDTO<AdminQnAListResponseDTO> result = assertDoesNotThrow(() -> adminMemberQnAReadUseCase.getMemberQnAList(pageDTO, 0L));

        assertNotNull(result);
        assertTrue(result.content().isEmpty());
        assertTrue(result.pagingData().isEmpty());
        assertEquals(0, result.pagingData().getTotalElements());
        assertEquals(0, result.pagingData().getTotalPages());
    }

    @Test
    @DisplayName(value = "미처리 회원 문의 목록 조회")
    void getNewMemberQnAList() {
        AdminQnAPageDTO pageDTO = new AdminQnAPageDTO(NEW_LIST_TYPE, 1);
        int totalPages = TestPaginationUtils.getTotalPages(newMemberQnAList.size(), pageDTO.amount());
        PagingListDTO<AdminQnAListResponseDTO> result = assertDoesNotThrow(() -> adminMemberQnAReadUseCase.getMemberQnAList(pageDTO, 0L));

        assertNotNull(result);
        assertFalse(result.content().isEmpty());
        assertFalse(result.pagingData().isEmpty());
        assertEquals(pageDTO.amount(), result.content().size());
        assertEquals(newMemberQnAList.size(), result.pagingData().getTotalElements());
        assertEquals(totalPages, result.pagingData().getTotalPages());
    }

    @Test
    @DisplayName(value = "미처리 회원 문의 목록 조회. 데이터가 없는 경우")
    void getEmptyMemberQnAListEmpty() {
        memberQnAReplyRepository.deleteAll();
        memberQnARepository.deleteAll();
        AdminQnAPageDTO pageDTO = new AdminQnAPageDTO(NEW_LIST_TYPE, 1);

        PagingListDTO<AdminQnAListResponseDTO> result = assertDoesNotThrow(() -> adminMemberQnAReadUseCase.getMemberQnAList(pageDTO, 0L));

        assertNotNull(result);
        assertTrue(result.content().isEmpty());
        assertTrue(result.pagingData().isEmpty());
        assertEquals(0, result.pagingData().getTotalElements());
        assertEquals(0, result.pagingData().getTotalPages());
    }

    @Test
    @DisplayName(value = "미처리 회원 문의 목록 조회. 검색")
    void getNewMemberQnAListSearch() {
        Member searchMemberFixture = memberList.get(0);
        AdminQnAPageDTO pageDTO = new AdminQnAPageDTO(searchMemberFixture.getUserId(), NEW_LIST_TYPE, 1);
        int totalElements = newMemberQnAList.stream()
                .filter(v ->
                        v.getMember().getUserId().equals(searchMemberFixture.getUserId()))
                .toList()
                .size();
        int totalPages = TestPaginationUtils.getTotalPages(totalElements, pageDTO.amount());
        int resultContentSize = Math.min(totalElements, pageDTO.amount());
        PagingListDTO<AdminQnAListResponseDTO> result = assertDoesNotThrow(() -> adminMemberQnAReadUseCase.getMemberQnAList(pageDTO, 0L));

        assertNotNull(result);
        assertFalse(result.content().isEmpty());
        assertFalse(result.pagingData().isEmpty());
        assertEquals(resultContentSize, result.content().size());
        assertEquals(totalElements, result.pagingData().getTotalElements());
        assertEquals(totalPages, result.pagingData().getTotalPages());
    }

    @Test
    @DisplayName(value = "미처리 회원 문의 목록 조회. 검색. 결과가 없는 경우")
    void getNewMemberQnAListSearchEmpty() {
        AdminQnAPageDTO pageDTO = new AdminQnAPageDTO("NoneUser", NEW_LIST_TYPE, 1);
        PagingListDTO<AdminQnAListResponseDTO> result = assertDoesNotThrow(() -> adminMemberQnAReadUseCase.getMemberQnAList(pageDTO, 0L));

        assertNotNull(result);
        assertTrue(result.content().isEmpty());
        assertTrue(result.pagingData().isEmpty());
        assertEquals(0, result.pagingData().getTotalElements());
        assertEquals(0, result.pagingData().getTotalPages());
    }
}
