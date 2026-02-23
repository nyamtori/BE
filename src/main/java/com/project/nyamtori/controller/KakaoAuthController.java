package com.project.nyamtori.controller;

import com.project.nyamtori.dto.response.LoginResponse;
import com.project.nyamtori.service.AuthService;
import com.project.nyamtori.service.KakaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth/oauth/kakao")
@RequiredArgsConstructor
public class KakaoAuthController {

    private final KakaoService kakaoService;
    private final AuthService authService;

    @GetMapping("/login")
    public ResponseEntity<?> redirectToKakaoLogin() {

        String kakaoAuthUrl =
                "https://kauth.kakao.com/oauth/authorize?response_type=code"
                        + "&client_id=" + kakaoService.getClientId()
                        + "&redirect_uri=" + kakaoService.getRedirectUri();

        return ResponseEntity.status(302)
                .header("Location", kakaoAuthUrl)
                .build();
    }

    @GetMapping("/callback")
    public ResponseEntity<LoginResponse> kakaoCallback(@RequestParam String code) {

        LoginResponse loginResponse = authService.kakaoLogin(code);

        return ResponseEntity.ok(loginResponse);
    }
}
