package com.example.moduleauth.service;

import com.example.moduleauth.model.dto.member.UserSearchDTO;
import com.example.moduleauth.model.dto.member.UserSearchPwDTO;
import com.example.moduleauth.repository.MemberRepository;
import com.example.modulecommon.model.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberReader {

    private final MemberRepository memberRepository;

    public Member getMemberByUserId(String userId) {
        return memberRepository.findByUserId(userId);
    }

    public Member getMemberById(String userId) {
        return memberRepository.findById(userId).orElse(null);
    }

    public Member getLocalMemberByUserId(String userId) {
        return memberRepository.findByLocalUserId(userId);
    }

    public String getSearchUserId(UserSearchDTO searchDTO) {
        return memberRepository.searchId(searchDTO);
    }

    public Long countMatchingBySearchPwDTO(UserSearchPwDTO searchPwDTO) {
        return memberRepository.findByPassword(searchPwDTO);
    }

    public Member getMemberByNickname(String nickname) {
        return memberRepository.findByNickname(nickname);
    }
}
