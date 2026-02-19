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

    @Test
    void register_sets_access_and_refresh_cookies() {
        AuthService authService = Mockito.mock(AuthService.class);
        JwtCookieService cookieService = Mockito.mock(JwtCookieService.class);
        RefreshTokenService refreshTokenService = Mockito.mock(RefreshTokenService.class);
        AuthController controller = new AuthController(authService, cookieService, refreshTokenService);

        UserDto user = new UserDto(1L, "u", "e", "", "user", Instant.now());
        AuthResponseDto response = new AuthResponseDto("access", user);
        Mockito.when(authService.register(Mockito.any())).thenReturn(response);
        Mockito.when(refreshTokenService.issueAndReplaceForUser(1L)).thenReturn("refresh");
        Mockito.when(cookieService.createAccessTokenCookie("access")).thenReturn(ResponseCookie.from("a", "1").build());
        Mockito.when(cookieService.createRefreshTokenCookie("refresh"))
                .thenReturn(ResponseCookie.from("r", "1").build());

        ResponseEntity<AuthResponseDto> out = controller
                .register(new RegisterRequest("u", "e@e.com", "StrongP@ssw0rd"));
        Assertions.assertThat(out.getHeaders().get(HttpHeaders.SET_COOKIE)).hasSize(2);
        Mockito.verify(refreshTokenService).issueAndReplaceForUser(1L);
    }

    @Test
    void refresh_rotates_tokens_and_sets_cookies() {
        AuthService authService = Mockito.mock(AuthService.class);
        JwtCookieService cookieService = Mockito.mock(JwtCookieService.class);
        RefreshTokenService refreshTokenService = Mockito.mock(RefreshTokenService.class);
        AuthController controller = new AuthController(authService, cookieService, refreshTokenService);

        Mockito.when(cookieService.getRefreshCookieName()).thenReturn("refresh_token");
        Mockito.when(refreshTokenService.rotate("old_refresh"))
                .thenReturn(new RefreshTokenService.RefreshRotationResult(5L, "new_refresh"));

        UserDto user = new UserDto(5L, "u", "e", "", "user", Instant.now());
        AuthResponseDto response = new AuthResponseDto("new_access", user);
        Mockito.when(authService.refreshAccessToken(5L)).thenReturn(response);
        Mockito.when(cookieService.createAccessTokenCookie("new_access"))
                .thenReturn(ResponseCookie.from("a", "1").build());
        Mockito.when(cookieService.createRefreshTokenCookie("new_refresh"))
                .thenReturn(ResponseCookie.from("r", "1").build());

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setCookies(new jakarta.servlet.http.Cookie("refresh_token", "old_refresh"));

        ResponseEntity<AuthResponseDto> out = controller.refresh(req);
        Assertions.assertThat(out.getHeaders().get(HttpHeaders.SET_COOKIE)).hasSize(2);
        Assertions.assertThat(out.getBody()).isSameAs(response);
    }

    @Test
    void logout_revokes_refresh_and_clears_both_cookies() {
        AuthService authService = Mockito.mock(AuthService.class);
        JwtCookieService cookieService = Mockito.mock(JwtCookieService.class);
        RefreshTokenService refreshTokenService = Mockito.mock(RefreshTokenService.class);
        AuthController controller = new AuthController(authService, cookieService, refreshTokenService);

        Mockito.when(cookieService.clearAccessTokenCookie()).thenReturn(ResponseCookie.from("a", "").build());
        Mockito.when(cookieService.clearRefreshTokenCookie()).thenReturn(ResponseCookie.from("r", "").build());

        MddUserEntity principal = new MddUserEntity();
        principal.setId(99L);

        ResponseEntity<Void> out = controller.logout(principal);
        Assertions.assertThat(out.getHeaders().get(HttpHeaders.SET_COOKIE)).hasSize(2);
        Mockito.verify(refreshTokenService).revokeForUser(99L);
    }

    @Test
    void login_sets_access_and_refresh_cookies() {
        AuthService authService = Mockito.mock(AuthService.class);
        JwtCookieService cookieService = Mockito.mock(JwtCookieService.class);
        RefreshTokenService refreshTokenService = Mockito.mock(RefreshTokenService.class);
        AuthController controller = new AuthController(authService, cookieService, refreshTokenService);

        UserDto user = new UserDto(2L, "u", "e", "", "user", Instant.now());
        AuthResponseDto response = new AuthResponseDto("access", user);
        Mockito.when(authService.login(Mockito.any())).thenReturn(response);
        Mockito.when(refreshTokenService.issueAndReplaceForUser(2L)).thenReturn("refresh");
        Mockito.when(cookieService.createAccessTokenCookie("access")).thenReturn(ResponseCookie.from("a", "1").build());
        Mockito.when(cookieService.createRefreshTokenCookie("refresh"))
                .thenReturn(ResponseCookie.from("r", "1").build());

        ResponseEntity<AuthResponseDto> out = controller.login(new LoginRequest("e", "p"));
        Assertions.assertThat(out.getHeaders().get(HttpHeaders.SET_COOKIE)).hasSize(2);
        Mockito.verify(refreshTokenService).issueAndReplaceForUser(2L);
    }
}
