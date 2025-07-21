package com.example.moduleuser.usecase;

import com.example.moduleauth.model.dto.member.UserSearchPwDTO;
import com.example.moduleuser.model.dto.member.in.*;
import com.example.moduleuser.model.dto.member.out.UserStatusResponseDTO;
import com.example.moduleuser.service.UserWriteService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserWriteUseCase {

    private final UserWriteService userWriteService;

    public String joinProc(JoinDTO joinDTO) {

        return userWriteService.joinProc(joinDTO);
    }

    public UserStatusResponseDTO loginProc(LoginDTO loginDTO,
                                           HttpServletRequest request,
                                           HttpServletResponse response) {
        return userWriteService.loginProc(loginDTO, request, response);
    }

    public String oAuthUserIssueToken(HttpServletRequest request,
                                      HttpServletResponse response) {
        return userWriteService.oAuthUserIssueToken(request, response);
    }

    public String logoutProc(LogoutDTO dto, HttpServletResponse response) {
        return userWriteService.logoutProc(dto, response);
    }

    public String searchPassword(UserSearchPwDTO searchDTO) {
        return userWriteService.searchPw(searchDTO);
    }

    public String checkCertificationNo(UserCertificationDTO certificationDTO) {
        return userWriteService.checkCertificationNo(certificationDTO);
    }

    public String resetPw(UserResetPwDTO resetDTO) {
        return userWriteService.resetPw(resetDTO);
    }
}
