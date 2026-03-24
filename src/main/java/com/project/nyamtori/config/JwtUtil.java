package com.project.nyamtori.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token-expire-time}")
    private long accessExpireTime;

    @Value("${jwt.refresh-token-expire-time}")
    private long refreshExpireTime;

    private SecretKey key;

    @PostConstruct
    public void init(){
        this.key=Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public String createAccessToken(Long userId){
        Date now = new Date();
        Date expire = new Date(now.getTime()+accessExpireTime);

        return Jwts.builder()
                .subject(userId.toString())
                .claim("type", "access")
                .issuedAt(now)
                .expiration(expire)
                .signWith(key,Jwts.SIG.HS256)
                .compact();
    }

    public String createRefreshToken(Long userId){
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("type", "refresh")
                .expiration(new Date(System.currentTimeMillis()+refreshExpireTime))
                .signWith(key)
                .compact();
    }

    public Long getUserId(String token){
        return Long.valueOf(
                Jwts.parser()
                        .verifyWith(key)
                        .build()
                        .parseSignedClaims(token)
                        .getPayload()
                        .getSubject()
        );
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getTokenType(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.get("type", String.class);
    }

}