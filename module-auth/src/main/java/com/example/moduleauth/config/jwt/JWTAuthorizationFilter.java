package com.example.moduleauth.config.jwt;

import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.example.moduleauthapi.model.dto.TokenVerifyResult;
import com.example.moduleauthapi.service.JWTTokenProvider;
import com.example.moduleauthapi.service.JWTTokenService;
import com.example.modulecommon.model.enumuration.ErrorCode;
import com.example.moduleconfig.properties.CookieProperties;
import com.example.moduleconfig.properties.TokenProperties;
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

    private final JWTTokenProvider jwtTokenProvider;

    private final JWTTokenService jwtTokenService;

    private final TokenProperties tokenProperties;

    private final CookieProperties cookieProperties;


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        // reissue 경로 요청 시 RefreshToken이 없다면 잘못된 요청.
        if("/api/reissue".equals(request.getRequestURI())) {
            Cookie refreshToken = WebUtils.getCookie(request, tokenProperties.getRefresh().getHeader());

            if(refreshToken == null){
                jwtTokenService.setExceptionResponse(ErrorCode.BAD_REQUEST, response);
                return;
            }

            chain.doFilter(request, response);
            return;
        }

        String accessToken = request.getHeader(tokenProperties.getAccess().getHeader());
        Cookie inoToken = WebUtils.getCookie(request, cookieProperties.getIno().getHeader());

        if(inoToken != null && accessToken != null && jwtTokenProvider.checkTokenPrefix(accessToken)) {
            String accessTokenValue = accessToken.replace(tokenProperties.getPrefix(), "");

            try {
                TokenVerifyResult verifyResult = jwtTokenProvider.verifyAccessToken(accessTokenValue);

                Collection<? extends GrantedAuthority> authorities = verifyResult.role().getAuthorities();

                Authentication authentication = new UsernamePasswordAuthenticationToken(verifyResult.userId(), null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch(TokenExpiredException e) {
                jwtTokenService.setExceptionResponse(ErrorCode.TOKEN_EXPIRED, response);
                return;
            } catch(JWTDecodeException e) {
                jwtTokenService.setExceptionResponse(ErrorCode.TOKEN_INVALID, response);
                return;
            }
        }

        chain.doFilter(request, response);
    }
}
