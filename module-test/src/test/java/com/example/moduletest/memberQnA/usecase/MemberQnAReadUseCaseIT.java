package com.example.moduletest.memberQnA.usecase;

import com.example.modulecommon.customException.CustomAccessDeniedException;
import com.example.modulecommon.customException.CustomNotFoundException;
import com.example.modulecommon.fixture.MemberAndAuthFixture;
import com.example.modulecommon.fixture.MemberQnAFixture;
import com.example.modulecommon.fixture.QnAClassificationFixture;
import com.example.modulecommon.model.dto.MemberAndAuthFixtureDTO;
import com.example.modulecommon.model.dto.page.MyPagePageDTO;
import com.example.modulecommon.model.entity.*;
import com.example.modulecommon.utils.TestPaginationUtils;
import com.example.modulemypage.model.dto.memberQnA.out.MemberQnADetailResponseDTO;
import com.example.modulemypage.model.dto.memberQnA.out.MemberQnAListDTO;
import com.example.modulemypage.model.dto.memberQnA.out.MemberQnAModifyDataDTO;
import com.example.modulemypage.model.dto.memberQnA.out.QnAClassificationDTO;
import com.example.modulemypage.repository.MemberQnAReplyRepository;
import com.example.modulemypage.repository.MemberQnARepository;
import com.example.modulemypage.repository.QnAClassificationRepository;
import com.example.modulemypage.usecase.MemberQnAReadUseCase;
import com.example.moduletest.ModuleTestApplication;
import com.example.moduleuser.repository.AuthRepository;
import com.example.moduleuser.repository.MemberRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.Page;
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
public class MemberQnAReadUseCaseIT {

    @Autowired
    private MemberQnAReadUseCase memberQnAReadUseCase;

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

    @Autowired
    private EntityManager em;

    private List<Member> memberList;

    private Member member;

    private List<QnAClassification> qnAClassificationList;

    private List<MemberQnA> allMemberQnAList;

    private List<MemberQnA> answerMemberQnAList;

    private List<MemberQnAReply> memberQnAReplyList;

    @BeforeEach
    void init() {
        MemberAndAuthFixtureDTO memberAndAuthFixture = MemberAndAuthFixture.createDefaultMember(2);
        MemberAndAuthFixtureDTO adminFixture = MemberAndAuthFixture.createAdmin();
        Member admin = adminFixture.memberList().get(0);
        memberList = memberAndAuthFixture.memberList();
        member = memberList.get(0);
        List<Member> saveMemberList = new ArrayList<>(memberList);
        saveMemberList.addAll(adminFixture.memberList());
        memberRepository.saveAll(saveMemberList);
        List<Auth> saveAuthList = new ArrayList<>(memberAndAuthFixture.authList());
        saveAuthList.addAll(adminFixture.authList());
        authRepository.saveAll(saveAuthList);

        qnAClassificationList = QnAClassificationFixture.createQnAClassificationList();
        qnAClassificationRepository.saveAll(qnAClassificationList);

        answerMemberQnAList = MemberQnAFixture.createMemberQnACompletedAnswer(qnAClassificationList, memberList);
        memberQnAReplyList = MemberQnAFixture.createMemberQnAReply(answerMemberQnAList, admin);
        List<MemberQnA> newMemberQnAList = MemberQnAFixture.createDefaultMemberQnA(qnAClassificationList, memberList);
        allMemberQnAList = new ArrayList<>(answerMemberQnAList);
        allMemberQnAList.addAll(newMemberQnAList);

        memberQnARepository.saveAll(allMemberQnAList);
        memberQnAReplyRepository.saveAll(memberQnAReplyList);

        em.flush();
        em.clear();
    }

    private List<MemberQnA> getMemberQnAList(Member member, int limit) {
        int size = getLimitSize(allMemberQnAList.size(), limit);

        return allMemberQnAList.stream()
                .filter(v -> userIdEquals(v.getMember(), member))
                .limit(size)
                .toList();
    }

    private int getLimitSize(int listSize, int limit) {
        return limit == 0 ? listSize : limit;
    }

    private boolean userIdEquals(Member listMember, Member member) {
        return listMember.getUserId().equals(member.getUserId());
    }

    @Test
    @DisplayName(value = "회원 문의 목록 조회")
    void getMemberQnAList() {
        List<MemberQnA> fixtureList = getMemberQnAList(member, 0);
        MyPagePageDTO pageDTO = new MyPagePageDTO(1);
        int totalPages = TestPaginationUtils.getTotalPages(fixtureList.size(), pageDTO.amount());
        int contentElements = Math.min(fixtureList.size(), pageDTO.amount());

        Page<MemberQnAListDTO> result = assertDoesNotThrow(() -> memberQnAReadUseCase.getMemberQnAList(pageDTO, member.getUserId()));

        assertNotNull(result);
        assertFalse(result.getContent().isEmpty());
        assertFalse(result.isEmpty());
        assertEquals(contentElements, result.getContent().size());
        assertEquals(totalPages, result.getTotalPages());
        assertEquals(fixtureList.size(), result.getTotalElements());
    }

    @Test
    @DisplayName(value = "회원 문의 목록 조회. 데이터가 없는 경우")
    void getMemberQnAListEmpty() {
        memberQnARepository.deleteAll();
        MyPagePageDTO pageDTO = new MyPagePageDTO(1);

        Page<MemberQnAListDTO> result = assertDoesNotThrow(() -> memberQnAReadUseCase.getMemberQnAList(pageDTO, member.getUserId()));

        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalPages());
        assertEquals(0, result.getTotalElements());
    }

    @Test
    @DisplayName(value = "회원 문의 상세 조회")
    void getMemberQnADetail() {
        MemberQnA fixture = answerMemberQnAList.get(0);
        List<MemberQnAReply> replyFixtureList = memberQnAReplyList.stream()
                .filter(v -> v.getMemberQnA().getId().equals(fixture.getId()))
                .toList();
        Member fixtureMember = fixture.getMember();
        String nickname = fixtureMember.getNickname() == null ? fixtureMember.getUserName() : fixtureMember.getNickname();
        MemberQnADetailResponseDTO result = assertDoesNotThrow(() -> memberQnAReadUseCase.getMemberQnADetail(fixture.getId(), nickname));

        assertNotNull(result);
        assertEquals(fixture.getId(), result.memberQnAId());
        assertEquals(fixture.getQnAClassification().getQnaClassificationName(), result.qnaClassification());
        assertEquals(fixture.getMemberQnATitle(), result.qnaTitle());
        assertEquals(fixture.getMember().getNickname(), result.writer());
        assertEquals(fixture.getMemberQnAContent(), result.qnaContent());
        assertEquals(fixture.getUpdatedAt().toLocalDate(), result.updatedAt());
        assertEquals(fixture.isMemberQnAStat(), result.memberQnAStat());
        assertEquals(replyFixtureList.size(), result.replyList().size());
    }

    @Test
    @DisplayName(value = "회원 문의 상세 조회. 작성자가 일치하지 않는 경우")
    void getMemberQnADetailWriterNotEquals() {
        MemberQnA fixture = answerMemberQnAList.get(0);

        assertThrows(
                CustomAccessDeniedException.class,
                () -> memberQnAReadUseCase.getMemberQnADetail(fixture.getId(), "WrongUserId")
        );
    }

    @Test
    @DisplayName(value = "회원 문의 상세 조회. 회원 문의 아이디가 잘못된 경우")
    void getMemberQnADetailWrongId() {
        assertThrows(
                CustomNotFoundException.class,
                () -> memberQnAReadUseCase.getMemberQnADetail(0L, member.getUserId())
        );
    }

    @Test
    @DisplayName(value = "회원 문의 수정을 위한 상세 조회")
    void getModifyData() {
        MemberQnA fixture = allMemberQnAList.get(0);

        MemberQnAModifyDataDTO result = assertDoesNotThrow(() -> memberQnAReadUseCase.getModifyData(fixture.getId(), fixture.getMember().getUserId()));

        assertNotNull(result);
        assertEquals(fixture.getId(), result.qnaId());
        assertEquals(fixture.getMemberQnATitle(), result.qnaTitle());
        assertEquals(fixture.getMemberQnAContent(), result.qnaContent());
        assertEquals(fixture.getQnAClassification().getId(), result.qnaClassificationId());
        assertEquals(qnAClassificationList.size(), result.classificationList().size());
    }

    @Test
    @DisplayName(value = "회원 문의 수정을 위한 상세 조회. 회원 문의 아이디가 잘못 된 경우")
    void getModifyDataWrongId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> memberQnAReadUseCase.getModifyData(0L, member.getUserId())
        );
    }

    @Test
    @DisplayName(value = "회원 문의 분류 목록 조회")
    void getQnAClassification() {
        List<QnAClassificationDTO> result = assertDoesNotThrow(() -> memberQnAReadUseCase.getQnAClassification());

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(qnAClassificationList.size(), result.size());
        for(int i = 0; i < qnAClassificationList.size(); i++) {
            QnAClassification classification = qnAClassificationList.get(i);
            QnAClassificationDTO resultDTO = result.get(i);

            assertEquals(classification.getId(), resultDTO.id());
            assertEquals(classification.getQnaClassificationName(), resultDTO.name());
        }
    }

    @Test
    @DisplayName(value = "회원 문의 분류 목록 조회. 데이터가 없는 경우")
    void getQnAClassificationEmpty() {
        qnAClassificationRepository.deleteAll();

        List<QnAClassificationDTO> result = assertDoesNotThrow(() -> memberQnAReadUseCase.getQnAClassification());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
