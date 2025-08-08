package com.example.moduleuser.usecase;

import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.enumuration.Result;
import com.example.moduleuser.model.dto.member.in.UserSearchDTO;
import com.example.moduleuser.model.dto.member.out.MyPageInfoDTO;
import com.example.moduleuser.model.dto.member.out.UserSearchIdResponseDTO;
import com.example.moduleuser.service.UserDataService;
import com.example.moduleuser.service.UserDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.Principal;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserReadUseCase {

    private final UserDataService userDataService;
    private final UserDomainService userDomainService;

    public String checkJoinUserId(String userId) {
        Member member = userDataService.getMemberByUserIdOrElseNull(userId);

        if(member == null)
            return Result.NO_DUPLICATE.getResultKey();

        return Result.DUPLICATE.getResultKey();
    }

    public String checkNickname(String nickname, Principal principal) {
        Member member = userDataService.getMemberByNickname(nickname);

        if(member == null || (principal != null && member.getUserId().equals(principal.getName())))
            return Result.NO_DUPLICATE.getResultKey();

        return Result.DUPLICATE.getResultKey();
    }

    public UserSearchIdResponseDTO searchId(UserSearchDTO searchDTO) {
        String userId = userDataService.getSearchUserId(searchDTO);
        String message = Result.OK.getResultKey();
        if(userId == null)
            message = Result.NOTFOUND.getResultKey();

        return new UserSearchIdResponseDTO(userId, message);
    }

    public MyPageInfoDTO getMyPageUserInfo(String userId) {
        Member member = userDataService.getMemberByUserIdOrElseIllegal(userId);

        return userDomainService.createMyPageInfoDTO(member);
    }
}
