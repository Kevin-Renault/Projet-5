package com.openclassrooms.mddapi.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
public class JwtCookieService {

    private final String cookieName;
    private final boolean cookieSecure;
    private final String sameSite;
    private final long expirationSeconds;

    public JwtCookieService(
            @Value("${security.jwt.cookie-name:access_token}") String cookieName,
            @Value("${security.jwt.cookie-secure:false}") boolean cookieSecure,
            @Value("${security.jwt.cookie-samesite:Lax}") String sameSite,
            @Value("${security.jwt.expiration-seconds:86400}") long expirationSeconds) {
        this.cookieName = cookieName;
        this.cookieSecure = cookieSecure;
        this.sameSite = sameSite;
        this.expirationSeconds = expirationSeconds;
    }

    public String getCookieName() {
        return cookieName;
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
}
