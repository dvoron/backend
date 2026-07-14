package com.example.backend.service.impl;

import com.example.backend.model.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;


@Service
public class JwtService {

//    @Value("${jwt.secret}")
//    private String secret;
    private String secret = "mysecretkeymysecretkeymysecretkey";

//    public String generateToken(User user, UUID sessionId) {
//
////        Add roles in the future instead of "USER", like : user.getRole()
//        return Jwts.builder()
//                .subject(user.getId().toString())
//                .claim("sid", sessionId.toString())
//                .claim("role", "USER")
//                .issuedAt(new Date())
//                .expiration(Date.from(
//                        Instant.now().plus(Duration.ofMinutes(15))
//                ))
//                .signWith(getKey())
//                .compact();
//    }
//
//    public Claims validate(String token) {
//
//        return Jwts.parser()
//                .verifyWith(getKey())
//                .build()
//                .parseSignedClaims(token)
//                .getPayload();
//    }
//
//    private SecretKey getKey() {
//        return Keys.hmacShaKeyFor(secret.getBytes());
//    }
//public String generateToken(UserDetails user) {
//
//    return Jwts.builder()
//            .setSubject(user.getUsername())
//            .setIssuedAt(new Date())
//            .setExpiration(
//                    new Date(
//                            System.currentTimeMillis()
//                                    + 1000 * 60 * 60
//                    )
//            )
//            .signWith(
//                    Keys.hmacShaKeyFor(
//                            SECRET.getBytes()
//                    ),
//                    SignatureAlgorithm.HS256
//            )
//            .compact();
//}
//
//
//    public String extractUsername(String token){
//
//        return Jwts.parserBuilder()
//                .setSigningKey(
//                        SECRET.getBytes()
//                )
//                .build()
//                .parseClaimsJws(token)
//                .getBody()
//                .getSubject();
//    }
}