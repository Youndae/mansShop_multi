package com.example.moduleuser.service.reader;

import com.example.moduleauth.port.output.AuthMemberReader;
import com.example.modulecommon.model.entity.Member;
import com.example.moduleuser.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthMemberReaderImpl implements AuthMemberReader {

    private final MemberRepository memberRepository;

    @Override
    public Member findByUserId(String userId) {
        return memberRepository.findByUserId(userId);
    }

    @Override
    public Member findByIdOrElseNull(String userId) {
        return memberRepository.findById(userId).orElse(null);
    }

    @Override
    public Member findByLocalUserId(String userId) {
        return memberRepository.findByLocalUserId(userId);
    }
}
