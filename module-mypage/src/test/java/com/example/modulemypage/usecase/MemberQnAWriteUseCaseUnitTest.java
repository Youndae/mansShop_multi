package com.example.modulemypage.usecase;

import com.example.modulecommon.customException.CustomAccessDeniedException;
import com.example.modulecommon.model.dto.qna.in.QnAReplyInsertDTO;
import com.example.modulecommon.model.dto.qna.in.QnAReplyPatchDTO;
import com.example.modulecommon.model.entity.*;
import com.example.modulecommon.model.enumuration.Result;
import com.example.modulecommon.model.enumuration.Role;
import com.example.modulemypage.model.dto.memberQnA.in.MemberQnAInsertDTO;
import com.example.modulemypage.model.dto.memberQnA.in.MemberQnAModifyDTO;
import com.example.modulemypage.service.MemberQnADataService;
import com.example.modulemypage.service.MemberQnADomainService;
import com.example.moduleuser.service.UserDataService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MemberQnAWriteUseCaseUnitTest {

    @InjectMocks
    private MemberQnAWriteUseCase memberQnAWriteUseCase;

    @Mock
    private UserDataService userDataService;

    @Mock
    private MemberQnADataService memberQnADataService;

    @Mock
    private MemberQnADomainService memberQnADomainService;

    private static final String USER_ID = "tester";

    private MemberQnAInsertDTO getMemberQnAInsertDTO() {
        return new MemberQnAInsertDTO(
                "testTitle",
                "testContent",
                1L
        );
    }

    private QnAReplyInsertDTO getQnAReplyInsertDTO() {
        return new QnAReplyInsertDTO(1L, "testReplyContent");
    }

    private QnAReplyPatchDTO getQnAReplyPatchDTO() {
        return new QnAReplyPatchDTO(1L, "testPatchReplyContent");
    }

    private MemberQnAModifyDTO getMemberQnAModifyDTO() {
        return new MemberQnAModifyDTO(
                1L,
                "testPatchTitle",
                "testPatchContent",
                1L
        );
    }

    private Member getMember() {
        Member member = Member.builder()
                .userId(USER_ID)
                .build();
        Auth auth = Auth.builder()
                .auth(Role.MEMBER.getRole())
                .build();

        member.addMemberAuth(auth);

        return member;
    }

    @Test
    @DisplayName(value = "회원 문의 작성. 회원 아이디가 잘못된 경우")
    void postMemberQnAWrongUserId() {
        MemberQnAInsertDTO insertDTO = getMemberQnAInsertDTO();
        when(userDataService.getMemberByUserIdOrElseIllegal(any()))
                .thenThrow(IllegalArgumentException.class);

        assertThrows(
                IllegalArgumentException.class,
                () -> memberQnAWriteUseCase.postMemberQnA(insertDTO, USER_ID)
        );

        verify(memberQnADataService, never())
                .findQnAClassificationByIdOrElseIllegal(anyLong());
        verify(memberQnADomainService, never())
                .buildInsertMemberQnA(any(MemberQnAInsertDTO.class), any(QnAClassification.class), any(Member.class));
        verify(memberQnADataService, never())
                .saveMemberQnA(any(MemberQnA.class));
    }

    @Test
    @DisplayName(value = "회원 문의 작성. 문의 분류 아이디가 잘못된 경우")
    void postMemberQnAWrongQnAClassificationId() {
        MemberQnAInsertDTO insertDTO = getMemberQnAInsertDTO();
        Member member = getMember();
        when(userDataService.getMemberByUserIdOrElseIllegal(any()))
                .thenReturn(member);
        when(memberQnADataService.findQnAClassificationByIdOrElseIllegal(anyLong()))
                .thenThrow(IllegalArgumentException.class);

        assertThrows(
                IllegalArgumentException.class,
                () -> memberQnAWriteUseCase.postMemberQnA(insertDTO, USER_ID)
        );

        verify(memberQnADomainService, never())
                .buildInsertMemberQnA(any(MemberQnAInsertDTO.class), any(QnAClassification.class), any(Member.class));
        verify(memberQnADataService, never())
                .saveMemberQnA(any(MemberQnA.class));
    }

    @Test
    @DisplayName(value = "회원 문의 답변 작성")
    void postMemberQnAReply() {
        QnAReplyInsertDTO insertDTO = getQnAReplyInsertDTO();
        Member member = getMember();
        MemberQnA memberQnA = MemberQnA.builder()
                .id(1L)
                .member(member)
                .build();
        when(userDataService.getMemberByUserIdFetchAuthsOrElseIllegal(any()))
                .thenReturn(member);
        when(memberQnADataService.findMemberQnAByIdOrElseIllegal(anyLong()))
                .thenReturn(memberQnA);

        when(memberQnADataService.saveMemberQnA(any(MemberQnA.class))).thenReturn(1L);
        when(memberQnADomainService.buildInsertMemberQnaReply(any(Member.class), any(MemberQnA.class), any()))
                .thenReturn(MemberQnAReply.builder().id(1L).build());
        doNothing().when(memberQnADataService)
                .saveMemberQnAReply(any(MemberQnAReply.class));

        assertDoesNotThrow(
                () -> memberQnAWriteUseCase.postMemberQnAReply(insertDTO, USER_ID)
        );
    }

    @Test
    @DisplayName(value = "회원 문의 답변 작성. 회원 아이디가 잘못된 경우")
    void postMemberQnAReplyWrongUserId() {
        QnAReplyInsertDTO insertDTO = getQnAReplyInsertDTO();

        when(userDataService.getMemberByUserIdFetchAuthsOrElseIllegal(any()))
                .thenThrow(IllegalArgumentException.class);

        assertThrows(
                IllegalArgumentException.class,
                () -> memberQnAWriteUseCase.postMemberQnAReply(insertDTO, USER_ID)
        );

        verify(memberQnADataService, never())
                .findMemberQnAByIdOrElseIllegal(anyLong());
        verify(memberQnADataService, never())
                .findMemberQnAByIdOrElseIllegal(anyLong());
        verify(memberQnADataService, never())
                .saveMemberQnA(any(MemberQnA.class));
        verify(memberQnADomainService, never())
                .buildInsertMemberQnaReply(any(Member.class), any(MemberQnA.class), any());
        verify(memberQnADataService, never())
                .saveMemberQnAReply(any(MemberQnAReply.class));
    }

    @Test
    @DisplayName(value = "회원 문의 답변 작성. 문의 아이디가 잘못된 경우")
    void postMemberQnAReplyWrongQnAId() {
        QnAReplyInsertDTO insertDTO = getQnAReplyInsertDTO();
        Member member = getMember();

        when(userDataService.getMemberByUserIdFetchAuthsOrElseIllegal(any()))
                .thenReturn(member);
        when(memberQnADataService.findMemberQnAByIdOrElseIllegal(anyLong()))
                .thenThrow(IllegalArgumentException.class);

        assertThrows(
                IllegalArgumentException.class,
                () -> memberQnAWriteUseCase.postMemberQnAReply(insertDTO, USER_ID)
        );

        verify(memberQnADataService, never())
                .saveMemberQnA(any(MemberQnA.class));
        verify(memberQnADomainService, never())
                .buildInsertMemberQnaReply(any(Member.class), any(MemberQnA.class), any());
        verify(memberQnADataService, never())
                .saveMemberQnAReply(any(MemberQnAReply.class));
    }

    @Test
    @DisplayName(value = "회원 문의 답변 작성. 관리자가 아닌데 문의 작성자도 아닌 경우")
    void postMemberQnAReplyWriterNotEquals() {
        QnAReplyInsertDTO insertDTO = getQnAReplyInsertDTO();
        Member member = getMember();
        MemberQnA memberQnA = MemberQnA.builder()
                        .id(1L)
                        .member(Member.builder().userId("WrongUserId").build())
                        .build();
        when(userDataService.getMemberByUserIdFetchAuthsOrElseIllegal(any()))
                .thenReturn(member);
        when(memberQnADataService.findMemberQnAByIdOrElseIllegal(anyLong()))
                .thenReturn(memberQnA);

        assertThrows(
                CustomAccessDeniedException.class,
                () -> memberQnAWriteUseCase.postMemberQnAReply(insertDTO, USER_ID)
        );



        verify(memberQnADataService, never())
                .saveMemberQnA(any(MemberQnA.class));
        verify(memberQnADomainService, never())
                .buildInsertMemberQnaReply(any(Member.class), any(MemberQnA.class), any());
        verify(memberQnADataService, never())
                .saveMemberQnAReply(any(MemberQnAReply.class));
    }

    @Test
    @DisplayName(value = "회원 문의 답변 수정")
    void patchMemberQnAReply() {
        QnAReplyPatchDTO replyDTO = getQnAReplyPatchDTO();
        Member member = getMember();
        MemberQnAReply replyFixture = MemberQnAReply.builder()
                .id(1L)
                .member(member)
                .build();
        when(memberQnADataService.findMemberQnAReplyByIdOrElseIllegal(anyLong()))
                .thenReturn(replyFixture);

        doNothing().when(memberQnADataService).saveMemberQnAReply(any(MemberQnAReply.class));

        assertDoesNotThrow(
                () -> memberQnAWriteUseCase.patchMemberQnAReply(replyDTO, USER_ID)
        );
    }

    @Test
    @DisplayName(value = "회원 문의 답변 수정. 문의 답변 아이디가 잘못된 경우")
    void patchMemberQnAReplyWrongReplyId() {
        QnAReplyPatchDTO replyDTO = getQnAReplyPatchDTO();

        when(memberQnADataService.findMemberQnAReplyByIdOrElseIllegal(anyLong()))
                .thenThrow(IllegalArgumentException.class);

        assertThrows(
                IllegalArgumentException.class,
                () -> memberQnAWriteUseCase.patchMemberQnAReply(replyDTO, USER_ID)
        );

        verify(memberQnADataService, never())
                .saveMemberQnAReply(any(MemberQnAReply.class));
    }

    @Test
    @DisplayName(value = "회원 문의 답변 수정. 작성자가 일치하지 않는 경우")
    void patchMemberQnAReplyWriterNotEquals() {
        QnAReplyPatchDTO replyDTO = getQnAReplyPatchDTO();
        Member member = getMember();
        MemberQnAReply replyFixture = MemberQnAReply.builder()
                                        .id(1L)
                                        .member(member)
                                        .build();
        when(memberQnADataService.findMemberQnAReplyByIdOrElseIllegal(anyLong()))
                .thenReturn(replyFixture);

        assertThrows(
                CustomAccessDeniedException.class,
                () -> memberQnAWriteUseCase.patchMemberQnAReply(replyDTO, "WrongUserId")
        );

        verify(memberQnADataService, never())
                .saveMemberQnAReply(any(MemberQnAReply.class));
    }

    @Test
    @DisplayName(value = "회원 문의 수정. 문의 아이디가 잘못된 경우")
    void patchMemberQnAWrongQnAId() {
        MemberQnAModifyDTO modifyDTO = getMemberQnAModifyDTO();

        when(memberQnADataService.findMemberQnAByIdOrElseIllegal(anyLong()))
                .thenThrow(IllegalArgumentException.class);

        assertThrows(
                IllegalArgumentException.class,
                () -> memberQnAWriteUseCase.patchMemberQnA(modifyDTO, USER_ID)
        );

        verify(memberQnADataService, never()).findQnAClassificationByIdOrElseIllegal(anyLong());
        verify(memberQnADataService, never()).saveMemberQnA(any(MemberQnA.class));
    }

    @Test
    @DisplayName(value = "회원 문의 수정. 작성자가 일치하지 않는 경우")
    void patchMemberQnAWriterNotEquals() {
        MemberQnAModifyDTO modifyDTO = getMemberQnAModifyDTO();
        Member member = getMember();
        MemberQnA memberQnAFixture = MemberQnA.builder()
                                        .id(1L)
                                        .member(member)
                                        .build();
        when(memberQnADataService.findMemberQnAByIdOrElseIllegal(anyLong()))
                .thenReturn(memberQnAFixture);

        assertThrows(
                CustomAccessDeniedException.class,
                () -> memberQnAWriteUseCase.patchMemberQnA(modifyDTO, "WrongUserId")
        );

        verify(memberQnADataService, never()).findQnAClassificationByIdOrElseIllegal(anyLong());
        verify(memberQnADataService, never()).saveMemberQnA(any(MemberQnA.class));
    }

    @Test
    @DisplayName(value = "회원 문의 수정. 문의 분류 아이디가 잘못된 경우")
    void patchMemberQnAWrongQnAClassificationId() {
        MemberQnAModifyDTO modifyDTO = getMemberQnAModifyDTO();
        Member member = getMember();
        MemberQnA memberQnAFixture = MemberQnA.builder()
                .id(1L)
                .member(member)
                .build();

        when(memberQnADataService.findMemberQnAByIdOrElseIllegal(anyLong()))
                .thenReturn(memberQnAFixture);
        when(memberQnADataService.findQnAClassificationByIdOrElseIllegal(anyLong()))
                .thenThrow(IllegalArgumentException.class);

        assertThrows(
                IllegalArgumentException.class,
                () -> memberQnAWriteUseCase.patchMemberQnA(modifyDTO, USER_ID)
        );

        verify(memberQnADataService, never()).saveMemberQnA(any(MemberQnA.class));
    }

    @Test
    @DisplayName(value = "회원 문의 삭제")
    void deleteMemberQnA() {
        Member member = getMember();
        MemberQnA memberQnAFixture = MemberQnA.builder()
                .id(1L)
                .member(member)
                .build();

        when(memberQnADataService.findMemberQnAByIdOrElseIllegal(anyLong()))
                .thenReturn(memberQnAFixture);
        doNothing().when(memberQnADataService).deleteMemberQnAById(anyLong());

        assertDoesNotThrow(
                () -> memberQnAWriteUseCase.deleteMemberQnA(1L, USER_ID)
        );
    }

    @Test
    @DisplayName(value = "회원 문의 삭제. 문의 아이디가 잘못된 경우")
    void deleteMemberQnAWrongQnAId() {

        when(memberQnADataService.findMemberQnAByIdOrElseIllegal(anyLong()))
                .thenThrow(IllegalArgumentException.class);

        assertThrows(
                IllegalArgumentException.class,
                () -> memberQnAWriteUseCase.deleteMemberQnA(1L, USER_ID)
        );

        verify(memberQnADataService, never()).deleteMemberQnAById(anyLong());
    }

    @Test
    @DisplayName(value = "회원 문의 삭제. 작성자가 일치하지 않는 경우")
    void deleteMemberQnAWriterNotEquals() {
        Member member = getMember();
        MemberQnA memberQnAFixture = MemberQnA.builder()
                .id(1L)
                .member(member)
                .build();

        when(memberQnADataService.findMemberQnAByIdOrElseIllegal(anyLong()))
                .thenReturn(memberQnAFixture);

        assertThrows(
                CustomAccessDeniedException.class,
                () -> memberQnAWriteUseCase.deleteMemberQnA(1L, "wrongUserId")
        );

        verify(memberQnADataService, never()).deleteMemberQnAById(anyLong());
    }
}
