package com.project.nyamtori.controller;

import com.project.nyamtori.config.JwtUtil;
import com.project.nyamtori.dto.request.RefreshRequest;
import com.project.nyamtori.dto.response.LoginResponse;
import com.project.nyamtori.dto.response.TokenResponse;
import com.project.nyamtori.service.AuthService;
import com.project.nyamtori.service.KakaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final KakaoService kakaoService;
    private final AuthService authService;
    private final JwtUtil jwtUtil;

    @GetMapping("/oauth/kakao/login")
    public ResponseEntity<?> redirectToKakaoLogin() {

        String kakaoAuthUrl =
                "https://kauth.kakao.com/oauth/authorize?response_type=code"
                        + "&client_id=" + kakaoService.getClientId()
                        + "&redirect_uri=" + kakaoService.getRedirectUri();

        return ResponseEntity.status(302)
                .header("Location", kakaoAuthUrl)
                .build();
    }

    @GetMapping("/oauth/kakao/callback")
    public ResponseEntity<LoginResponse> kakaoCallback(@RequestParam String code) {

        LoginResponse loginResponse = authService.kakaoLogin(code);

        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshToken(
            @RequestBody RefreshRequest request
    ){

        Long userId = jwtUtil.getUserId(request.refreshToken());

        String newAccessToken = jwtUtil.createAccessToken(userId);

        return ResponseEntity.ok(new TokenResponse(newAccessToken));
    }
}
