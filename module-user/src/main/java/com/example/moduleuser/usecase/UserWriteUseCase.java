package com.example.moduleuser.usecase;

import com.example.modulecommon.customException.CustomBadCredentialsException;
import com.example.modulecommon.customException.CustomConflictException;
import com.example.modulecommon.customException.CustomNotFoundException;
import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.enumuration.ErrorCode;
import com.example.modulecommon.model.enumuration.Result;
import com.example.moduleuser.model.dto.member.in.*;
import com.example.moduleuser.service.UserDataService;
import com.example.moduleuser.service.UserDomainService;
import com.example.moduleuser.service.UserExternalService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
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
    public void joinProc(JoinDTO joinDTO) {
        try {
            Member member = userDomainService.getJoinMember(joinDTO);
            userDataService.saveMemberAndAuthToJoin(member);
        }catch(DataIntegrityViolationException e) {
            log.error("JoinProc DataIntegrityViolationException. joinDTO: {}", joinDTO);
            throw new CustomConflictException(ErrorCode.CONFLICT, ErrorCode.CONFLICT.getMessage());
        }catch(Exception e) {
            log.error("JoinProc Exception. {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    public void loginProc(String userId,
                           HttpServletRequest request,
                           HttpServletResponse response) {

        try {
            String statusResponse = userDomainService.getLoginUserStatusResponse(userId, request, response);

            if(statusResponse.equals(Result.FAIL.getResultKey()))
                throw new CustomBadCredentialsException(ErrorCode.UNAUTHORIZED, ErrorCode.UNAUTHORIZED.getMessage());
        }catch (Exception e) {
            log.info("login fail : {}", e.getMessage());
            throw new CustomBadCredentialsException(ErrorCode.UNAUTHORIZED, ErrorCode.UNAUTHORIZED.getMessage());
        }
    }

    public void issueOAuthUserToken(HttpServletRequest request, HttpServletResponse response) {
        Cookie temporaryCookie = userDomainService.getOAuthTemporaryCookie(request);

        if(temporaryCookie == null)
            throw new CustomBadCredentialsException(ErrorCode.UNAUTHORIZED, ErrorCode.UNAUTHORIZED.getMessage());

        String temporaryClaim = userDomainService.validateTemporaryClaimByUserId(temporaryCookie);
        userDataService.deleteTemporaryTokenAndCookie(temporaryClaim, response);
        if(!userDomainService.issueOAuthUserToken(temporaryClaim, request, response))
            throw new CustomBadCredentialsException(ErrorCode.UNAUTHORIZED, ErrorCode.UNAUTHORIZED.getMessage());
    }

    public String logoutProc(LogoutDTO dto, HttpServletResponse response) {
        return userDataService.deleteTokenAndCookieByLogout(dto, response);
    }

    public void searchPassword(UserSearchPwDTO searchDTO) {
        Long correctInfoCount = userDataService.countMatchingBySearchPwDTO(searchDTO);
        if(correctInfoCount == 0)
            throw new CustomNotFoundException(ErrorCode.BAD_REQUEST, ErrorCode.BAD_REQUEST.getMessage());

        int certificationNumber = userDomainService.createCertificationNumber();

        try {
            userDataService.saveCertificationNumberToRedis(searchDTO, certificationNumber);

            userExternalService.sendCertificationMail(searchDTO, certificationNumber);
        }catch (Exception e) {
            log.warn("mail send Exception. {}", e.getMessage());

            throw new RuntimeException(e.getMessage());
        }
    }

    public void checkCertificationNo(UserCertificationDTO certificationDTO) {
        try {
            String saveCertification = userDataService.getCertificationNumberFromRedis(certificationDTO.userId());

            if(saveCertification == null || !userDomainService.validateCertificationNo(certificationDTO.certification(), saveCertification))
                throw new CustomBadCredentialsException(ErrorCode.UNAUTHORIZED, ErrorCode.UNAUTHORIZED.getMessage());
        }catch(CustomBadCredentialsException e) {
            throw e;
        } catch (Exception e) {
            log.warn("certification check Exception. {}", e.getMessage());

            throw new RuntimeException(e.getMessage());
        }
    }

    public void resetPw(UserResetPwDTO resetDTO) {

        try {
            String saveCertification = userDataService.getCertificationNumberFromRedis(resetDTO.userId());
            userDataService.deleteCertificationNumberFromRedis(resetDTO.userId());

            if(saveCertification == null || !userDomainService.validateCertificationNo(resetDTO.certification(), saveCertification))
                throw new CustomBadCredentialsException(ErrorCode.UNAUTHORIZED, ErrorCode.UNAUTHORIZED.getMessage());
        }catch(CustomBadCredentialsException e) {
            throw e;
        }catch (Exception e) {
            log.warn("reset password Exception. {}", e.getMessage());

            throw new RuntimeException(e.getMessage());
        }

        Member member = userDataService.getMemberByUserIdOrElseNull(resetDTO.userId());
        if(member == null)
            throw new IllegalArgumentException();

        member.setUserPw(resetDTO.userPw());
        userDataService.saveMember(member);
    }

    public void patchMyPageUserInfo(MyPageInfoPatchDTO infoDTO, String userId) {
        Member member = userDataService.getMemberByUserIdOrElseIllegal(userId);
        member.patchUser(infoDTO.nickname(), infoDTO.phone(), infoDTO.mail());

        userDataService.saveMember(member);
    }
}
