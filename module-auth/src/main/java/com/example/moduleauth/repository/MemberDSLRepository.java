package com.example.moduleauth.repository;

import com.example.moduleauth.model.dto.member.UserSearchDTO;
import com.example.moduleauth.model.dto.member.UserSearchPwDTO;
import com.example.modulecommon.model.entity.Member;

public interface MemberDSLRepository {

    Member findByLocalUserId(String userId);

    String searchId(UserSearchDTO searchDTO);

    Long findByPassword(UserSearchPwDTO searchDTO);
}
