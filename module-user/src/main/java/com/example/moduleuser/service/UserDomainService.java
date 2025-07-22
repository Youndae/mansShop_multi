package com.example.moduleuser.service;

import com.example.moduleauth.config.jwt.JWTTokenProvider;
import com.example.moduleauth.config.user.CustomUser;
import com.example.modulecommon.customException.CustomAccessDeniedException;
import com.example.modulecommon.customException.CustomTokenStealingException;
import com.example.modulecommon.model.entity.Auth;
import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.enumuration.ErrorCode;
import com.example.modulecommon.model.enumuration.Result;
import com.example.modulecommon.model.enumuration.Role;
import com.example.moduleuser.model.dto.member.in.JoinDTO;
import com.example.moduleuser.model.dto.member.in.LoginDTO;
import com.example.moduleuser.model.dto.member.out.UserStatusResponseDTO;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.util.WebUtils;

import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDomainService {

    private final JWTTokenProvider jwtTokenProvider;

    private final AuthenticationManagerBuilder authenticationManagerBuilder;

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

    public CustomUser loginAuthenticated(LoginDTO dto) throws Exception {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(dto.userId(), dto.userPw());
        Authentication authentication =
                authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        return (CustomUser) authentication.getPrincipal();
    }

    public UserStatusResponseDTO getLoginUserStatusResponse(CustomUser authenticateUser,
                                                            HttpServletRequest request,
                                                            HttpServletResponse response) {
        String userId = authenticateUser.getUsername();

        if(checkInoAndIssueToken(userId, request, response))
            return new UserStatusResponseDTO(authenticateUser);

        return null;
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
}
