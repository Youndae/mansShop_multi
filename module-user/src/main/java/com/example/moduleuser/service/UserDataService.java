package com.example.moduleuser.service;

import com.example.moduleauth.config.jwt.JWTTokenProvider;
import com.example.moduleauth.model.dto.member.UserSearchDTO;
import com.example.moduleauth.model.dto.member.UserSearchPwDTO;
import com.example.modulecommon.model.entity.Auth;
import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.enumuration.Result;
import com.example.modulecommon.model.enumuration.Role;
import com.example.moduleuser.model.dto.member.in.LogoutDTO;
import com.example.moduleuser.repository.AuthRepository;
import com.example.moduleuser.repository.MemberRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDataService {

    private final JWTTokenProvider jwtTokenProvider;

    private final StringRedisTemplate redisTemplate;

    private final MemberRepository memberRepository;

    private final AuthRepository authRepository;

    public void saveMemberAndAuthToJoin(Member member) {
        Auth auth = Auth.builder()
                        .auth(Role.MEMBER.getRole())
                        .build();
        member.addMemberAuth(auth);
        saveMember(member);
        authRepository.save(auth);
    }

    public Long countMatchingBySearchPwDTO(UserSearchPwDTO searchDTO) {
        return memberRepository.findByPassword(searchDTO);
    }

    public Member getMemberByIdOrElseNull(String userId){
        return memberRepository.findById(userId).orElse(null);
    }

    public void saveMember(Member member) {
        memberRepository.save(member);
    }

    public Member getMemberByNickname(String nickname) {
        return memberRepository.findByNickname(nickname);
    }

    public String getSearchUserId(UserSearchDTO searchDTO) {
        return memberRepository.searchId(searchDTO);
    }

    public void deleteTemporaryTokenAndCookie(String temporaryClaim, HttpServletResponse response) {
        jwtTokenProvider.deleteTemporaryTokenAndCookie(temporaryClaim, response);
    }

    public String deleteTokenAndCookieByLogout(LogoutDTO dto, HttpServletResponse response) {
        try{
            jwtTokenProvider.deleteRedisDataAndCookie(dto.userId(), dto.inoValue(), response);

            return Result.OK.getResultKey();
        }catch (Exception e) {
            log.warn("logout delete Data Exception");
            e.printStackTrace();
            return Result.FAIL.getResultKey();
        }
    }

    public void saveCertificationNumberToRedis(UserSearchPwDTO searchDTO, int certificationNo) throws Exception {
        ValueOperations<String, String> stringValueOperations = redisTemplate.opsForValue();;
        stringValueOperations.set(searchDTO.userId(), String.valueOf(certificationNo), 6L, TimeUnit.MINUTES);
    }

    public String getCertificationNumberFromRedis(String userId) throws Exception {
        ValueOperations<String, String> valueOps = redisTemplate.opsForValue();

        return valueOps.get(userId);
    }

    public void deleteCertificationNumberFromRedis(String userId) throws Exception {
        redisTemplate.delete(userId);
    }
}
