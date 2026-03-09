package com.project.nyamtori.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${kakao.auth-base-url}")
    private String authBaseUrl;

    @Value("${kakao.api-base-url}")
    private String apiBaseUrl;

    @Bean
    public WebClient kakaoAuthWebClient() {
        return WebClient.builder()
                .baseUrl(authBaseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .build();
    }

    @Bean
    public WebClient kakaoApiWebClient() {
        return WebClient.builder()
                .baseUrl(apiBaseUrl)
                .build();
    }

    @Bean
    public WebClient geminiWebClient(
            @Value("${gemini.url}") String baseUrl,
            @Value("${gemini.api-key}") String apiKey
    ){
        return  WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("x-goog-api-key",apiKey)
                .defaultHeader("Content-Type","application/json")
                .build();
    }
}
