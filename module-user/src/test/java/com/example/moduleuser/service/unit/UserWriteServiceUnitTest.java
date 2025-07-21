package com.example.moduleuser.service.unit;

import com.example.moduleauth.model.dto.member.UserSearchPwDTO;
import com.example.moduleauth.repository.AuthRepository;
import com.example.moduleauth.repository.MemberRepository;
import com.example.modulecommon.model.entity.Auth;
import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.enumuration.Result;
import com.example.modulecommon.model.enumuration.Role;
import com.example.moduleuser.model.dto.member.in.JoinDTO;
import com.example.moduleuser.service.UserWriteService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class UserWriteServiceUnitTest {

    @InjectMocks
    @Spy
    private UserWriteService userWriteService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private AuthRepository authRepository;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private ValueOperations<String, String> valueOperations;

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
        Auth auth = Auth.builder()
                .auth(Role.MEMBER.getKey())
                .build();

        when(memberRepository.save(any(Member.class))).thenReturn(member);
        when(authRepository.save(any(Auth.class))).thenReturn(auth);

        String result = assertDoesNotThrow(() -> userWriteService.joinProc(joinDTO));

        verify(memberRepository).save(any(Member.class));
        verify(authRepository).save(any(Auth.class));
        assertEquals(Result.OK.getResultKey(), result);
    }

    @Test
    @DisplayName("비밀번호 찾기 요청. 정보 확인 후 인증번호 메일 전송")
    void searchPw() throws MessagingException {
        UserSearchPwDTO searchDTO = new UserSearchPwDTO("userId", "userName", "userEmail@userEmail.com");
        MimeMessage mimeMessage = mock(MimeMessage.class);

        when(memberRepository.findByPassword(searchDTO)).thenReturn(1L);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        doNothing().when(valueOperations).set(anyString(), anyString(), anyLong(), eq(TimeUnit.MINUTES));
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mimeMessage).setText(anyString(), eq("UTF-8"), eq("html"));;
        doNothing().when(javaMailSender).send(mimeMessage);
        doNothing().when(mimeMessage).addRecipients(MimeMessage.RecipientType.TO, searchDTO.userEmail());

        String result = assertDoesNotThrow(() -> userWriteService.searchPw(searchDTO));

        assertEquals(Result.OK.getResultKey(), result);
    }

    @Test
    @DisplayName(value = "비밀번호 찾기 요청. 사용자 정보가 없는 경우")
    void searchPwUserNotFound() {
        UserSearchPwDTO searchDTO = new UserSearchPwDTO("userId", "userName", "userEmail@userEmail.com");

        when(memberRepository.findByPassword(searchDTO)).thenReturn(0L);

        String result = assertDoesNotThrow(() -> userWriteService.searchPw(searchDTO));

        assertEquals(Result.NOTFOUND.getResultKey(), result);
    }

    @Test
    @DisplayName(value = "비밀번호 찾기 요청. 메일 전송 과정에서 Exception이 발생하는 경우.")
    void searchPwMessagingException() {
        UserSearchPwDTO searchDTO = new UserSearchPwDTO("userId", "userName", "userEmail@userEmail.com");

        when(memberRepository.findByPassword(searchDTO)).thenReturn(1L);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        doNothing().when(valueOperations).set(anyString(), anyString(), anyLong(), eq(TimeUnit.MINUTES));
        when(javaMailSender.createMimeMessage()).thenThrow(new RuntimeException("Mail send Fail"));

        String result = assertDoesNotThrow(() -> userWriteService.searchPw(searchDTO));

        assertEquals(Result.FAIL.getResultKey(), result);
    }
}
