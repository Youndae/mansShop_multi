package com.example.moduleauth.config.oauth;

import com.example.moduleauth.port.output.AuthMemberReader;
import com.example.moduleauth.port.output.AuthMemberStore;
import com.example.modulecommon.model.dto.oAuth.*;
import com.example.modulecommon.model.entity.Auth;
import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.enumuration.OAuthProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final AuthMemberReader authMemberReader;

    private final AuthMemberStore authMemberStore;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2Response oAuth2Response = null;

        if(registrationId.equals(OAuthProvider.GOOGLE.getKey()))
            oAuth2Response = new GoogleResponse(oAuth2User.getAttributes());
        else if(registrationId.equals(OAuthProvider.NAVER.getKey()))
            oAuth2Response = new NaverResponse(oAuth2User.getAttributes());
        else if(registrationId.equals(OAuthProvider.KAKAO.getKey()))
            oAuth2Response = new KakaoResponse(oAuth2User.getAttributes());

        String userId = oAuth2Response.getProvider() + "_" + oAuth2Response.getProviderId();
        Member existsData = authMemberReader.findByIdOrElseNull(userId);

        if(existsData == null) {
            Member member = OAuth2ResponseEntityConverter.toEntity(oAuth2Response, userId);
            member.addMemberAuth(new Auth().toMemberAuth());
            authMemberStore.saveMember(member);
            existsData = member;
        }else if(!existsData.getUserEmail().equals(oAuth2Response.getEmail()) || !existsData.getUserName().equals(oAuth2Response.getName())){
            existsData.setUserEmail(oAuth2Response.getEmail());
            existsData.setUserName(oAuth2Response.getName());
            authMemberStore.saveMember(existsData);
        }

        OAuth2DTO oAuth2DTO = new OAuth2DTO(existsData);

        return new CustomOAuth2User(oAuth2DTO);
    }
}
