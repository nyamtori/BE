package com.project.nyamtori.auth;

import com.project.nyamtori.config.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException{

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        Long userId = Long.valueOf(oAuth2User.getName());

        String accessToken = jwtUtil.createAccessToken(userId);
        String refreshToken = jwtUtil.createRefreshToken(userId);

        response.setContentType("application/json");

        response.getWriter().write(
                "{\"accessToken\":\"" + accessToken +
                        "\", \"refreshToken\":\"" + refreshToken + "\"}"
        );
    }
}