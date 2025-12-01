package com.example.moduleuser.usecase;

import com.example.modulecommon.customException.CustomDuplicateException;
import com.example.modulecommon.customException.CustomNotFoundException;
import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.enumuration.ErrorCode;
import com.example.moduleuser.model.dto.member.in.UserSearchDTO;
import com.example.moduleuser.model.dto.member.out.MyPageInfoDTO;
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

    public void checkJoinUserId(String userId) {
        Member member = userDataService.getMemberByUserIdOrElseNull(userId);

        if(member != null)
            throw new CustomDuplicateException(ErrorCode.CONFLICT, ErrorCode.CONFLICT.getMessage());
    }

    public void checkNickname(String nickname, Principal principal) {
        Member member = userDataService.getMemberByNickname(nickname);

        if(isConflict(member, principal))
            throw new CustomDuplicateException(ErrorCode.CONFLICT, ErrorCode.CONFLICT.getMessage());
    }

    private boolean isConflict(Member member, Principal principal) {
        if(member == null)
            return false;
        if (principal == null)
            return true;

        return !member.getUserId().equals(principal.getName());
    }

    public String searchId(UserSearchDTO searchDTO) {
        String userId = userDataService.getSearchUserId(searchDTO);

        if(userId == null)
            throw new CustomNotFoundException(ErrorCode.BAD_REQUEST, ErrorCode.BAD_REQUEST.getMessage());

        return userId;
    }

    public MyPageInfoDTO getMyPageUserInfo(String userId) {
        Member member = userDataService.getMemberByUserIdOrElseIllegal(userId);

        return userDomainService.createMyPageInfoDTO(member);
    }
}
