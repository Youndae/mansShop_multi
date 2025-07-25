package com.example.moduleuser.service.reader;

import com.example.modulecommon.customException.CustomAccessDeniedException;
import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.enumuration.ErrorCode;
import com.example.moduleuser.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberReader {

    private final MemberRepository memberRepository;

    public Member getMemberByUserIdOrElseNull(String userId) {
        return memberRepository.findById(userId).orElse(null);
    }

    public Member getMemberByUserIdOrElseIllegalArgs(String userId) {
        return memberRepository.findById(userId).orElseThrow(IllegalArgumentException::new);
    }

    public Member getMemberByUserIdOrElseAccessDenied(String userId) {
        return memberRepository.findById(userId)
                .orElseThrow(() ->
                        new CustomAccessDeniedException(
                                ErrorCode.ACCESS_DENIED,
                                ErrorCode.ACCESS_DENIED.getMessage()
                        )
                );
    }
}
