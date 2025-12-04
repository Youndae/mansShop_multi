package com.example.moduleauth.config.jwt;

import com.example.moduleauth.config.oauth.CustomOAuth2User;
import com.example.moduleauth.config.user.CustomUser;
import com.example.moduleauth.config.user.CustomUserDetails;
import com.example.moduleauthapi.service.JWTTokenProvider;
import com.example.moduleauthapi.service.JWTTokenService;
import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.enumuration.Result;
import com.example.moduleconfig.properties.CookieProperties;
import com.example.moduleconfig.properties.TokenProperties;
import com.example.moduleuser.service.UserDataService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import java.io.IOException;
import java.util.Collection;

@Component
@RequiredArgsConstructor
@Slf4j
public class JWTAuthorizationFilter extends OncePerRequestFilter {

    private final UserDataService userDataService;

    private final JWTTokenProvider jwtTokenProvider;

    private final JWTTokenService jwtTokenService;

    private final TokenProperties tokenProperties;

    private final CookieProperties cookieProperties;


    @Override
    protected void doFilterInternal(HttpServletRequest request
            , HttpServletResponse response
            , FilterChain chain) throws ServletException, IOException {
        String accessToken = request.getHeader(tokenProperties.getAccess().getHeader());
        Cookie refreshToken = WebUtils.getCookie(request, tokenProperties.getRefresh().getHeader());
        Cookie inoToken = WebUtils.getCookie(request, cookieProperties.getIno().getHeader());
        String username = null; // Authentication 객체 생성 시 필요한 사용자 아이디

        if(inoToken != null){
            String inoValue = inoToken.getValue();
            if(accessToken != null && refreshToken != null) {
                String refreshTokenValue = refreshToken.getValue();
                String accessTokenValue = accessToken.replace(tokenProperties.getPrefix(), "");

                if(!jwtTokenProvider.checkTokenPrefix(accessToken)
                        || !jwtTokenProvider.checkTokenPrefix(refreshTokenValue)){
                    chain.doFilter(request, response);
                    return;
                }else {
                    String claimByAccessToken = jwtTokenProvider.verifyAccessToken(accessTokenValue, inoValue);

                    if(claimByAccessToken.equals(Result.WRONG_TOKEN.getResultKey())
                            || claimByAccessToken.equals(Result.TOKEN_STEALING.getResultKey())){
                        jwtTokenService.deleteCookieAndThrowException(response);
                        return;
                    }else if(claimByAccessToken.equals(Result.TOKEN_EXPIRATION.getResultKey())){
                        if(request.getRequestURI().equals("/api/reissue")) {
                            chain.doFilter(request, response);
                        }else
                            jwtTokenService.tokenExpirationResponse(response);

                        return;
                    }else {
                        username = claimByAccessToken;
                    }
                }
            }else if(accessToken != null && refreshToken == null){
                String decodeTokenClaim = jwtTokenProvider.decodeToken(accessToken.replace(tokenProperties.getPrefix(), ""));

                jwtTokenService.deleteTokenAndCookieAndThrowException(decodeTokenClaim, inoValue, response);
                return;
            }else {
                chain.doFilter(request, response);
                return;
            }
        }

        if(username != null){
            Member memberEntity = userDataService.getMemberByUserIdFetchAuthsOrElseIllegal(username);
            String userId;
            Collection<? extends GrantedAuthority> authorities;
            CustomUserDetails userDetails;

            if(memberEntity.getProvider().equals("local"))
                userDetails = new CustomUser(memberEntity);
            else
                userDetails = new CustomOAuth2User(
                        memberEntity.toOAuth2DTOUseFilter()
                );

            userId = userDetails.getUserId();
            authorities = userDetails.getAuthorities();

            Authentication authentication =
                    new UsernamePasswordAuthenticationToken(userId, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        chain.doFilter(request, response);
    }
}
