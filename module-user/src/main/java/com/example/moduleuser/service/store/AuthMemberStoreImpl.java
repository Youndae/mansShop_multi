package com.example.moduleuser.service.store;

import com.example.moduleauth.port.output.AuthMemberStore;
import com.example.modulecommon.model.entity.Member;
import com.example.moduleuser.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthMemberStoreImpl implements AuthMemberStore {

    private final MemberRepository memberRepository;

    @Override
    public void saveMember(Member member) {
        memberRepository.save(member);
    }
}
