package com.example.moduleapi.service;

import com.example.moduleauth.repository.MemberRepository;
import com.example.modulecommon.customException.CustomAccessDeniedException;
import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.enumuration.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.Principal;

@Service
@RequiredArgsConstructor
@Slf4j
public class PrincipalService {

    private final MemberRepository memberRepository;

    public String extractUserId(Principal principal) {
        if(principal == null) {
            log.info("PrincipalService::extractUserId: principal is null");
            throw new CustomAccessDeniedException(ErrorCode.ACCESS_DENIED, ErrorCode.ACCESS_DENIED.getMessage());
        }

        return principal.getName();
    }

    public String extractUserIdIfExist(Principal principal) {
        if(principal == null)
            return null;

        return principal.getName();
    }

    public String getNicknameOrUsername(Principal principal) {
        if(principal == null) {
            log.info("PrincipalService::getNicknameOrUsername: principal is null");
            throw new CustomAccessDeniedException(ErrorCode.ACCESS_DENIED, ErrorCode.ACCESS_DENIED.getMessage());
        }

        Member member = memberRepository.findById(principal.getName())
                .orElseThrow(
                        () -> new CustomAccessDeniedException(ErrorCode.ACCESS_DENIED, ErrorCode.NOT_FOUND.getMessage())
                );

        return member.getNickname() == null ? member.getUserName() : member.getNickname();
    }
}
