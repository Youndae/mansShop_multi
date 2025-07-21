package com.example.modulecommon.model.dto;

import com.example.modulecommon.model.entity.Auth;
import com.example.modulecommon.model.entity.Member;

import java.util.List;

public record MemberAndAuthFixtureDTO(
        List<Member> memberList,
        List<Auth> authList
) {
}
