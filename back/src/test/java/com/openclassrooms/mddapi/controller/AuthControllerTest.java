package com.openclassrooms.mddapi.controller;

import com.openclassrooms.mddapi.dto.UserDto;
import com.openclassrooms.mddapi.dto.auth.AuthResponseDto;
import com.openclassrooms.mddapi.dto.auth.LoginRequest;
import com.openclassrooms.mddapi.dto.auth.RegisterRequest;
import com.openclassrooms.mddapi.entity.MddUserEntity;
import com.openclassrooms.mddapi.security.JwtCookieService;
import com.openclassrooms.mddapi.security.RefreshTokenService;
import com.openclassrooms.mddapi.service.AuthService;
import java.time.Instant;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

class AuthControllerTest {

    private static final String ACCESS_COOKIE_NAME = "a";
    private static final String REFRESH_COOKIE_NAME = "r";
    private static final String COOKIE_VALUE = "1";

    private static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";

    private static final String USERNAME = "u";
    private static final String EMAIL = "e";
    private static final String ROLE = "user";
    private static final String ACCESS_TOKEN = "access";
    private static final String REFRESH_TOKEN = "refresh";

    @Test
    void registerSetsAccessAndRefreshCookies() {
        AuthService authService = Mockito.mock(AuthService.class);
        JwtCookieService cookieService = Mockito.mock(JwtCookieService.class);
        RefreshTokenService refreshTokenService = Mockito.mock(RefreshTokenService.class);
        AuthController controller = new AuthController(authService, cookieService, refreshTokenService);

        UserDto user = new UserDto(1L, USERNAME, EMAIL, "", ROLE, Instant.now());
        AuthResponseDto response = new AuthResponseDto(ACCESS_TOKEN, user);
        Mockito.when(authService.register(Mockito.any())).thenReturn(response);
        Mockito.when(refreshTokenService.issueAndReplaceForUser(1L)).thenReturn(REFRESH_TOKEN);
        Mockito.when(cookieService.createAccessTokenCookie(ACCESS_TOKEN))
            .thenReturn(ResponseCookie.from(ACCESS_COOKIE_NAME, COOKIE_VALUE).build());
        Mockito.when(cookieService.createRefreshTokenCookie(REFRESH_TOKEN))
            .thenReturn(ResponseCookie.from(REFRESH_COOKIE_NAME, COOKIE_VALUE).build());

        ResponseEntity<AuthResponseDto> out = controller
                .register(new RegisterRequest("u", "e@e.com", "StrongP@ssw0rd"));
        Assertions.assertThat(out.getHeaders().get(HttpHeaders.SET_COOKIE)).hasSize(3);
        Mockito.verify(refreshTokenService).issueAndReplaceForUser(1L);
    }

    @Test
    void refreshRotatesTokensAndSetsCookies() {
        AuthService authService = Mockito.mock(AuthService.class);
        JwtCookieService cookieService = Mockito.mock(JwtCookieService.class);
        RefreshTokenService refreshTokenService = Mockito.mock(RefreshTokenService.class);
        AuthController controller = new AuthController(authService, cookieService, refreshTokenService);

        Mockito.when(cookieService.getRefreshCookieName()).thenReturn(REFRESH_TOKEN_COOKIE_NAME);
        Mockito.when(refreshTokenService.rotate("old_refresh"))
                .thenReturn(new RefreshTokenService.RefreshRotationResult(5L, "new_refresh"));

        UserDto user = new UserDto(5L, USERNAME, EMAIL, "", ROLE, Instant.now());
        AuthResponseDto response = new AuthResponseDto("new_access", user);
        Mockito.when(authService.refreshAccessToken(5L)).thenReturn(response);
        Mockito.when(cookieService.createAccessTokenCookie("new_access"))
            .thenReturn(ResponseCookie.from(ACCESS_COOKIE_NAME, COOKIE_VALUE).build());
        Mockito.when(cookieService.createRefreshTokenCookie("new_refresh"))
            .thenReturn(ResponseCookie.from(REFRESH_COOKIE_NAME, COOKIE_VALUE).build());

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setCookies(new jakarta.servlet.http.Cookie(REFRESH_TOKEN_COOKIE_NAME, "old_refresh"));

        ResponseEntity<AuthResponseDto> out = controller.refresh(req);
        Assertions.assertThat(out.getHeaders().get(HttpHeaders.SET_COOKIE)).hasSize(3);
        Assertions.assertThat(out.getBody()).isSameAs(response);
    }

    @Test
    void logoutRevokesRefreshAndClearsBothCookies() {
        AuthService authService = Mockito.mock(AuthService.class);
        JwtCookieService cookieService = Mockito.mock(JwtCookieService.class);
        RefreshTokenService refreshTokenService = Mockito.mock(RefreshTokenService.class);
        AuthController controller = new AuthController(authService, cookieService, refreshTokenService);

        Mockito.when(cookieService.clearAccessTokenCookie()).thenReturn(ResponseCookie.from(ACCESS_COOKIE_NAME, "").build());
        Mockito.when(cookieService.clearRefreshTokenCookie()).thenReturn(ResponseCookie.from(REFRESH_COOKIE_NAME, "").build());

        MddUserEntity principal = new MddUserEntity();
        principal.setId(99L);

        MockHttpServletRequest req = new MockHttpServletRequest();
        ResponseEntity<Void> out = controller.logout(req, principal);
        Assertions.assertThat(out.getHeaders().get(HttpHeaders.SET_COOKIE)).hasSize(2);
        Mockito.verify(refreshTokenService).revokeForUser(99L);
    }

    @Test
    void logoutWithoutPrincipalRevokesByRefreshCookieAndClearsCookies() {
        AuthService authService = Mockito.mock(AuthService.class);
        JwtCookieService cookieService = Mockito.mock(JwtCookieService.class);
        RefreshTokenService refreshTokenService = Mockito.mock(RefreshTokenService.class);
        AuthController controller = new AuthController(authService, cookieService, refreshTokenService);

        Mockito.when(cookieService.getRefreshCookieName()).thenReturn(REFRESH_TOKEN_COOKIE_NAME);
        Mockito.when(cookieService.clearAccessTokenCookie()).thenReturn(ResponseCookie.from(ACCESS_COOKIE_NAME, "").build());
        Mockito.when(cookieService.clearRefreshTokenCookie()).thenReturn(ResponseCookie.from(REFRESH_COOKIE_NAME, "").build());

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setCookies(new jakarta.servlet.http.Cookie(REFRESH_TOKEN_COOKIE_NAME, "rt"));

        ResponseEntity<Void> out = controller.logout(req, null);
        Assertions.assertThat(out.getHeaders().get(HttpHeaders.SET_COOKIE)).hasSize(2);
        Mockito.verify(refreshTokenService).revokePresentedToken("rt");
    }

    @Test
    void loginSetsAccessAndRefreshCookies() {
        AuthService authService = Mockito.mock(AuthService.class);
        JwtCookieService cookieService = Mockito.mock(JwtCookieService.class);
        RefreshTokenService refreshTokenService = Mockito.mock(RefreshTokenService.class);
        AuthController controller = new AuthController(authService, cookieService, refreshTokenService);

        UserDto user = new UserDto(2L, USERNAME, EMAIL, "", ROLE, Instant.now());
        AuthResponseDto response = new AuthResponseDto(ACCESS_TOKEN, user);
        Mockito.when(authService.login(Mockito.any())).thenReturn(response);
        Mockito.when(refreshTokenService.issueAndReplaceForUser(2L)).thenReturn(REFRESH_TOKEN);
        Mockito.when(cookieService.createAccessTokenCookie(ACCESS_TOKEN))
            .thenReturn(ResponseCookie.from(ACCESS_COOKIE_NAME, COOKIE_VALUE).build());
        Mockito.when(cookieService.createRefreshTokenCookie(REFRESH_TOKEN))
            .thenReturn(ResponseCookie.from(REFRESH_COOKIE_NAME, COOKIE_VALUE).build());

        ResponseEntity<AuthResponseDto> out = controller.login(new LoginRequest("e", "p"));
        Assertions.assertThat(out.getHeaders().get(HttpHeaders.SET_COOKIE)).hasSize(3);
        Mockito.verify(refreshTokenService).issueAndReplaceForUser(2L);
    }
}
