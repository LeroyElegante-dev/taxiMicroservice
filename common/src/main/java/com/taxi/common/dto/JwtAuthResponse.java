package com.taxi.common.dto;

public record JwtAuthResponse(String token, String tokenType, long expiresInSeconds) {

    public static JwtAuthResponse of(String token, long expiresInSeconds) {
        return new JwtAuthResponse(token, "Bearer", expiresInSeconds);
    }
}
