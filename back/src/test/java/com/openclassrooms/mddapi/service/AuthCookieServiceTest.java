package com.openclassrooms.mddapi.service;

import java.time.Duration;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;

class AuthCookieServiceTest {

    private final AuthCookieService service = new AuthCookieService();

    @Test
    void accessCookie_has_expected_attributes() {
        ResponseCookie cookie = service.accessCookie("jwt");

        Assertions.assertThat(cookie.getName()).isEqualTo("ACCESS_TOKEN");
        Assertions.assertThat(cookie.getValue()).isEqualTo("jwt");
        Assertions.assertThat(cookie.isHttpOnly()).isTrue();
        Assertions.assertThat(cookie.isSecure()).isTrue();
        Assertions.assertThat(cookie.getSameSite()).isEqualTo("Lax");
        Assertions.assertThat(cookie.getPath()).isEqualTo("/");
        Assertions.assertThat(cookie.getMaxAge()).isEqualTo(Duration.ofMinutes(10));
    }

    @Test
    void refreshCookie_has_expected_attributes() {
        ResponseCookie cookie = service.refreshCookie("refresh");

        Assertions.assertThat(cookie.getName()).isEqualTo("REFRESH_TOKEN");
        Assertions.assertThat(cookie.getValue()).isEqualTo("refresh");
        Assertions.assertThat(cookie.isHttpOnly()).isTrue();
        Assertions.assertThat(cookie.isSecure()).isTrue();
        Assertions.assertThat(cookie.getSameSite()).isEqualTo("Lax");
        Assertions.assertThat(cookie.getPath()).isEqualTo("/api/auth");
        Assertions.assertThat(cookie.getMaxAge()).isEqualTo(Duration.ofDays(30));
    }

    @Test
    void clearAccess_expires_cookie() {
        ResponseCookie cookie = service.clearAccess();

        Assertions.assertThat(cookie.getName()).isEqualTo("ACCESS_TOKEN");
        Assertions.assertThat(cookie.getValue()).isEmpty();
        Assertions.assertThat(cookie.getMaxAge()).isEqualTo(Duration.ZERO);
        Assertions.assertThat(cookie.getPath()).isEqualTo("/");
    }

    @Test
    void clearRefresh_expires_cookie() {
        ResponseCookie cookie = service.clearRefresh();

        Assertions.assertThat(cookie.getName()).isEqualTo("REFRESH_TOKEN");
        Assertions.assertThat(cookie.getValue()).isEmpty();
        Assertions.assertThat(cookie.getMaxAge()).isEqualTo(Duration.ZERO);
        Assertions.assertThat(cookie.getPath()).isEqualTo("/api/auth");
    }
}
