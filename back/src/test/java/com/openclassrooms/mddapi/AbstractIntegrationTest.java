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

    @Autowired
    protected TestRestTemplate rest;

    @Autowired
    protected JwtCookieService jwtCookieService;

    protected record AuthSession(Long userId, String cookie) {
    }

    protected static HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }

    protected static HttpHeaders headersWithCookie(String cookie) {
        HttpHeaders headers = jsonHeaders();
        headers.add(HttpHeaders.COOKIE, cookie);
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
        String cookie = extractCookie(loginResponse.getHeaders(), jwtCookieService.getCookieName());
        Assertions.assertThat(cookie).isNotBlank();
        return cookie;
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
}
