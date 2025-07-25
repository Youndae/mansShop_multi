package com.example.moduleauth.config.user;

import com.example.moduleauth.port.output.AuthMemberReader;
import com.example.modulecommon.customException.CustomBadCredentialsException;
import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.enumuration.ErrorCode;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    @Setter(onMethod_ = {@Autowired})
    private AuthMemberReader authMemberReader;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member member = authMemberReader.findByLocalUserId(username);

        if(member == null)
            throw new CustomBadCredentialsException(ErrorCode.BAD_CREDENTIALS, ErrorCode.BAD_CREDENTIALS.getMessage());

        return new CustomUser(member);
    }
}
