package com.example.moduleuser.service;

import com.example.moduleauthapi.model.dto.TokenIssueResponse;
import com.example.moduleauthapi.model.dto.TokenVerifyResult;
import com.example.moduleauthapi.service.JWTTokenProvider;
import com.example.modulecommon.customException.CustomBadCredentialsException;
import com.example.modulecommon.model.entity.Auth;
import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.enumuration.ErrorCode;
import com.example.modulecommon.model.enumuration.MailSuffix;
import com.example.modulecommon.model.enumuration.Role;
import com.example.moduleconfig.properties.CookieProperties;
import com.example.moduleconfig.properties.TokenProperties;
import com.example.moduleuser.model.dto.member.business.LoginUserInfo;
import com.example.moduleuser.model.dto.member.in.JoinDTO;
import com.example.moduleuser.model.dto.member.out.MyPageInfoDTO;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.util.WebUtils;

import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDomainService {

    private final JWTTokenProvider jwtTokenProvider;

    private final TokenProperties tokenProperties;

    private final CookieProperties cookieProperties;

    public Member getJoinMember(JoinDTO dto) {
        Member memberEntity = dto.toEntity();
        Auth auth = Auth.builder()
                .auth(Role.MEMBER.getKey())
                .build();

        memberEntity.addMemberAuth(auth);

        return memberEntity;
    }

    public TokenIssueResponse getLoginUserStatusResponse(LoginUserInfo loginUserInfo,
                                                         HttpServletRequest request,
                                                         HttpServletResponse response) {

        try {
            TokenIssueResponse result = checkInoAndIssueToken(loginUserInfo, request, response);

            if(result.accessToken() == null){
                log.warn("UserDomainService.getLoginUserStatusResponse :: AccessToken is null. Issue token result : {}", result);
                throw new CustomBadCredentialsException(ErrorCode.UNAUTHORIZED, ErrorCode.UNAUTHORIZED.getMessage());
            }

            return result;
        } catch(Exception e) {
            log.warn("UserDomainService.getLoginUserStatusResponse :: Exception");
            e.printStackTrace();
        }

        return null;
    }

    private TokenIssueResponse checkInoAndIssueToken(LoginUserInfo loginInfo,
                                                     HttpServletRequest request,
                                                     HttpServletResponse response){
        Cookie inoCookie = WebUtils.getCookie(request, cookieProperties.getIno().getHeader());

        if(inoCookie == null)
            return jwtTokenProvider.issueAllTokens(loginInfo.userId(), loginInfo.role(),  response);
        else
            return jwtTokenProvider.issueTokens(loginInfo.userId(), loginInfo.role(), inoCookie.getValue(), response);
    }

    public Cookie getOAuthTemporaryCookie(HttpServletRequest request) {
        return WebUtils.getCookie(request, tokenProperties.getTemporary().getHeader());
    }

    public TokenVerifyResult validateTemporaryClaimByUserId(Cookie temporaryCookie) {
        String temporaryValue = temporaryCookie.getValue();

        return jwtTokenProvider.verifyTemporaryToken(temporaryValue);
    }

    public TokenIssueResponse issueOAuthUserToken(TokenVerifyResult verifyResult,
                                       HttpServletRequest request,
                                       HttpServletResponse response) {

        LoginUserInfo loginUserInfo = new LoginUserInfo(verifyResult.userId(), verifyResult.role());

        return checkInoAndIssueToken(loginUserInfo, request, response);
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
