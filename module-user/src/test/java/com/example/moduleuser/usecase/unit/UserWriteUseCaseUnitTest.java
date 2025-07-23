package com.example.moduleuser.usecase.unit;

import com.example.moduleauth.model.dto.member.UserSearchPwDTO;
import com.example.moduleauth.service.MemberReader;
import com.example.moduleauth.service.MemberStore;
import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.enumuration.Result;
import com.example.moduleuser.model.dto.member.in.JoinDTO;
import com.example.moduleuser.model.dto.member.in.UserCertificationDTO;
import com.example.moduleuser.service.UserDataService;
import com.example.moduleuser.service.UserDomainService;
import com.example.moduleuser.service.UserExternalService;
import com.example.moduleuser.usecase.UserWriteUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class UserWriteUseCaseUnitTest {

    @InjectMocks
    @Spy
    private UserWriteUseCase userWriteUseCase;

    @Mock
    private UserDomainService userDomainService;

    @Mock
    private UserDataService userDataService;

    @Mock
    private UserExternalService userExternalService;

    @Mock
    private MemberReader memberReader;

    @Mock
    private MemberStore memberStore;

    @Test
    @DisplayName(value = "회원가입 요청")
    void joinProc() {
        JoinDTO joinDTO = new JoinDTO(
                "userId",
                "userPw",
                "userName",
                "nickname",
                "01012345678",
                "2000/01/01",
                "userEmail@email.com"
        );
        Member member = joinDTO.toEntity();

        when(userDomainService.getJoinMember(any(JoinDTO.class))).thenReturn(member);
        doNothing().when(memberStore).saveMemberAndAuth(any(Member.class));

        String result = assertDoesNotThrow(() -> userWriteUseCase.joinProc(joinDTO));

        assertEquals(Result.OK.getResultKey(), result);
    }

    @Test
    @DisplayName("비밀번호 찾기 요청. 정보 확인 후 인증번호 메일 전송")
    void searchPw() throws Exception {
        UserSearchPwDTO searchDTO = new UserSearchPwDTO("userId", "userName", "userEmail@userEmail.com");

        when(memberReader.countMatchingBySearchPwDTO(searchDTO)).thenReturn(1L);
        when(userDomainService.createCertificationNumber()).thenReturn(123456);
        doNothing().when(userDataService).saveCertificationNumberToRedis(any(UserSearchPwDTO.class), anyInt());
        doNothing().when(userExternalService).sendCertificationMail(any(UserSearchPwDTO.class), anyInt());

        String result = assertDoesNotThrow(() -> userWriteUseCase.searchPassword(searchDTO));

        assertEquals(Result.OK.getResultKey(), result);
    }

    @Test
    @DisplayName(value = "비밀번호 찾기 요청. 사용자 정보가 없는 경우")
    void searchPwUserNotFound() {
        UserSearchPwDTO searchDTO = new UserSearchPwDTO("userId", "userName", "userEmail@userEmail.com");

        when(memberReader.countMatchingBySearchPwDTO(searchDTO)).thenReturn(0L);

        String result = assertDoesNotThrow(() -> userWriteUseCase.searchPassword(searchDTO));

        assertEquals(Result.NOTFOUND.getResultKey(), result);
    }

    @Test
    @DisplayName(value = "비밀번호 찾기 요청. 메일 전송 과정에서 Exception이 발생하는 경우.")
    void searchPwMessagingException() throws Exception {
        UserSearchPwDTO searchDTO = new UserSearchPwDTO("userId", "userName", "userEmail@userEmail.com");

        when(memberReader.countMatchingBySearchPwDTO(searchDTO)).thenReturn(1L);
        when(userDomainService.createCertificationNumber()).thenReturn(123456);
        doNothing().when(userDataService).saveCertificationNumberToRedis(any(UserSearchPwDTO.class), anyInt());
        doThrow(new RuntimeException("mail send fail")).when(userExternalService).sendCertificationMail(any(UserSearchPwDTO.class), anyInt());


        String result = assertDoesNotThrow(() -> userWriteUseCase.searchPassword(searchDTO));

        assertEquals(Result.FAIL.getResultKey(), result);
    }

    @Test
    @DisplayName(value = "사용자가 전달한 인증번호와 Redis에 캐싱된 인증번호 비교 검증")
    void checkCertificationNo() throws Exception {
        UserCertificationDTO certificationDTO = new UserCertificationDTO("tester", "123456");

        when(userDataService.getCertificationNumberFromRedis(any())).thenReturn(certificationDTO.certification());
        when(userDomainService.validateCertificationNo(any(), any())).thenReturn(true);

        String result = assertDoesNotThrow(() -> userWriteUseCase.checkCertificationNo(certificationDTO));

        assertEquals(Result.OK.getResultKey(), result);
    }

    @Test
    @DisplayName(value = "사용자가 전달한 인증번호와 Redis에 캐싱된 인증번호 비교 검증. Redis에 캐싱된 인증번호가 없는 경우")
    void checkCertificationNoNotFound() throws Exception {
        UserCertificationDTO certificationDTO = new UserCertificationDTO("tester", "123456");

        when(userDataService.getCertificationNumberFromRedis(any())).thenReturn(null);

        String result = assertDoesNotThrow(() -> userWriteUseCase.checkCertificationNo(certificationDTO));

        assertEquals(Result.FAIL.getResultKey(), result);
    }

    @Test
    @DisplayName(value = "사용자가 전달한 인증번호와 Redis에 캐싱된 인증번호 비교 검증. 일치하지 않는 경우")
    void checkCertificationNoNotEquals() throws Exception {
        UserCertificationDTO certificationDTO = new UserCertificationDTO("tester", "123456");

        when(userDataService.getCertificationNumberFromRedis(any())).thenReturn("456123");
        when(userDomainService.validateCertificationNo(any(), any())).thenReturn(false);

        String result = assertDoesNotThrow(() -> userWriteUseCase.checkCertificationNo(certificationDTO));

        assertEquals(Result.FAIL.getResultKey(), result);
    }

    @Test
    @DisplayName(value = "사용자가 전달한 인증번호와 Redis에 캐싱된 인증번호 비교 검증. 검증 과정 중 오류가 발생한 경우")
    void checkCertificationNoError() throws Exception {
        UserCertificationDTO certificationDTO = new UserCertificationDTO("tester", "123456");

        doThrow(new RuntimeException("Redis Connection Exception")).when(userDataService).getCertificationNumberFromRedis(any());

        String result = assertDoesNotThrow(() -> userWriteUseCase.checkCertificationNo(certificationDTO));

        assertEquals(Result.ERROR.getResultKey(), result);
    }
}
