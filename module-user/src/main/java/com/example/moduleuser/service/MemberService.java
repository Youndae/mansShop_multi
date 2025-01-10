package com.example.moduleuser.service;

import com.example.moduleauth.model.dto.member.UserSearchDTO;
import com.example.moduleauth.model.dto.member.UserSearchPwDTO;
import com.example.moduleauth.repository.MemberRepository;
import com.example.modulecommon.model.entity.Auth;
import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.enumuration.Result;
import com.example.moduleuser.model.dto.member.in.*;
import com.example.moduleuser.model.dto.member.out.UserSearchIdResponseDTO;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {

    /**
     * MemberService
     *  MemberRepository
     *  -> login, logout, issuedToken -> module-auth
     */

    private final MemberRepository memberRepository;

    private final StringRedisTemplate redisTemplate;

    private final JavaMailSender javaMailSender;

    private static final String checkDuplicatedResponseMessage = "duplicated";

    private static final String checkNoDuplicatesResponseMessage = "No duplicates";

    public String joinProc(JoinDTO joinDTO) {
        if(!checkJoinData(joinDTO))
            throw new IllegalArgumentException("Join member data check is false");

        Member memberEntity = joinDTO.toEntity();
        memberEntity.addMemberAuth(new Auth().toMemberAuth());

        memberRepository.save(memberEntity);

        return Result.OK.getResultKey();
    }

    private boolean checkJoinData(JoinDTO joinDTO) {
        Member checkId = memberRepository.findById(joinDTO.userId()).orElse(null);
        Member checkNickname = memberRepository.findByNickname(joinDTO.nickname());

        return checkId == null && checkNickname == null;
    }

    public String checkJoinId(String userId) {
        Member member = memberRepository.findById(userId).orElse(null);

        return member == null ? checkNoDuplicatesResponseMessage : checkDuplicatedResponseMessage;
    }

    public String checkNickname(String nickname, String userId) {
        Member member = memberRepository.findByNickname(nickname);
        String responseMessage = checkDuplicatedResponseMessage;

        if(member == null || member.getUserId().equals(userId))
            responseMessage = checkNoDuplicatesResponseMessage;

        return responseMessage;
    }

    public UserSearchIdResponseDTO searchId(UserSearchDTO searchDTO) {
        String userId = memberRepository.searchId(searchDTO);
        String message = userId == null ? Result.NOTFOUND.getResultKey() : Result.OK.getResultKey();

        return new UserSearchIdResponseDTO(userId, message);
    }

    public String searchPw(UserSearchPwDTO searchDTO) {
        Long count = memberRepository.findByPassword(searchDTO);

        if(count == 0)
            return Result.NOTFOUND.getResultKey();

        Random ran = new Random();
        int certificationNo = ran.nextInt(899999) + 100001;

        try {
            ValueOperations<String, String> stringValueOperations = redisTemplate.opsForValue();
            stringValueOperations.set(searchDTO.userId(),
                                    String.valueOf(certificationNo),
                                    6L,
                                    TimeUnit.MINUTES
                            );
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

        String msgOfEmail = "";
        msgOfEmail += "<div style='margin:20px;'>";
        msgOfEmail += "<h1> 안녕하세요 test 입니다. </h1>";
        msgOfEmail += "<br/>";
        msgOfEmail += "<p>아래 코드를 입력해주세요</p>";
        msgOfEmail += "<br/>";
        msgOfEmail += "<div align='center' style='border:1px solid black; font-family:verdana';>";
        msgOfEmail += "<h3 style='color:blue;'>비밀번호 변경 인증코드입니다.</h3>";
        msgOfEmail += "<div style='font-size:130%'>";
        msgOfEmail += "CODE : <strong>";
        msgOfEmail += certificationNo + "</strong></div><br/>";
        msgOfEmail += "</div>";

        message.setText(msgOfEmail, "UTF-8", "html");

        return message;
    }

    public String checkCertificationNo(UserCertificationDTO certificationDTO) {
        String result = null;

        try {
            ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
            result = valueOperations.get(certificationDTO.userId());
        }catch (Exception e) {
            log.warn("certificationCheck Exception");
            e.printStackTrace();
            return Result.ERROR.getResultKey();
        }

        return certificationDTO.certification().equals(result) ?
                Result.OK.getResultKey() : Result.FAIL.getResultKey();
    }

    public String resetPw(UserResetPwDTO resetDTO) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        String certificationValue = valueOperations.get(resetDTO.userId());
        redisTemplate.delete(resetDTO.userId());

        if(!certificationValue.equals(resetDTO.certification()))
            return Result.FAIL.getResultKey();

        Member member = memberRepository.findById(resetDTO.userId()).orElseThrow(IllegalAccessError::new);
        member.setUserPw(resetDTO.userPw());
        memberRepository.save(member);

        return Result.OK.getResultKey();
    }
}
