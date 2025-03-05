package com.example.module_member.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;
import java.util.Date;


@Component
public class JwtUtil {
    private static final String SECRET_KEY = "test"; // 환경 변수에서 가져오도록 변경 가능
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 10; // 10시간

    public String generateToken(int userid) {
        return Jwts.builder()
                .setSubject(String.valueOf(userid))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }
}