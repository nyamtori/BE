package com.project.nyamtori.service;

import com.project.nyamtori.domain.User;
import com.project.nyamtori.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest)
            throws OAuth2AuthenticationException{
        OAuth2User oAuth2User = super.loadUser(userRequest);

        return processUser(oAuth2User);
    }

    OAuth2User processUser(OAuth2User oAuth2User) {
        Map<String, Object> attributes = oAuth2User.getAttributes();

        Long kakaoId = (Long) attributes.get("id");

        Map<String, Object> properties =
                (Map<String, Object>) attributes.get("properties");

        String nickname = (String) properties.get("nickname");

        User user = userRepository.findByKakaoId(String.valueOf(kakaoId))
                .orElseGet(() ->
                        userRepository.save(
                                User.builder()
                                        .kakaoId(String.valueOf(kakaoId))
                                        .nickname(nickname)
                                        .build()
                        )
                );

        Map<String, Object> principalAttributes = new HashMap<>(attributes);
        principalAttributes.put("userId", user.getUserId());

        return new DefaultOAuth2User(
                oAuth2User.getAuthorities(),
                principalAttributes,
                "userId"
        );
    }
}
