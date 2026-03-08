package com.project.nyamtori.service;

import com.project.nyamtori.config.JwtUtil;
import com.project.nyamtori.domain.User;
import com.project.nyamtori.dto.response.KakaoUserInfoResponse;
import com.project.nyamtori.dto.response.LoginResponse;
import com.project.nyamtori.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final KakaoService kakaoService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public LoginResponse kakaoLogin(String code){
        String kakoAccessToken = kakaoService.getAccessTokenFromKakao(code);

        KakaoUserInfoResponse userInfo = kakaoService.getUserInfo(kakoAccessToken);

        String kakaoIdString = String.valueOf(userInfo.id());

        User user = userRepository.findByKakaoId(userInfo.id())
                .orElseGet(() -> {
                    return userRepository.save(
                            User.builder()
                                    .kakaoId(kakaoIdString)
                                    .nickname(userInfo.properties().nickname())
                                    .build()
                    );
                });

        String accessToken = jwtUtil.createAccessToken(user.getUserId());
        String refreshToken = jwtUtil.createRefreshToken(user.getUserId());

        return new LoginResponse(accessToken,refreshToken, user);

    }


}
