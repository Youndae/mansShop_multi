package com.example.moduleauth.port.output;

import com.example.modulecommon.model.entity.Member;

public interface AuthMemberStore {

    void saveMember(Member member);
}
