package com.example.moduleuser.usecase;

import com.example.moduleauth.model.dto.member.UserSearchDTO;
import com.example.moduleuser.model.dto.member.out.UserSearchIdResponseDTO;
import com.example.moduleuser.service.UserReadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.Principal;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserReadUseCase {

    private final UserReadService userReadService;

    public String checkJoinUserId(String userId) {
        return userReadService.checkJoinId(userId);
    }

    public String checkNickname(String nickname, Principal principal) {
        return userReadService.checkNickname(nickname, principal);
    }

    public UserSearchIdResponseDTO searchId(UserSearchDTO searchDTO) {
        return userReadService.searchId(searchDTO);
    }
}
