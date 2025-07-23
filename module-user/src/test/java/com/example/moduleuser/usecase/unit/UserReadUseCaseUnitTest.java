package com.example.moduleuser.usecase.unit;

import com.example.moduleauth.model.dto.member.UserSearchDTO;
import com.example.moduleauth.service.MemberReader;
import com.example.modulecommon.fixture.MemberAndAuthFixture;
import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.enumuration.Result;
import com.example.moduleuser.model.dto.member.out.UserSearchIdResponseDTO;
import com.example.moduleuser.usecase.UserReadUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.Principal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserReadUseCaseUnitTest {

    @InjectMocks
    private UserReadUseCase userReadUseCase;

    @Mock
    private MemberReader memberReader;

    @Test
    @DisplayName(value = "회원가입시 아이디 중복체크. 정상인 경우")
    void checkJoinId() {
        when(memberReader.getMemberById(any())).thenReturn(null);

        String result = assertDoesNotThrow(() -> userReadUseCase.checkJoinUserId("userId"));

        assertEquals(Result.NO_DUPLICATE.getResultKey(), result);
    }

    @Test
    @DisplayName(value = "회원가입시 아이디 중복체크. 중복인 경우")
    void checkJoinIdIsDuplicated() {
        Member member = MemberAndAuthFixture.createDefaultMember(1).memberList().get(0);

        when(memberReader.getMemberById(any())).thenReturn(member);

        String result = assertDoesNotThrow(() -> userReadUseCase.checkJoinUserId(member.getUserId()));

        assertEquals(Result.DUPLICATE.getResultKey(), result);
    }

    @Test
    @DisplayName(value = "회원가입시 닉네임 중복체크. 정상인 경우")
    void checkJoinNickname() {
        when(memberReader.getMemberByUserId(any())).thenReturn(null);

        String result = assertDoesNotThrow(() -> userReadUseCase.checkNickname("nickname", null));

        assertEquals(Result.NO_DUPLICATE.getResultKey(), result);
    }

    @Test
    @DisplayName(value = "회원가입시 닉네임 중복체크. 중복인 경우")
    void checkJoinNicknameIsDuplicated() {
        Member member = MemberAndAuthFixture.createDefaultMember(1).memberList().get(0);

        when(memberReader.getMemberByNickname(any())).thenReturn(member);

        String result = assertDoesNotThrow(() -> userReadUseCase.checkNickname("nickname", null));

        assertEquals(Result.DUPLICATE.getResultKey(), result);
    }

    @Test
    @DisplayName(value = "정보 수정시 닉네임 중복체크. 정상인 경우")
    void checkNickname() {
        Principal principal = mock(Principal.class);

        when(memberReader.getMemberByNickname(any())).thenReturn(null);

        String result = assertDoesNotThrow(() -> userReadUseCase.checkNickname("nicknameElse", principal));

        assertEquals(Result.NO_DUPLICATE.getResultKey(), result);
    }

    @Test
    @DisplayName(value = "정보 수정시 닉네임 중복체크. 중복인 경우")
    void checkNicknameIsDuplicated() {
        Member member = MemberAndAuthFixture.createDefaultMember(1).memberList().get(0);

        Principal principal = mock(Principal.class);

        when(principal.getName()).thenReturn("userId");
        when(memberReader.getMemberByNickname(any())).thenReturn(member);

        String result = assertDoesNotThrow(() -> userReadUseCase.checkNickname(member.getNickname(), principal));

        assertEquals(Result.DUPLICATE.getResultKey(), result);
    }

    @Test
    @DisplayName(value = "정보 수정시 닉네임 중복체크. 중복이지만 자신이 사용하고 있는 닉네임인 경우")
    void checkNicknameIsNotDuplicated() {
        Member member = Member.builder()
                .userId("coco")
                .nickname("cocoNickname")
                .build();

        Principal principal = mock(Principal.class);

        when(principal.getName()).thenReturn(member.getUserId());
        when(memberReader.getMemberByNickname(any())).thenReturn(member);

        String result = assertDoesNotThrow(() -> userReadUseCase.checkNickname(member.getNickname(), principal));

        assertEquals(Result.NO_DUPLICATE.getResultKey(), result);
    }

    @Test
    @DisplayName(value = "아이디 찾기 요청")
    void searchId() {
        String userId = "userId";
        UserSearchDTO searchDTO = new UserSearchDTO("userName", "01012345678", null);

        when(memberReader.getSearchUserId(searchDTO)).thenReturn(userId);

        UserSearchIdResponseDTO result = assertDoesNotThrow(() -> userReadUseCase.searchId(searchDTO));

        assertEquals(Result.OK.getResultKey(), result.message());
        assertEquals(userId, result.userId());
    }

    @Test
    @DisplayName(value = "아이디 찾기 요청. 정보가 없는 경우")
    void searchIdIsNull() {
        UserSearchDTO searchDTO = new UserSearchDTO("userName", "01012345678", null);

        when(memberReader.getSearchUserId(searchDTO)).thenReturn(null);

        UserSearchIdResponseDTO result = assertDoesNotThrow(() -> userReadUseCase.searchId(searchDTO));

        assertEquals(Result.NOTFOUND.getResultKey(), result.message());
        assertNull(result.userId());
    }

}
