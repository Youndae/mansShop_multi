package com.example.modulemypage.usecase;

import com.example.modulecommon.customException.CustomAccessDeniedException;
import com.example.modulecommon.customException.CustomNotFoundException;
import com.example.modulemypage.model.dto.memberQnA.business.MemberQnADetailDTO;
import com.example.modulemypage.service.MemberQnADataService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MemberQnAReadUseCaseUnitTest {

    @InjectMocks
    private MemberQnAReadUseCase memberQnAReadUseCase;

    @Mock
    private MemberQnADataService memberQnADataService;

    @Test
    @DisplayName(value = "회원 문의 상세 조회. 문의 아이디가 잘못된 경우")
    void getMemberQnADetailWrongQnAId() {

        when(memberQnADataService.findMemberQnADetailDTOById(anyLong()))
                .thenReturn(null);

        assertThrows(
                CustomNotFoundException.class,
                () -> memberQnAReadUseCase.getMemberQnADetail(1L, "nickname")
        );

        verify(memberQnADataService, never()).getMemberQnADetailAllReplies(anyLong());
    }

    @Test
    @DisplayName(value = "회원 문의 상세 조회. 작성자가 일치하지 않는 경우")
    void getMemberQnADetailWriterNotEquals() {
        MemberQnADetailDTO responseFixture = new MemberQnADetailDTO(
                1L,
                "qnaClassificationName",
                "qnaTitle",
                "writer",
                "qnaContent",
                LocalDateTime.now(),
                true
        );

        when(memberQnADataService.findMemberQnADetailDTOById(anyLong()))
                .thenReturn(responseFixture);
        when(memberQnADataService.getMemberQnADetailAllReplies(anyLong()))
                .thenReturn(Collections.emptyList());

        assertThrows(
                CustomAccessDeniedException.class,
                () -> memberQnAReadUseCase.getMemberQnADetail(1L, "nickname")
        );
    }

    @Test
    @DisplayName(value = "회원 문의 수정을 위한 조회. 문의 아이디가 잘못된 경우")
    void getModifyDataWrongQnAId() {
        when(memberQnADataService.findModifyDataByIdAndUserId(anyLong(), any()))
                .thenReturn(null);

        assertThrows(
                IllegalArgumentException.class,
                () -> memberQnAReadUseCase.getModifyData(1L, "nickname")
        );

        verify(memberQnADataService, never()).getAllQnAClassificationDTO();
    }
}
