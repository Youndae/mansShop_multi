package com.example.moduleuser.model.enumuration;

import com.example.modulecommon.utils.CommonSearchType;
import com.example.modulecommon.utils.CommonSearchTypeFinder;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum AdminMemberSearchType implements CommonSearchType {
    USER_ID("userId"),
    USER_NAME("userName"),
    NICKNAME("nickname");

    private final String value;

    public static AdminMemberSearchType from (String value) {
        return CommonSearchTypeFinder.from(values(), value, AdminMemberSearchType.class);
    }

    @Override
    public String value() {
        return value;
    }
}
