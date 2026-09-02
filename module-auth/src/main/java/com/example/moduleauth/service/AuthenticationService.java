package com.example.moduleauth.service;

import com.example.moduleauth.config.user.CustomUser;
import com.example.moduleuser.model.dto.member.business.LoginUserInfo;
import com.example.moduleuser.model.dto.member.in.LoginDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    public LoginUserInfo loginAuthenticated(LoginDTO loginDTO) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginDTO.userId(), loginDTO.userPw());
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        CustomUser customUser = (CustomUser) authentication.getPrincipal();

        return new LoginUserInfo(customUser.getUserId(), customUser.getAuthorities());
    }
}
