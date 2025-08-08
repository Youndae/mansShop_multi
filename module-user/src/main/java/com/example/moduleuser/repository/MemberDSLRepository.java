package com.example.moduleuser.repository;

import com.example.modulecommon.model.entity.Member;
import com.example.moduleuser.model.dto.admin.out.AdminMemberDTO;
import com.example.moduleuser.model.dto.admin.page.AdminMemberPageDTO;
import com.example.moduleuser.model.dto.member.in.UserSearchDTO;
import com.example.moduleuser.model.dto.member.in.UserSearchPwDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MemberDSLRepository {

    Member findByLocalUserId(String userId);

    Member findByUserId(String userId);

    String searchId(UserSearchDTO searchDTO);

    Long findByPassword(UserSearchPwDTO searchDTO);

    Page<AdminMemberDTO> findMember(AdminMemberPageDTO pageDTO, Pageable pageable);
}
