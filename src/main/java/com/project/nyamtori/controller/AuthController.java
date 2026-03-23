package com.project.nyamtori.controller;

import com.project.nyamtori.config.JwtUtil;
import com.project.nyamtori.dto.request.RefreshRequest;
import com.project.nyamtori.dto.response.LoginResponse;
import com.project.nyamtori.dto.response.TokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtUtil jwtUtil;

    @GetMapping("/oauth/kakao/login")
    public String loginSuccess(Authentication authentication){
        return "로그인 성공: " + authentication.getName();
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
