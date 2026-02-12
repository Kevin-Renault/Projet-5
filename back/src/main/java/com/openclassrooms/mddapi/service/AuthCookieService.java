package com.openclassrooms.mddapi.service;

import java.time.Duration;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
public class AuthCookieService {

    private static final String ACCESS_TOKEN_COOKIE = "ACCESS_TOKEN";
    private static final String REFRESH_TOKEN_COOKIE = "REFRESH_TOKEN";
    private static final String API_AUTH_PATH = "/api/auth";
    private static final String SAME_SITE = "Lax";

    public ResponseCookie accessCookie(String jwt) {
        return ResponseCookie.from(ACCESS_TOKEN_COOKIE, jwt)
                .httpOnly(true)
                .secure(true)
                .sameSite(SAME_SITE) // "Strict" si tu n'as aucun besoin cross-site
                .path("/")
                .maxAge(Duration.ofMinutes(10))
                .build();
    }

    public ResponseCookie refreshCookie(String refreshToken) {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE, refreshToken)
                .httpOnly(true)
                .secure(true)
                .sameSite(SAME_SITE)
                .path(API_AUTH_PATH) // réduit la surface d’envoi
                .maxAge(Duration.ofDays(30))
                .build();
    }

    public ResponseCookie clearAccess() {
        return ResponseCookie.from(ACCESS_TOKEN_COOKIE, "")
                .httpOnly(true).secure(true).sameSite(SAME_SITE).path("/")
                .maxAge(Duration.ZERO).build();
    }

    public ResponseCookie clearRefresh() {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE, "")
                .httpOnly(true).secure(true).sameSite(SAME_SITE).path(API_AUTH_PATH)
                .maxAge(Duration.ZERO).build();
    }
}
