package com.example.moduleuser.usecase.admin;

import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.enumuration.Result;
import com.example.moduleuser.model.dto.admin.in.AdminPostPointDTO;
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
public class AdminMemberWriteUseCaseUnitTest {

    @InjectMocks
    private AdminMemberWriteUseCase adminMemberWriteUseCase;

    @Mock
    private UserDataService userDataService;

    @Test
    @DisplayName(value = "회원 포인트 지급")
    void postPoint() {
        Member member = Member.builder()
                .userId("tester")
                .memberPoint(100L)
                .build();

        when(userDataService.getMemberByUserIdOrElseIllegal(any())).thenReturn(member);
        doNothing().when(userDataService).saveMember(any(Member.class));

        assertDoesNotThrow(() -> adminMemberWriteUseCase.postPoint(new AdminPostPointDTO(member.getUserId(), 1000)));
    }

    @Test
    @DisplayName(value = "회원 포인트 지급. 사용자 아이디가 잘못된 경우")
    void postPointWrongUserId() {
        when(userDataService.getMemberByUserIdOrElseIllegal(any())).thenThrow(IllegalArgumentException.class);

        assertThrows(
                IllegalArgumentException.class,
                () -> adminMemberWriteUseCase.postPoint(new AdminPostPointDTO("tester", 1000))
        );

        verify(userDataService, never()).saveMember(any(Member.class));
    }
}
