package com.example.moduleapi.service;

import com.example.modulecommon.customException.CustomAccessDeniedException;
import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.enumuration.ErrorCode;
import com.example.moduleuser.service.reader.MemberReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.Principal;

@Service
@RequiredArgsConstructor
@Slf4j
public class PrincipalService {

    private final MemberReader memberReader;

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

        Member member = memberReader.getMemberByUserIdOrElseAccessDenied(principal.getName());

        return member.getNickname() == null ? member.getUserName() : member.getNickname();
    }
}
