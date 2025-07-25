package com.example.moduleauth.port.output;

import com.example.modulecommon.model.entity.Member;

public interface AuthMemberReader {

    Member findByUserId(String userId);

    Member findByIdOrElseNull(String userId);

    Member findByLocalUserId(String userId);
}
