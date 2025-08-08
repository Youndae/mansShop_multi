package com.example.moduleuser.usecase.admin;

import com.example.moduleuser.model.dto.admin.out.AdminMemberDTO;
import com.example.moduleuser.model.dto.admin.page.AdminMemberPageDTO;
import com.example.moduleuser.service.UserDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminMemberReadUseCase {

    private final UserDataService userDataService;

    public Page<AdminMemberDTO> getAdminMemberList(AdminMemberPageDTO pageDTO) {
        return userDataService.getAdminMemberPageList(pageDTO);
    }
}
