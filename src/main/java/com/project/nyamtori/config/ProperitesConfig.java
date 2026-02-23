package com.project.nyamtori.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        KakaoOauthProperties.class
})

//KakaoOauthProperties를 스프링 빈으로 등록
public class ProperitesConfig {

}
