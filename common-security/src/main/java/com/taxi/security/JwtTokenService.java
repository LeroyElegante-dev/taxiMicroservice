package com.taxi.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class JwtTokenService {

    public static final String CLAIM_ROLE = "role";

    private final JwtProperties properties;
    private final SecretKey key;

    public JwtTokenService(JwtProperties properties) {
        this.properties = properties;
        this.key = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String username, String role) {
        long now = System.currentTimeMillis();
        Date issued = new Date(now);
        Date expiry = new Date(now + properties.getExpirationSeconds() * 1000);
        return Jwts.builder()
                .subject(username)
                .issuedAt(issued)
                .expiration(expiry)
                .claim(CLAIM_ROLE, role)
                .signWith(key)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
