package com.openclassrooms.mddapi.service;

import java.time.Duration;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
public class AuthCookieService {

    public ResponseCookie accessCookie(String jwt) {
        return ResponseCookie.from("ACCESS_TOKEN", jwt)
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax") // "Strict" si tu n'as aucun besoin cross-site
                .path("/")
                .maxAge(Duration.ofMinutes(10))
                .build();
    }

    public ResponseCookie refreshCookie(String refreshToken) {
        return ResponseCookie.from("REFRESH_TOKEN", refreshToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/api/auth") // réduit la surface d’envoi
                .maxAge(Duration.ofDays(30))
                .build();
    }

    public ResponseCookie clearAccess() {
        return ResponseCookie.from("ACCESS_TOKEN", "")
                .httpOnly(true).secure(true).sameSite("Lax").path("/")
                .maxAge(Duration.ZERO).build();
    }

    public ResponseCookie clearRefresh() {
        return ResponseCookie.from("REFRESH_TOKEN", "")
                .httpOnly(true).secure(true).sameSite("Lax").path("/api/auth")
                .maxAge(Duration.ZERO).build();
    }
}
