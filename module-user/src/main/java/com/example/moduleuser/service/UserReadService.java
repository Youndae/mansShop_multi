package com.example.moduleuser.service;

import com.example.moduleauth.model.dto.member.UserSearchDTO;
import com.example.moduleauth.repository.MemberRepository;
import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.enumuration.Result;
import com.example.moduleuser.model.dto.member.out.UserSearchIdResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.Principal;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserReadService {

    private final MemberRepository memberRepository;

    public String checkJoinId(String userId) {
        Member member = memberRepository.findById(userId).orElse(null);

        String responseMessage = Result.DUPLICATE.getResultKey();

        if(member == null)
            responseMessage = Result.NO_DUPLICATE.getResultKey();


        return responseMessage;
    }

    public String checkNickname(String nickname, Principal principal) {

        Member member = memberRepository.findByNickname(nickname);

        String responseMessage = Result.DUPLICATE.getResultKey();

        if(member == null || (principal != null && member.getUserId().equals(principal.getName())))
            responseMessage = Result.NO_DUPLICATE.getResultKey();


        return responseMessage;
    }

    public UserSearchIdResponseDTO searchId(UserSearchDTO searchDTO) {
        String userId = memberRepository.searchId(searchDTO);
        String message = Result.OK.getResultKey();
        if(userId == null)
            message = Result.NOTFOUND.getResultKey();


        return new UserSearchIdResponseDTO(userId, message);
    }
}
