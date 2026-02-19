package com.openclassrooms.mddapi.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
public class JwtCookieService {

    private final String cookieName;
    private final String refreshCookieName;
    private final boolean cookieSecure;
    private final String sameSite;
    private final long expirationSeconds;
    private final long refreshExpirationSeconds;

    public JwtCookieService(
            @Value("${security.jwt.cookie-name:access_token}") String cookieName,
            @Value("${security.jwt.refresh-cookie-name:refresh_token}") String refreshCookieName,
            @Value("${security.jwt.cookie-secure:false}") boolean cookieSecure,
            @Value("${security.jwt.cookie-samesite:Lax}") String sameSite,
            @Value("${security.jwt.expiration-seconds:86400}") long expirationSeconds,
            @Value("${security.jwt.refresh-expiration-seconds:2592000}") long refreshExpirationSeconds) {
        this.cookieName = cookieName;
        this.refreshCookieName = refreshCookieName;
        this.cookieSecure = cookieSecure;
        this.sameSite = sameSite;
        this.expirationSeconds = expirationSeconds;
        this.refreshExpirationSeconds = refreshExpirationSeconds;
    }

    public String getCookieName() {
        return cookieName;
    }

    public String getRefreshCookieName() {
        return refreshCookieName;
    }

    public boolean isCookieSecure() {
        return cookieSecure;
    }

    public String getSameSite() {
        return sameSite;
    }

    public ResponseCookie createAccessTokenCookie(String token) {
        return ResponseCookie.from(cookieName, token)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .sameSite(sameSite)
                .maxAge(expirationSeconds)
                .build();
    }

    public ResponseCookie clearAccessTokenCookie() {
        return ResponseCookie.from(cookieName, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .sameSite(sameSite)
                .maxAge(0)
                .build();
    }

    public ResponseCookie createRefreshTokenCookie(String token) {
        return ResponseCookie.from(refreshCookieName, token)
                .httpOnly(true)
                .secure(cookieSecure)
                // Restreint l'envoi du refresh cookie à la surface auth uniquement.
                .path("/api/auth")
                .sameSite(sameSite)
                .maxAge(refreshExpirationSeconds)
                .build();
    }

    public ResponseCookie clearRefreshTokenCookie() {
        return ResponseCookie.from(refreshCookieName, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/api/auth")
                .sameSite(sameSite)
                .maxAge(0)
                .build();
    }
}
