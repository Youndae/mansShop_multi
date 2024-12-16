package com.example.moduleauth.repository;

import com.example.modulecommon.model.entity.Member;

public interface MemberDSLRepository {

    Member findByLocalUserId(String userId);
}
