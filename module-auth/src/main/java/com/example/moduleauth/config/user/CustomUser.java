package com.example.moduleauth.config.user;

import com.example.modulecommon.model.entity.Member;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
import java.util.stream.Collectors;

@Getter
@Slf4j
public class CustomUser extends User implements CustomUserDetails {

    private Member member;

    public CustomUser(String username, String password, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
    }

    public CustomUser(Member member) {
        super(
                member.getUserId(),
                member.getUserPw(),
                member.getAuths()
                        .stream()
                        .map(auth ->
                                new SimpleGrantedAuthority(auth.getAuth())
                        )
                        .collect(Collectors.toList())
        );
    }

    @Override
    public String getUserId() {
        return member.getUserId();
    }
}
