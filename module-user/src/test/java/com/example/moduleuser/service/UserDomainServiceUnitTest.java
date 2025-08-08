package com.example.moduleuser.service;

import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.enumuration.Role;
import com.example.moduleuser.model.dto.member.in.JoinDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class UserDomainServiceUnitTest {

    @InjectMocks
    private UserDomainService userDomainService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    @DisplayName(value = "회원 가입 시 요청 DTO를 Member로 변환 후 반환")
    void getJoinMember() {
        JoinDTO joinDTO = new JoinDTO(
                "tester1",
                "1234",
                "testUsername",
                "testNickname",
                "01012345678",
                "2000/01/01",
                "tester@tester.com"
        );

        Member result = userDomainService.getJoinMember(joinDTO);

        assertNotNull(result);
        assertEquals(joinDTO.userId(), result.getUserId());
        assertTrue(passwordEncoder.matches(joinDTO.userPw(), result.getUserPw()));
        assertEquals(joinDTO.userName(), result.getUserName());
        assertEquals(joinDTO.nickname(), result.getNickname());
        assertEquals("010-1234-5678", result.getPhone());
        assertEquals(LocalDate.of(2000, 1, 1), result.getBirth());
        assertEquals(joinDTO.userEmail(), result.getUserEmail());
        assertEquals(1, result.getAuths().size());
        assertEquals(Role.MEMBER.getKey(), result.getAuths().get(0).getAuth());
    }
}
