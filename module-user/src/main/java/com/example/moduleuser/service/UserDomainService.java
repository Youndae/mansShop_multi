package com.example.moduleuser.service;

import com.example.moduleauthapi.service.JWTTokenProvider;
import com.example.modulecommon.customException.CustomAccessDeniedException;
import com.example.modulecommon.customException.CustomTokenStealingException;
import com.example.modulecommon.model.entity.Auth;
import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.enumuration.ErrorCode;
import com.example.modulecommon.model.enumuration.MailSuffix;
import com.example.modulecommon.model.enumuration.Result;
import com.example.modulecommon.model.enumuration.Role;
import com.example.moduleuser.model.dto.member.in.JoinDTO;
import com.example.moduleuser.model.dto.member.out.MyPageInfoDTO;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.WebUtils;

import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDomainService {

    private final JWTTokenProvider jwtTokenProvider;

    @Value("#{jwt['token.temporary.header']}")
    private String temporaryHeader;

    @Value("#{jwt['cookie.ino.header']}")
    private String inoHeader;

    public Member getJoinMember(JoinDTO dto) {
        Member memberEntity = dto.toEntity();
        Auth auth = Auth.builder()
                .auth(Role.MEMBER.getKey())
                .build();

        memberEntity.addMemberAuth(auth);

        return memberEntity;
    }

    public String getLoginUserStatusResponse(String userId,
                                                            HttpServletRequest request,
                                                            HttpServletResponse response) {

        if(checkInoAndIssueToken(userId, request, response))
            return Result.OK.getResultKey();

        return Result.FAIL.getResultKey();
    }

    private boolean checkInoAndIssueToken(String userId,
                                          HttpServletRequest request,
                                          HttpServletResponse response){
        Cookie inoCookie = WebUtils.getCookie(request, inoHeader);

        if(inoCookie == null)
            jwtTokenProvider.issueAllTokens(userId, response);
        else
            jwtTokenProvider.issueTokens(userId, inoCookie.getValue(), response);

        return true;
    }

    public Cookie getOAuthTemporaryCookie(HttpServletRequest request) {
        return WebUtils.getCookie(request, temporaryHeader);
    }

    public String validateTemporaryClaimByUserId(Cookie temporaryCookie) {
        String temporaryValue = temporaryCookie.getValue();
        String temporaryClaimByUserId = jwtTokenProvider.verifyTemporaryToken(temporaryValue);

        if(temporaryClaimByUserId.equals(Result.WRONG_TOKEN.getResultKey())
                || temporaryClaimByUserId.equals(Result.TOKEN_EXPIRATION.getResultKey()))
            throw new CustomAccessDeniedException(ErrorCode.ACCESS_DENIED, ErrorCode.ACCESS_DENIED.getMessage());
        else if(temporaryClaimByUserId.equals(Result.TOKEN_STEALING.getResultKey()))
            throw new CustomTokenStealingException(ErrorCode.TOKEN_STEALING, ErrorCode.TOKEN_STEALING.getMessage());

        return temporaryClaimByUserId;
    }

    public boolean issueOAuthUserToken(String temporaryClaim,
                                       HttpServletRequest request,
                                       HttpServletResponse response) {
        return checkInoAndIssueToken(temporaryClaim, request, response);
    }

    public int createCertificationNumber() {
        Random ran = new Random();
        return ran.nextInt(899999) + 100001;
    }

    public boolean validateCertificationNo(String certification, String saveCertification) {
        return certification.equals(saveCertification);
    }

    public MyPageInfoDTO createMyPageInfoDTO(Member member) {
        String[] splitMail = member.getUserEmail().split("@");
        String mailSuffix = splitMail[1].substring(0, splitMail[1].indexOf('.'));
        String type = MailSuffix.findSuffixType(mailSuffix);

        return new MyPageInfoDTO(member, splitMail, type);
    }
}
