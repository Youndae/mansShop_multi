package com.example.moduleauth.config.security;

import com.example.moduleauth.config.jwt.JWTAuthorizationFilter;
import com.example.moduleauth.config.oauth.CustomOAuth2SuccessHandler;
import com.example.moduleauth.config.oauth.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity(securedEnabled = true)
@Slf4j
public class SecurityConfig {

    private final CorsFilter corsFilter;

    private final CustomOAuth2UserService customOAuth2UserService;

    private final CustomOAuth2SuccessHandler customOAuth2SuccessHandler;

    private final JWTAuthorizationFilter jwtAuthorizationFilter;

    @Bean
    @Profile({"prod", "dev"})
    protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth ->
                        auth
                                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                                .requestMatchers("/api/my-page/**").hasRole("MEMBER")
                                .requestMatchers("/api/login",
                                        "/api/join",
                                        "/api/search-id",
                                        "/api/search-pw"
                                ).anonymous()
                                .requestMatchers("/api/logout").authenticated()
                                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").hasRole("ADMIN")
                                .anyRequest().permitAll()
                );

        http
                .sessionManagement(config ->
                        config.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilter(corsFilter)
                .addFilterBefore(
                        jwtAuthorizationFilter,
                        BasicAuthenticationFilter.class
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable);

        http
                .oauth2Login((oauth2) ->
                        oauth2
                                .loginPage("/login")
                                .userInfoEndpoint((userInfoEndpointConfig) ->
                                        userInfoEndpointConfig.userService(customOAuth2UserService)
                                )
                                .successHandler(customOAuth2SuccessHandler)
                );

        return http.build();
    }

    @Bean
    @Profile("test")
    protected SecurityFilterChain devSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth ->
                                auth
                                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                                        .requestMatchers("/api/my-page/**").hasRole("MEMBER")
                                        .requestMatchers("/api/login",
                                                "/api/join",
                                                "/api/search-id",
                                                "/api/search-pw"
                                        ).anonymous()
                                        .requestMatchers("/api/logout").authenticated()
                                        .requestMatchers(
                                                "/swagger-ui/**", "/v3/api-docs"
                                        ).permitAll()
                                        .anyRequest().permitAll()
                );

        http
                .sessionManagement(config ->
                        config.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilter(corsFilter)
                .addFilterBefore(
                        jwtAuthorizationFilter,
                        BasicAuthenticationFilter.class
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable);

        return http.build();
    }
}
