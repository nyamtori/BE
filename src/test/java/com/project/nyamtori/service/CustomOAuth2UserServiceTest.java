package com.project.nyamtori.service;

import com.project.nyamtori.domain.User;
import com.project.nyamtori.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomOAuth2UserService customOAuth2UserService;

    @Test
    void processUser_usesInternalUserId_asPrincipalName_notKakaoId() {
        long kakaoId = 999888777L;
        Long internalUserId = 42L;

        Map<String, Object> attributes = Map.of(
                "id", kakaoId,
                "properties", Map.of("nickname", "예빈")
        );

        OAuth2User rawOAuth2User = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                "id"
        );

        User savedUser = User.builder()
                .userId(internalUserId)
                .kakaoId(String.valueOf(kakaoId))
                .nickname("예빈")
                .build();

        when(userRepository.findByKakaoId(String.valueOf(kakaoId)))
                .thenReturn(Optional.of(savedUser));

        OAuth2User result = customOAuth2UserService.processUser(rawOAuth2User);

        assertThat(result.getName()).isEqualTo(String.valueOf(internalUserId));
    }
}
