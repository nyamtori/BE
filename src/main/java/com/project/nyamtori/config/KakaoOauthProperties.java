package com.project.nyamtori.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "kakao.oauth")
public record KakaoOauthProperties (
        String restApiKey,
        String clientSecret,
        String tokenUri,
        String userInfoUri,
        List<String> redirectUriAllowList
){

}
