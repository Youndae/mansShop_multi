package com.example.moduleuser.service;

import com.example.moduleauth.config.jwt.JWTTokenProvider;
import com.example.moduleauth.config.user.CustomUser;
import com.example.moduleauth.model.dto.member.UserSearchPwDTO;
import com.example.moduleauth.repository.AuthRepository;
import com.example.moduleauth.repository.MemberRepository;
import com.example.modulecommon.customException.CustomAccessDeniedException;
import com.example.modulecommon.customException.CustomBadCredentialsException;
import com.example.modulecommon.customException.CustomTokenStealingException;
import com.example.modulecommon.model.entity.Auth;
import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.enumuration.ErrorCode;
import com.example.modulecommon.model.enumuration.Result;
import com.example.modulecommon.model.enumuration.Role;
import com.example.moduleuser.model.dto.member.in.*;
import com.example.moduleuser.model.dto.member.out.UserStatusResponseDTO;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.util.WebUtils;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserWriteService {

    private final MemberRepository memberRepository;

    private final AuthRepository authRepository;

    private final JWTTokenProvider jwtTokenProvider;

    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    private final StringRedisTemplate redisTemplate;

    private final JavaMailSender javaMailSender;

    @Value("#{jwt['token.temporary.header']}")
    private String temporaryHeader;

    @Value("#{jwt['cookie.ino.header']}")
    private String inoHeader;



    public String joinProc(JoinDTO joinDTO) {

        Member memberEntity = joinDTO.toEntity();
        Auth auth = Auth.builder()
                .auth(Role.MEMBER.getKey())
                .build();
        memberEntity.addMemberAuth(auth);

        memberRepository.save(memberEntity);
        authRepository.save(auth);

        return Result.OK.getResultKey();
    }

    public UserStatusResponseDTO loginProc(LoginDTO dto, HttpServletRequest request, HttpServletResponse response) {
        try {
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(dto.userId(), dto.userPw());
            Authentication authentication =
                    authenticationManagerBuilder.getObject().authenticate(authenticationToken);
            CustomUser customUser = (CustomUser) authentication.getPrincipal();
            String userId = customUser.getUsername();

            if(userId != null)
                if (checkInoAndIssueToken(userId, request, response))
                    return new UserStatusResponseDTO(customUser);
        }catch (Exception e) {
            throw new CustomBadCredentialsException(ErrorCode.BAD_CREDENTIALS, ErrorCode.BAD_CREDENTIALS.getMessage());
        }

        return null;
    }

    public String logoutProc(LogoutDTO dto, HttpServletResponse response) {

        try{
            jwtTokenProvider.deleteRedisDataAndCookie(dto.userId(), dto.inoValue(), response);

            return Result.OK.getResultKey();
        }catch (Exception e) {
            log.warn("logout delete Data Exception");
            e.printStackTrace();
            return Result.FAIL.getResultKey();
        }
    }

    public String oAuthUserIssueToken(HttpServletRequest request, HttpServletResponse response) {

        Cookie temporaryCookie = WebUtils.getCookie(request, temporaryHeader);

        if(temporaryCookie == null)
            throw new CustomBadCredentialsException(ErrorCode.BAD_CREDENTIALS, ErrorCode.BAD_CREDENTIALS.getMessage());

        String temporaryValue = temporaryCookie.getValue();
        String temporaryClaimByUserId = jwtTokenProvider.verifyTemporaryToken(temporaryValue);

        if(temporaryClaimByUserId.equals(Result.WRONG_TOKEN.getResultKey())
                || temporaryClaimByUserId.equals(Result.TOKEN_EXPIRATION.getResultKey()))
            throw new CustomAccessDeniedException(ErrorCode.ACCESS_DENIED, ErrorCode.ACCESS_DENIED.getMessage());
        else if(temporaryClaimByUserId.equals(Result.TOKEN_STEALING.getResultKey()))
            throw new CustomTokenStealingException(ErrorCode.TOKEN_STEALING, ErrorCode.TOKEN_STEALING.getMessage());

        jwtTokenProvider.deleteTemporaryTokenAndCookie(temporaryClaimByUserId, response);

        if(checkInoAndIssueToken(temporaryClaimByUserId, request, response))
            return Result.OK.getResultKey();
        else
            throw new CustomBadCredentialsException(ErrorCode.BAD_CREDENTIALS, ErrorCode.BAD_CREDENTIALS.getMessage());
    }

    private boolean checkInoAndIssueToken(String userId, HttpServletRequest request, HttpServletResponse response){
        Cookie inoCookie = WebUtils.getCookie(request, inoHeader);

        if(inoCookie == null)
            jwtTokenProvider.issueAllTokens(userId, response);
        else
            jwtTokenProvider.issueTokens(userId, inoCookie.getValue(), response);

        return true;
    }

    public String searchPw(UserSearchPwDTO searchDTO) {

        Long count = memberRepository.findByPassword(searchDTO);

        if(count == 0)
            return Result.NOTFOUND.getResultKey();

        Random ran = new Random();
        int certificationNo = ran.nextInt(899999) + 100001;

        try{
            ValueOperations<String, String> stringValueOperations = redisTemplate.opsForValue();;
            stringValueOperations.set(searchDTO.userId(), String.valueOf(certificationNo), 6L, TimeUnit.MINUTES);

            MimeMessage mailForm = createEmailForm(searchDTO.userEmail(), certificationNo);
            javaMailSender.send(mailForm);

            return Result.OK.getResultKey();
        }catch (Exception e) {
            log.warn("mail Send Exception");
            e.printStackTrace();
            return Result.FAIL.getResultKey();
        }

    }

    public MimeMessage createEmailForm(String userEmail, int certificationNo) throws MessagingException {
        String mailTitle = "Man's Shop 비밀번호 변경";

        MimeMessage message = javaMailSender.createMimeMessage();
        message.addRecipients(MimeMessage.RecipientType.TO, userEmail);
        message.setSubject(mailTitle);

        String msgOfEmail="";
        msgOfEmail += "<div style='margin:20px;'>";
        msgOfEmail += "<h1> 안녕하세요 test 입니다. </h1>";
        msgOfEmail += "<br>";
        msgOfEmail += "<p>아래 코드를 입력해주세요<p>";
        msgOfEmail += "<br>";
        msgOfEmail += "<p>감사합니다.<p>";
        msgOfEmail += "<br>";
        msgOfEmail += "<div align='center' style='border:1px solid black; font-family:verdana';>";
        msgOfEmail += "<h3 style='color:blue;'>회원가입 인증 코드입니다.</h3>";
        msgOfEmail += "<div style='font-size:130%'>";
        msgOfEmail += "CODE : <strong>";
        msgOfEmail += certificationNo + "</strong><div><br/> ";
        msgOfEmail += "</div>";

        message.setText(msgOfEmail, "UTF-8", "html");

        return message;
    }

    public String checkCertificationNo(UserCertificationDTO certificationDTO) {
        String result = null;

        try{
            ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
            result = valueOperations.get(certificationDTO.userId());
        }catch (Exception e) {
            log.warn("certificationCheck Exception");
            e.printStackTrace();
            return Result.ERROR.getResultKey();
        }

        if(certificationDTO.certification().equals(result))
            return Result.OK.getResultKey();

        return Result.FAIL.getResultKey();
    }

    public String resetPw(UserResetPwDTO resetDTO) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        String certificationValue = valueOperations.get(resetDTO.userId());
        redisTemplate.delete(resetDTO.userId());

        if(certificationValue == null || !certificationValue.equals(resetDTO.certification()))
            return Result.FAIL.getResultKey();

        Member member = memberRepository.findById(resetDTO.userId()).orElseThrow(IllegalArgumentException::new);
        member.setUserPw(resetDTO.userPw());

        memberRepository.save(member);

        return Result.OK.getResultKey();
    }
}
