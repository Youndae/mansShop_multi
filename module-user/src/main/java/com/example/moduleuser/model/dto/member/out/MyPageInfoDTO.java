package com.example.moduleuser.model.dto.member.out;

import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.utils.PhoneNumberUtils;

public record MyPageInfoDTO(
        String nickname,
        String phone,
        String mailPrefix,
        String mailSuffix,
        String mailType
) {

    public MyPageInfoDTO(Member member, String[] splitMail, String mailType) {
        this(
                member.getNickname(),
                PhoneNumberUtils.unformat(member.getPhone()),
                splitMail[0],
                splitMail[1],
                mailType
        );
    }
}
