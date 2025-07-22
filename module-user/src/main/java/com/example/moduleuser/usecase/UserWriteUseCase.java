package com.example.moduleuser.usecase;

import com.example.moduleauth.config.user.CustomUser;
import com.example.moduleauth.model.dto.member.UserSearchPwDTO;
import com.example.modulecommon.customException.CustomBadCredentialsException;
import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.enumuration.ErrorCode;
import com.example.modulecommon.model.enumuration.Result;
import com.example.moduleuser.model.dto.member.in.*;
import com.example.moduleuser.model.dto.member.out.UserStatusResponseDTO;
import com.example.moduleuser.service.UserDataService;
import com.example.moduleuser.service.UserDomainService;
import com.example.moduleuser.service.UserExternalService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserWriteUseCase {

    private final UserDataService userDataService;

    private final UserDomainService userDomainService;

    private final UserExternalService userExternalService;


    @Transactional(rollbackFor = Exception.class)
    public String joinProc(JoinDTO joinDTO) {
        Member member = userDomainService.getJoinMember(joinDTO);

        return userDataService.saveMember(member);
    }

    public UserStatusResponseDTO loginProc(LoginDTO loginDTO,
                                           HttpServletRequest request,
                                           HttpServletResponse response) {

        try {
            CustomUser authenticateUser = userDomainService.loginAuthenticated(loginDTO);
            UserStatusResponseDTO responseDTO = userDomainService.getLoginUserStatusResponse(authenticateUser, request, response);

            if(responseDTO == null)
                throw new CustomBadCredentialsException(ErrorCode.BAD_CREDENTIALS, ErrorCode.BAD_CREDENTIALS.getMessage());

            return responseDTO;
        }catch (Exception e) {
            log.info("login fail : {}", e.getMessage());
            throw new CustomBadCredentialsException(ErrorCode.BAD_CREDENTIALS, ErrorCode.BAD_CREDENTIALS.getMessage());
        }
    }

    public String issueOAuthUserToken(HttpServletRequest request, HttpServletResponse response) {
        Cookie temporaryCookie = userDomainService.getOAuthTemporaryCookie(request);

        if(temporaryCookie == null)
            throw new CustomBadCredentialsException(ErrorCode.BAD_CREDENTIALS, ErrorCode.BAD_CREDENTIALS.getMessage());

        String temporaryClaim = userDomainService.validateTemporaryClaimByUserId(temporaryCookie);
        userDataService.deleteTemporaryTokenAndCookie(temporaryClaim, response);
        if(userDomainService.issueOAuthUserToken(temporaryClaim, request, response))
            return Result.OK.getResultKey();
        else
            throw new CustomBadCredentialsException(ErrorCode.BAD_CREDENTIALS, ErrorCode.BAD_CREDENTIALS.getMessage());
    }

    public String logoutProc(LogoutDTO dto, HttpServletResponse response) {
        return userDataService.deleteTokenAndCookieByLogout(dto, response);
    }

    public String searchPassword(UserSearchPwDTO searchDTO) {
        Long correctInfoCount = userDataService.searchPwCorrectInfoCount(searchDTO);
        if(correctInfoCount == 0)
            return Result.NOTFOUND.getResultKey();

        int certificationNumber = userDomainService.createCertificationNumber();

        try {
            userDataService.saveCertificationNumberToRedis(searchDTO, certificationNumber);

            userExternalService.sendCertificationMail(searchDTO, certificationNumber);

            return Result.OK.getResultKey();
        }catch (Exception e) {
            log.warn("mail send Exception");
            e.printStackTrace();
            return Result.FAIL.getResultKey();
        }
    }

    public String checkCertificationNo(UserCertificationDTO certificationDTO) {
        try {
            String saveCertification = userDataService.getCertificationNumberFromRedis(certificationDTO.userId());
            if(saveCertification == null || !userDomainService.validateCertificationNo(certificationDTO.certification(), saveCertification))
                return Result.FAIL.getResultKey();
        }catch (Exception e) {
            log.warn("certification check Exception");
            e.printStackTrace();
            return Result.ERROR.getResultKey();
        }

        return Result.OK.getResultKey();
    }

    public String resetPw(UserResetPwDTO resetDTO) {

        try {
            String saveCertification = userDataService.getCertificationNumberFromRedis(resetDTO.userId());
            userDataService.deleteCertificationNumberFromRedis(resetDTO.userId());

            if(saveCertification == null || !userDomainService.validateCertificationNo(resetDTO.certification(), saveCertification))
                return Result.FAIL.getResultKey();
        }catch (Exception e) {
            log.warn("reset password Exception");
            e.printStackTrace();
            return Result.ERROR.getResultKey();
        }

        Member member = userDataService.getMemberByUserIdNotFoundIllegalException(resetDTO.userId());
        userDataService.patchPassword(member, resetDTO);

        return Result.OK.getResultKey();
    }
}
