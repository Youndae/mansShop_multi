package com.example.modulemypage.usecase.admin;

import com.example.modulecommon.model.dto.qna.in.QnAReplyInsertDTO;
import com.example.modulecommon.model.entity.MemberQnA;
import com.example.modulecommon.model.enumuration.Result;
import com.example.modulemypage.service.MemberQnADataService;
import com.example.modulemypage.service.MemberQnAExternalService;
import com.example.modulemypage.usecase.MemberQnAWriteUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminMemberQnAWriteUseCaseUnitTest {

    @InjectMocks
    private AdminMemberQnAWriteUseCase adminMemberQnAWriteUseCase;

    @Mock
    private MemberQnADataService memberQnADataService;

    @Mock
    private MemberQnAWriteUseCase memberQnAWriteUseCase;

    @Mock
    private MemberQnAExternalService memberQnAExternalService;

    @Test
    @DisplayName(value = "회원 문의 답변 작성. 답변 데이터 저장을 실패한 경우")
    void postMemberQnAReplyFiledPost() {
        QnAReplyInsertDTO insertDTO = new QnAReplyInsertDTO(1L, "testContent");

        when(memberQnAWriteUseCase.postMemberQnAReply(any(QnAReplyInsertDTO.class), any())).thenReturn(Result.FAIL.getResultKey());

        assertThrows(IllegalArgumentException.class, () -> adminMemberQnAWriteUseCase.postMemberQnAReply(insertDTO, "tester"));

        verify(memberQnADataService, never()).findMemberQnAByIdOrElseIllegal(anyLong());
        verify(memberQnADataService, never()).saveMemberQnA(any(MemberQnA.class));
        verify(memberQnAExternalService, never()).sendMemberQnANotification(anyLong(), any(MemberQnA.class));
    }

    @Test
    @DisplayName(value = "회원 문의 분류 삭제. 분류 아이디가 잘못된 경우")
    void deleteQnAClassificationWrongId() {
        when(memberQnADataService.getQnAClassificationByIdOrElseIllegal(anyLong())).thenThrow(IllegalArgumentException.class);

        assertThrows(IllegalArgumentException.class, () -> adminMemberQnAWriteUseCase.deleteQnAClassification(1L));

        verify(memberQnADataService, never()).deleteQnAClassificationById(anyLong());
    }
}
