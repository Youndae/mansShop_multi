package com.example.moduletest.memberQnA.usecase;

import com.example.modulecommon.customException.CustomAccessDeniedException;
import com.example.modulecommon.fixture.MemberAndAuthFixture;
import com.example.modulecommon.fixture.MemberQnAFixture;
import com.example.modulecommon.fixture.QnAClassificationFixture;
import com.example.modulecommon.model.dto.MemberAndAuthFixtureDTO;
import com.example.modulecommon.model.dto.qna.in.QnAReplyInsertDTO;
import com.example.modulecommon.model.dto.qna.in.QnAReplyPatchDTO;
import com.example.modulecommon.model.dto.qna.out.QnADetailReplyDTO;
import com.example.modulecommon.model.entity.*;
import com.example.modulecommon.model.enumuration.Result;
import com.example.modulemypage.model.dto.memberQnA.in.MemberQnAInsertDTO;
import com.example.modulemypage.model.dto.memberQnA.in.MemberQnAModifyDTO;
import com.example.modulemypage.repository.MemberQnAReplyRepository;
import com.example.modulemypage.repository.MemberQnARepository;
import com.example.modulemypage.repository.QnAClassificationRepository;
import com.example.modulemypage.usecase.MemberQnAWriteUseCase;
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
public class MemberQnAWriteUseCaseIT {

    @Autowired
    private MemberQnAWriteUseCase memberQnAWriteUseCase;

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

    @Test
    @DisplayName(value = "회원 문의 작성")
    void postMemberQnA() {
        QnAClassification qnAClassification = qnAClassificationList.get(0);
        MemberQnAInsertDTO insertDTO = new MemberQnAInsertDTO(
                "testInsertTitle",
                "testInsertContent",
                qnAClassification.getId()
        );

        Long result = assertDoesNotThrow(() -> memberQnAWriteUseCase.postMemberQnA(insertDTO, member.getUserId()));

        assertNotNull(result);

        MemberQnA saveData = memberQnARepository.findById(result).orElse(null);

        assertNotNull(saveData);
        assertEquals(member.getUserId(), saveData.getMember().getUserId());
        assertEquals(insertDTO.title(), saveData.getMemberQnATitle());
        assertEquals(insertDTO.content(), saveData.getMemberQnAContent());
        assertEquals(qnAClassification.getId(), saveData.getQnAClassification().getId());
    }

    @Test
    @DisplayName(value = "회원 문의 작성. 문의 분류 아이디가 잘못된 경우")
    void postMemberQnAClassificationNotFound() {
        MemberQnAInsertDTO insertDTO = new MemberQnAInsertDTO(
                "testInsertTitle",
                "testInsertContent",
                0L
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> memberQnAWriteUseCase.postMemberQnA(insertDTO, member.getUserId())
        );
    }

    @Test
    @DisplayName(value = "회원 문의 답변 작성")
    void postMemberQnAReply() {
        MemberQnA fixture = answerMemberQnAList.stream().filter(MemberQnA::isMemberQnAStat).toList().get(0);
        int size = memberQnAReplyList.stream().filter(v -> v.getMemberQnA().getId().equals(fixture.getId())).toList().size();
        String userId = fixture.getMember().getUserId();
        QnAReplyInsertDTO replyInsertDTO = new QnAReplyInsertDTO(fixture.getId(), "test reply content");

        String result = assertDoesNotThrow(() -> memberQnAWriteUseCase.postMemberQnAReply(replyInsertDTO, userId));

        assertNotNull(result);
        assertEquals(Result.OK.getResultKey(), result);

        MemberQnA patchData = memberQnARepository.findById(fixture.getId()).orElse(null);
        List<QnADetailReplyDTO> replyList = memberQnAReplyRepository.findAllByQnAId(fixture.getId());
        QnADetailReplyDTO saveReply = replyList.get(replyList.size() - 1);

        assertNotNull(patchData);
        assertFalse(patchData.isMemberQnAStat());
        assertEquals(size + 1, replyList.size());
        assertEquals(fixture.getMember().getNickname(), saveReply.writer());
        assertEquals(replyInsertDTO.content(), saveReply.replyContent());
    }

    @Test
    @DisplayName(value = "회원 문의 답변 작성. 관리자가 아닌데 문의 작성자와 답글 작성자가 일치하지 않는 경우")
    void postMemberQnAReplyWriterNotEquals() {
        MemberQnA fixture = answerMemberQnAList.stream()
                .filter(v ->
                        v.isMemberQnAStat() && v.getMember().getUserId().equals(member.getUserId())
                )
                .toList()
                .get(0);
        String userId = memberList.get(1).getUserId();
        QnAReplyInsertDTO replyInsertDTO = new QnAReplyInsertDTO(fixture.getId(), "test reply content");

        assertThrows(
                CustomAccessDeniedException.class,
                () -> memberQnAWriteUseCase.postMemberQnAReply(replyInsertDTO, userId)
        );

        MemberQnA patchData = memberQnARepository.findById(fixture.getId()).orElse(null);

        assertNotNull(patchData);
        assertTrue(patchData.isMemberQnAStat());
    }

    @Test
    @DisplayName(value = "회원 문의 답변 작성. 문의 아이디가 잘못 된 경우")
    void postMemberQnAReplyWrongId() {
        QnAReplyInsertDTO replyInsertDTO = new QnAReplyInsertDTO(0L, "test reply content");

        assertThrows(
                IllegalArgumentException.class,
                () -> memberQnAWriteUseCase.postMemberQnAReply(replyInsertDTO, member.getUserId())
        );
    }

    @Test
    @DisplayName(value = "회원 문의 답변 수정")
    void patchMemberQnAReply() {
        MemberQnAReply fixture = memberQnAReplyList.get(0);
        String userId = fixture.getMember().getUserId();
        QnAReplyPatchDTO replyDTO = new QnAReplyPatchDTO(fixture.getId(), "test patch reply Content");

        String result = assertDoesNotThrow(() -> memberQnAWriteUseCase.patchMemberQnAReply(replyDTO, userId));

        assertNotNull(result);
        assertEquals(Result.OK.getResultKey(), result);

        MemberQnAReply patchData = memberQnAReplyRepository.findById(fixture.getId()).orElse(null);

        assertNotNull(patchData);
        assertEquals(replyDTO.content(), patchData.getReplyContent());
    }

    @Test
    @DisplayName(value = "회원 문의 답변 수정. 답변 아이디가 잘못 된 경우")
    void patchMemberQnAReplyWrongId() {
        QnAReplyPatchDTO replyDTO = new QnAReplyPatchDTO(0L, "test patch reply Content");

        assertThrows(
                IllegalArgumentException.class,
                () -> memberQnAWriteUseCase.patchMemberQnAReply(replyDTO, member.getUserId())
        );
    }

    @Test
    @DisplayName(value = "회원 문의 답변 수정. 작성자가 일치하지 않는 경우")
    void patchMemberQnAReplyWriterNotEquals() {
        MemberQnAReply fixture = memberQnAReplyList.get(0);
        QnAReplyPatchDTO replyDTO = new QnAReplyPatchDTO(fixture.getId(), "test patch reply Content");

        assertThrows(
                CustomAccessDeniedException.class,
                () -> memberQnAWriteUseCase.patchMemberQnAReply(replyDTO, "WrongUserId")
        );
    }

    @Test
    @DisplayName(value = "회원 문의 수정")
    void patchMemberQnA() {
        MemberQnA fixture = allMemberQnAList.get(0);
        String userId = fixture.getMember().getUserId();
        long classificationId = qnAClassificationList.get(1).getId();
        MemberQnAModifyDTO modifyDTO = new MemberQnAModifyDTO(
                fixture.getId(),
                "test modify title",
                "test modify content",
                classificationId
        );

        String result = assertDoesNotThrow(() -> memberQnAWriteUseCase.patchMemberQnA(modifyDTO, userId));

        assertNotNull(result);
        assertEquals(Result.OK.getResultKey(), result);

        MemberQnA patchData = memberQnARepository.findById(fixture.getId()).orElse(null);

        assertNotNull(patchData);
        assertEquals(modifyDTO.title(), patchData.getMemberQnATitle());
        assertEquals(modifyDTO.content(), patchData.getMemberQnAContent());
        assertEquals(modifyDTO.classificationId(), patchData.getQnAClassification().getId());
    }

    @Test
    @DisplayName(value = "회원 문의 수정. 회원 문의 아이디가 잘못 된 경우")
    void patchMemberQnAWrongId() {
        long classificationId = qnAClassificationList.get(1).getId();
        MemberQnAModifyDTO modifyDTO = new MemberQnAModifyDTO(
                0L,
                "test modify title",
                "test modify content",
                classificationId
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> memberQnAWriteUseCase.patchMemberQnA(modifyDTO, member.getUserId())
        );
    }

    @Test
    @DisplayName(value = "회원 문의 수정. 작성자가 일치하지 않는 경우")
    void patchMemberQnAWriterNotEquals() {
        MemberQnA fixture = allMemberQnAList.get(0);
        String userId = memberList.get(1).getUserId();
        long classificationId = qnAClassificationList.get(1).getId();
        MemberQnAModifyDTO modifyDTO = new MemberQnAModifyDTO(
                fixture.getId(),
                "test modify title",
                "test modify content",
                classificationId
        );

        assertThrows(
                CustomAccessDeniedException.class,
                () -> memberQnAWriteUseCase.patchMemberQnA(modifyDTO, userId)
        );
    }

    @Test
    @DisplayName(value = "회원 문의 삭제")
    void deleteMemberQnA() {
        MemberQnA fixture = allMemberQnAList.get(0);
        String userId = fixture.getMember().getUserId();

        String result = assertDoesNotThrow(() -> memberQnAWriteUseCase.deleteMemberQnA(fixture.getId(), userId));

        assertNotNull(result);
        assertEquals(Result.OK.getResultKey(), result);

        MemberQnA deleteData = memberQnARepository.findById(fixture.getId()).orElse(null);
        assertNull(deleteData);
    }

    @Test
    @DisplayName(value = "회원 문의 삭제. 잘못 된 아이디인 경우")
    void deleteMemberQnAWrongId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> memberQnAWriteUseCase.deleteMemberQnA(0L, member.getUserId())
        );
    }

    @Test
    @DisplayName(value = "회원 문의 삭제. 작성자가 일치하지 않는 경우")
    void deleteMemberQnAWriterNotEquals() {
        MemberQnA fixture = allMemberQnAList.get(0);
        String userId = memberList.get(1).getUserId();

        assertThrows(
                CustomAccessDeniedException.class,
                () -> memberQnAWriteUseCase.deleteMemberQnA(fixture.getId(), userId)
        );

        MemberQnA deleteData = memberQnARepository.findById(fixture.getId()).orElse(null);

        assertNotNull(deleteData);
    }
}
