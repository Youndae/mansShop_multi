package com.example.moduletest.admin.memberQnA.usecase;

import com.example.modulecommon.fixture.MemberAndAuthFixture;
import com.example.modulecommon.fixture.MemberQnAFixture;
import com.example.modulecommon.fixture.QnAClassificationFixture;
import com.example.modulecommon.model.dto.MemberAndAuthFixtureDTO;
import com.example.modulecommon.model.dto.qna.in.QnAReplyInsertDTO;
import com.example.modulecommon.model.dto.qna.out.QnADetailReplyDTO;
import com.example.modulecommon.model.entity.*;
import com.example.modulecommon.model.enumuration.Result;
import com.example.modulemypage.repository.MemberQnAReplyRepository;
import com.example.modulemypage.repository.MemberQnARepository;
import com.example.modulemypage.repository.QnAClassificationRepository;
import com.example.modulemypage.service.MemberQnAExternalService;
import com.example.modulemypage.usecase.admin.AdminMemberQnAWriteUseCase;
import com.example.modulenotification.repository.NotificationRepository;
import com.example.moduletest.ModuleTestApplication;
import com.example.moduleuser.repository.AuthRepository;
import com.example.moduleuser.repository.MemberRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = ModuleTestApplication.class)
@EntityScan(basePackages = "com.example.modulecommon")
@EnableJpaRepositories(basePackages = "com.example")
@ComponentScan(basePackages = {"com.example.moduleconfig", "com.example.modulecommon"})
@ActiveProfiles("test")
public class AdminMemberQnAWriteUseCaseIT {

    @Autowired
    private AdminMemberQnAWriteUseCase adminMemberQnAWriteUseCase;

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
    private NotificationRepository notificationRepository;

    @MockitoBean
    private MemberQnAExternalService memberQnAExternalService;

    private List<Member> memberList;

    private Member admin;

    private List<QnAClassification> qnAClassificationList;

    private List<MemberQnA> newMemberQnAList;

    private List<MemberQnA> allMemberQnA;

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
        authRepository.saveAll(adminFixture.authList());

        List<Member> completeQnAMemberFixture = memberList.stream().limit(5).toList();

        newMemberQnAList = MemberQnAFixture.createDefaultMemberQnA(qnAClassificationList, memberList);
        List<MemberQnA> completeMemberQnA = MemberQnAFixture.createMemberQnACompletedAnswer(qnAClassificationList, completeQnAMemberFixture);
        allMemberQnA = new ArrayList<>(newMemberQnAList);
        allMemberQnA.addAll(completeMemberQnA);
        memberQnARepository.saveAll(allMemberQnA);

        List<MemberQnAReply> memberQnAReplyList = MemberQnAFixture.createMemberQnAReply(completeMemberQnA, admin);
        memberQnAReplyRepository.saveAll(memberQnAReplyList);
    }

    @AfterEach
    void cleanUp() {
        notificationRepository.deleteAll();
        memberQnAReplyRepository.deleteAll();
        memberQnARepository.deleteAll();
        qnAClassificationRepository.deleteAll();
        authRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName(value = "회원 문의 답변 완료 처리")
    void patchMemberQnAComplete() {
        MemberQnA memberQnA = newMemberQnAList.get(0);

        assertDoesNotThrow(() -> adminMemberQnAWriteUseCase.patchMemberQnAComplete(memberQnA.getId()));

        MemberQnA patchMemberQnA = memberQnARepository.findById(memberQnA.getId()).orElse(null);

        assertNotNull(patchMemberQnA);
        assertTrue(patchMemberQnA.isMemberQnAStat());
    }

    @Test
    @DisplayName(value = "회원 문의 답변 완료 처리. 회원 문의 데이터가 없는 경우")
    void patchMemberQnACompleteNotFound() {
        assertThrows(
                IllegalArgumentException.class,
                () -> adminMemberQnAWriteUseCase.patchMemberQnAComplete(0L)
        );
    }

    /**
     * RabbitMQ를 통한 Notification의 처리 여부는 검증하지 않는다.
     * 테스트 트랜잭션과 RabbitMQ 트랜잭션이 별개로 동작함으로써 UseCase 통합 테스트에서는 해결이 불가.
     * 추후 방법을 찾는다면 추가하겠지만 현재 시도해본 모든 방법으로는 해결할 수 없었기 때문에 제외.
     * 대신 module-api의 컨트롤러 통합 테스트에서는 MockMvc 사용을 함으로써 flush가 되어 consumer 또는 consuemr를 통해 호출되는 메서드에서
     * BeforeEach의 데이터에 접근이 가능하기 때문에 Notificaiton을 검증.
     *
     * 즉, RabbitMQ 호출에 대한 검증은 Mocking 처리하고 Controller 통합테스트에서 처리를 완전하게 검증.
     */
    @Test
    @DisplayName(value = "회원 문의 답변 작성")
    void postMemberQnAReply() {
        MemberQnA memberQnA = newMemberQnAList.get(0);
        String content = "test Reply Content";
        QnAReplyInsertDTO insertDTO = new QnAReplyInsertDTO(memberQnA.getId(), content);

        doNothing().when(memberQnAExternalService).sendMemberQnANotification(anyLong(), any(MemberQnA.class));

        assertDoesNotThrow(() -> adminMemberQnAWriteUseCase.postMemberQnAReply(insertDTO, admin.getUserId()));

        List<QnADetailReplyDTO> replyList = memberQnAReplyRepository.findAllByQnAId(memberQnA.getId());
        assertFalse(replyList.isEmpty());

        QnADetailReplyDTO reply = replyList.get(0);

        assertEquals(admin.getNickname(), reply.writer());
        assertEquals(content, reply.replyContent());

        MemberQnA patchMemberQnA = memberQnARepository.findById(memberQnA.getId()).orElse(null);
        assertNotNull(patchMemberQnA);
        assertTrue(patchMemberQnA.isMemberQnAStat());
    }

    @Test
    @DisplayName(value = "회원 문의 답변 작성. 회원 문의 데이터가 없는 경우")
    void postMemberQnAReplyNotFound() {
        String content = "test Reply Content";
        QnAReplyInsertDTO insertDTO = new QnAReplyInsertDTO(0L, content);

        assertThrows(
                IllegalArgumentException.class,
                () -> adminMemberQnAWriteUseCase.postMemberQnAReply(insertDTO, admin.getUserId())
        );
    }

    @Test
    @DisplayName(value = "회원 문의 분류 추가")
    void postQnAClassification() {
        String classificationName = "테스트 분류";

        assertDoesNotThrow(() -> adminMemberQnAWriteUseCase.postQnAClassification(classificationName));

        List<QnAClassification> allQnAClassificationList = qnAClassificationRepository.findAll();

        assertFalse(allQnAClassificationList.isEmpty());
        assertEquals(qnAClassificationList.size() + 1, allQnAClassificationList.size());
        assertEquals(classificationName, allQnAClassificationList.get(allQnAClassificationList.size() - 1).getQnaClassificationName());
    }

    @Test
    @DisplayName(value = "회원 문의 분류 삭제")
    void deleteQnAClassification() {
        Long deleteId = qnAClassificationList.get(0).getId();

        assertDoesNotThrow(() -> adminMemberQnAWriteUseCase.deleteQnAClassification(deleteId));

        QnAClassification checkDeleteClassification = qnAClassificationRepository.findById(deleteId).orElse(null);

        assertNull(checkDeleteClassification);
    }

    @Test
    @DisplayName(value = "회원 문의 분류 삭제. 분류 아이디가 잘못된 경우")
    void deleteQnAClassificationWrongId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> adminMemberQnAWriteUseCase.deleteQnAClassification(0L)
        );
    }
}
