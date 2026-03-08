package com.project.nyamtori.service;

import com.project.nyamtori.dto.response.KakaoTokenResponse;
import com.project.nyamtori.dto.response.KakaoUserInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.BodyInserters;


@RequiredArgsConstructor
@Service
public class KakaoService {

    private final WebClient kakaoApiWebClient;
    private final WebClient kakaoAuthWebClient;

    @Value("${kakao.client_id}")
    private String clientId;
    @Value("${kakao.redirect_uri}")
    private String redirectUri;
    @Value("${kakao.client_secret}")
    private String clientSecret;

    public String getClientId() {
        return clientId;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public String getAccessTokenFromKakao(String code){
        KakaoTokenResponse kakaoTokenResponseDto=kakaoAuthWebClient.post()
                .uri("/oauth/token")
                .body(
                        BodyInserters.fromFormData("grant_type","authorization_code")
                                .with("client_id",clientId)
                                .with("client_secret",clientSecret)
                                .with("redirect_uri", redirectUri)
                                .with("code",code))
                .retrieve()
                .bodyToMono(KakaoTokenResponse.class)
                .block();


        return kakaoTokenResponseDto.accessToken();

    }

    public KakaoUserInfoResponse getUserInfo(String accessToken){
        KakaoUserInfoResponse userInfo = kakaoApiWebClient.get()
                .uri("/v2/user/me")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(KakaoUserInfoResponse.class)
                .block();

        return userInfo;
    }


}
