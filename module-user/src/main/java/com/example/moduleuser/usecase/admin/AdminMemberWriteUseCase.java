package com.example.moduleuser.usecase.admin;

import com.example.modulecommon.model.entity.Member;
import com.example.moduleuser.model.dto.admin.in.AdminPostPointDTO;
import com.example.moduleuser.service.UserDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminMemberWriteUseCase {

    private final UserDataService userDataService;

    public void postPoint(AdminPostPointDTO pointDTO) {
        Member member = userDataService.getMemberByUserIdOrElseIllegal(pointDTO.userId());
        member.setMemberPoint(pointDTO.point());
        userDataService.saveMember(member);
    }
}
