package com.taxi.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "taxi.jwt")
public class JwtProperties {

    /**
     * HMAC секрет (минимум 256 бит для HS256).
     */
    private String secret =
            "local-dev-secret-change-me-use-at-least-32-bytes-for-production-demo-key!!";

    /** Время жизни токена, секунды. */
    private long expirationSeconds = 86400;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpirationSeconds() {
        return expirationSeconds;
    }

    public void setExpirationSeconds(long expirationSeconds) {
        this.expirationSeconds = expirationSeconds;
    }
}
