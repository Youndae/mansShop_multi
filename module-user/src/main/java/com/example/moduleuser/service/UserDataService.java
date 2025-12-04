package com.example.moduleuser.service;

import com.example.moduleauthapi.service.JWTTokenProvider;
import com.example.modulecommon.customException.CustomAccessDeniedException;
import com.example.modulecommon.model.entity.Auth;
import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.enumuration.ErrorCode;
import com.example.modulecommon.model.enumuration.Role;
import com.example.moduleuser.model.dto.admin.out.AdminMemberDTO;
import com.example.moduleuser.model.dto.admin.page.AdminMemberPageDTO;
import com.example.moduleuser.model.dto.member.in.LogoutDTO;
import com.example.moduleuser.model.dto.member.in.UserSearchDTO;
import com.example.moduleuser.model.dto.member.in.UserSearchPwDTO;
import com.example.moduleuser.repository.AuthRepository;
import com.example.moduleuser.repository.MemberRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    public void saveMemberAndAuthToJoin(Member member) throws Exception {
        Auth auth = Auth.builder()
                        .auth(Role.MEMBER.getKey())
                        .build();
        member.addMemberAuth(auth);
        saveMember(member);
        authRepository.save(auth);
    }

    public Member getMemberByLocalUserId(String userId) {
        return memberRepository.findByLocalUserId(userId);
    }

    public Member getMemberByUserIdOrElseIllegal(String userId) {
        return memberRepository.findById(userId).orElseThrow(IllegalArgumentException::new);
    }

    public Member getMemberByUserIdOrElseNull(String userId){
        return memberRepository.findById(userId).orElse(null);
    }

    public Member getMemberByUserIdOrElseAccessDenied(String userId) {
        return memberRepository.findById(userId)
                .orElseThrow(() ->
                        new CustomAccessDeniedException(
                                ErrorCode.FORBIDDEN,
                                ErrorCode.FORBIDDEN.getMessage()
                        )
                );
    }

    public Member getMemberByUserIdFetchAuthsOrElseIllegal(String userId) {
        Member member = memberRepository.findByUserId(userId);

        if(member == null)
            throw new IllegalArgumentException("Member fetch auths Data is null");

        return member;
    }

    public Long countMatchingBySearchPwDTO(UserSearchPwDTO searchDTO) {
        return memberRepository.findByPassword(searchDTO);
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

    public void deleteTokenAndCookieByLogout(LogoutDTO dto, HttpServletResponse response) {
        try{
            jwtTokenProvider.deleteRedisDataAndCookie(dto.userId(), dto.inoValue(), response);
        }catch (Exception e) {
            log.warn("logout delete Data Exception. {}", e.getMessage());
            throw new RuntimeException("logout delete Data Exception.", e);
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

    public Page<AdminMemberDTO> getAdminMemberPageList(AdminMemberPageDTO pageDTO) {
        Pageable pageable = PageRequest.of(pageDTO.page() - 1,
                                        pageDTO.amount(),
                                        Sort.by("createdAt").descending()
                                );

        return memberRepository.findMember(pageDTO, pageable);
    }
}
