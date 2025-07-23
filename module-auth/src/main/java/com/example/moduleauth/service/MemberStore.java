package com.example.moduleauth.service;

import com.example.moduleauth.repository.AuthRepository;
import com.example.moduleauth.repository.MemberRepository;
import com.example.modulecommon.model.entity.Auth;
import com.example.modulecommon.model.entity.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberStore {

    private final MemberRepository memberRepository;

    private final AuthRepository authRepository;

    public void saveMember(Member member) {
        memberRepository.save(member);
    }

    public void saveMemberAndAuth(Member member) {
        List<Auth> auths = member.getAuths();

        memberRepository.save(member);
        authRepository.saveAll(auths);
    }
}
