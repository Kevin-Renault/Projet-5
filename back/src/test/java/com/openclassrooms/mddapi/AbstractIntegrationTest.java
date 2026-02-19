package com.openclassrooms.mddapi;

import com.openclassrooms.mddapi.dto.auth.AuthResponseDto;
import com.openclassrooms.mddapi.dto.auth.LoginRequest;
import com.openclassrooms.mddapi.dto.auth.RegisterRequest;
import com.openclassrooms.mddapi.security.JwtCookieService;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

abstract class AbstractIntegrationTest {

    private static final String CSRF_COOKIE_NAME = "XSRF-TOKEN";
    private static final String CSRF_HEADER_NAME = "X-XSRF-TOKEN";

    @Autowired
    protected TestRestTemplate rest;

    @Autowired
    protected JwtCookieService jwtCookieService;

    protected String csrfCookiePair;
    protected String csrfTokenValue;

    protected record AuthSession(Long userId, String cookie) {
    }

    protected static HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }

    protected HttpHeaders headersWithCookie(String cookie) {
        HttpHeaders headers = jsonHeaders();

        String cookieHeader = cookie;
        if (csrfCookiePair != null && !csrfCookiePair.isBlank()) {
            cookieHeader = cookieHeader + "; " + csrfCookiePair;
        }
        headers.add(HttpHeaders.COOKIE, cookieHeader);

        if (csrfTokenValue != null && !csrfTokenValue.isBlank()) {
            headers.add(CSRF_HEADER_NAME, csrfTokenValue);
        }

        return headers;
    }

    protected AuthSession registerAndGetSession(String username, String email, String password) {
        ResponseEntity<AuthResponseDto> registerResponse = rest.postForEntity(
                "/api/auth/register",
                new RegisterRequest(username, email, password),
                AuthResponseDto.class);

        Assertions.assertThat(registerResponse.getStatusCode().value()).isEqualTo(200);
        Assertions.assertThat(registerResponse.getBody()).isNotNull();
        Assertions.assertThat(registerResponse.getBody().user()).isNotNull();

        Long userId = registerResponse.getBody().user().id();
        Assertions.assertThat(userId).isNotNull();

        captureCsrf(registerResponse.getHeaders());
        ensureCsrf();

        String cookie = extractCookie(registerResponse.getHeaders(), jwtCookieService.getCookieName());
        Assertions.assertThat(cookie).isNotBlank();

        return new AuthSession(userId, cookie);
    }

    protected String loginAndGetCookie(String email, String password) {
        ResponseEntity<AuthResponseDto> loginResponse = rest.postForEntity(
                "/api/auth/login",
                new LoginRequest(email, password),
                AuthResponseDto.class);

        Assertions.assertThat(loginResponse.getStatusCode().value()).isEqualTo(200);
        captureCsrf(loginResponse.getHeaders());
        ensureCsrf();
        String cookie = extractCookie(loginResponse.getHeaders(), jwtCookieService.getCookieName());
        Assertions.assertThat(cookie).isNotBlank();
        return cookie;
    }

    protected void ensureCsrf() {
        if (csrfTokenValue != null && !csrfTokenValue.isBlank() && csrfCookiePair != null
                && !csrfCookiePair.isBlank()) {
            return;
        }

        ResponseEntity<Void> out = rest.exchange(
                "/api/auth/csrf",
                org.springframework.http.HttpMethod.GET,
                new HttpEntity<>(null, jsonHeaders()),
                Void.class);
        Assertions.assertThat(out.getStatusCode().value()).isEqualTo(204);
        captureCsrf(out.getHeaders());

        Assertions.assertThat(csrfCookiePair)
                .as("Expected %s cookie to be set by /api/auth/csrf", CSRF_COOKIE_NAME)
                .isNotBlank();
        Assertions.assertThat(csrfTokenValue)
                .as("Expected %s header value to be available after /api/auth/csrf", CSRF_HEADER_NAME)
                .isNotBlank();
    }

    protected void captureCsrf(HttpHeaders headers) {
        String xsrfPair = extractCookieIfPresent(headers, CSRF_COOKIE_NAME);
        if (xsrfPair == null || xsrfPair.isBlank()) {
            return;
        }
        csrfCookiePair = xsrfPair;
        int eq = xsrfPair.indexOf('=');
        csrfTokenValue = (eq >= 0 && eq + 1 < xsrfPair.length()) ? xsrfPair.substring(eq + 1) : null;
    }

    protected ResponseEntity<Void> logout(String cookie) {
        return rest.postForEntity(
                "/api/auth/logout",
                new HttpEntity<>(null, headersWithCookie(cookie)),
                Void.class);
    }

    protected static String extractCookie(HttpHeaders headers, String cookieName) {
        List<String> setCookieHeaders = headers.get(HttpHeaders.SET_COOKIE);
        if (setCookieHeaders == null || setCookieHeaders.isEmpty()) {
            throw new IllegalStateException("Missing Set-Cookie header");
        }

        for (String setCookie : setCookieHeaders) {
            if (setCookie == null) {
                continue;
            }
            if (setCookie.startsWith(cookieName + "=")) {
                int semi = setCookie.indexOf(';');
                String cookiePair = (semi >= 0) ? setCookie.substring(0, semi) : setCookie;
                if (!cookiePair.isBlank()) {
                    return cookiePair;
                }
            }
        }

        throw new IllegalStateException("Cookie '" + cookieName + "' not found in Set-Cookie headers");
    }

    protected static String extractCookieIfPresent(HttpHeaders headers, String cookieName) {
        List<String> setCookieHeaders = headers.get(HttpHeaders.SET_COOKIE);
        if (setCookieHeaders == null || setCookieHeaders.isEmpty()) {
            return null;
        }

        for (String setCookie : setCookieHeaders) {
            if (setCookie == null) {
                continue;
            }
            if (setCookie.startsWith(cookieName + "=")) {
                int semi = setCookie.indexOf(';');
                String cookiePair = (semi >= 0) ? setCookie.substring(0, semi) : setCookie;
                return cookiePair.isBlank() ? null : cookiePair;
            }
        }

        return null;
    }
}
