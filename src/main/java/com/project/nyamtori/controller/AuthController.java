package com.project.nyamtori.controller;

import com.project.nyamtori.config.JwtUtil;
import com.project.nyamtori.dto.request.RefreshRequest;
import com.project.nyamtori.dto.response.LoginResponse;
import com.project.nyamtori.dto.response.TokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtUtil jwtUtil;

    @GetMapping("/oauth/kakao/login")
    public String loginSuccess(Authentication authentication){
        return "로그인 성공: " + authentication.getName();
    }

    // 프론트엔드가 아직 없어서 임시로 붙여둔 콜백 — 카카오 로그인 성공 시 발급된 토큰을
    // 외부 도메인으로 리다이렉트하지 않고 이 화면에서 바로 확인/복사할 수 있게 한다.
    // FRONTEND_OAUTH_REDIRECT_URI를 이 경로로 지정해서 사용.
    @GetMapping("/oauth/callback")
    public ResponseEntity<Map<String, String>> oauthCallback(
            @RequestParam String accessToken,
            @RequestParam String refreshToken
    ) {
        return ResponseEntity.ok(Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken
        ));
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
