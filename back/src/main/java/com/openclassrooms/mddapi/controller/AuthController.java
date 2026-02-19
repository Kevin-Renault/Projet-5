package com.openclassrooms.mddapi.controller;

import com.openclassrooms.mddapi.dto.UserDto;
import com.openclassrooms.mddapi.dto.auth.AuthResponseDto;
import com.openclassrooms.mddapi.dto.auth.LoginRequest;
import com.openclassrooms.mddapi.dto.auth.RegisterRequest;
import com.openclassrooms.mddapi.entity.MddUserEntity;
import com.openclassrooms.mddapi.exception.ApiErrorResponse;
import com.openclassrooms.mddapi.security.JwtCookieService;
import com.openclassrooms.mddapi.security.RefreshTokenService;
import com.openclassrooms.mddapi.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Register, login, logout and retrieve the current user.")
public class AuthController {

    private final AuthService authService;
    private final JwtCookieService cookieService;
    private final RefreshTokenService refreshTokenService;

    public AuthController(AuthService authService, JwtCookieService cookieService,
            RefreshTokenService refreshTokenService) {
        this.authService = authService;
        this.cookieService = cookieService;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a new user account and sets an access token cookie in the response.", responses = {
            @ApiResponse(responseCode = "200", description = "User registered and authenticated", content = @Content(schema = @Schema(implementation = AuthResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Conflict (e.g. email already exists)", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<AuthResponseDto> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponseDto response = authService.register(request);
        String refreshToken = refreshTokenService.issueAndReplaceForUser(response.user().id());
        ResponseCookie accessCookie = cookieService.createAccessTokenCookie(response.token());
        ResponseCookie refreshCookie = cookieService.createRefreshTokenCookie(refreshToken);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticates a user and sets an access token cookie in the response.", responses = {
            @ApiResponse(responseCode = "200", description = "Authenticated", content = @Content(schema = @Schema(implementation = AuthResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequest request) {
        AuthResponseDto response = authService.login(request);
        String refreshToken = refreshTokenService.issueAndReplaceForUser(response.user().id());
        ResponseCookie accessCookie = cookieService.createAccessTokenCookie(response.token());
        ResponseCookie refreshCookie = cookieService.createRefreshTokenCookie(refreshToken);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Uses the refresh token cookie to rotate the session and set a new access token cookie.", responses = {
            @ApiResponse(responseCode = "200", description = "Access token refreshed", content = @Content(schema = @Schema(implementation = AuthResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<AuthResponseDto> refresh(HttpServletRequest request) {
        String presented = extractCookieValue(request, cookieService.getRefreshCookieName());
        RefreshTokenService.RefreshRotationResult rotation = refreshTokenService.rotate(presented);

        AuthResponseDto response = authService.refreshAccessToken(rotation.userId());

        ResponseCookie accessCookie = cookieService.createAccessTokenCookie(response.token());
        ResponseCookie refreshCookie = cookieService.createRefreshTokenCookie(rotation.newRefreshToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Clears access + refresh token cookies and revokes the refresh token.", responses = {
            @ApiResponse(responseCode = "204", description = "Logged out (cookie cleared)", content = @Content),
            @ApiResponse(responseCode = "401", description = "Not authenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @SecurityRequirement(name = com.openclassrooms.mddapi.config.OpenApiConfig.BEARER_AUTH_SCHEME)
    @SecurityRequirement(name = com.openclassrooms.mddapi.config.OpenApiConfig.COOKIE_AUTH_SCHEME)
    public ResponseEntity<Void> logout(
            HttpServletRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal Object principal) {

        if (principal instanceof MddUserEntity user) {
            refreshTokenService.revokeForUser(user.getId());
        } else {
            // Access token may be expired; still revoke based on refresh cookie when
            // present.
            String presented = extractCookieValue(request, cookieService.getRefreshCookieName());
            refreshTokenService.revokePresentedToken(presented);
        }

        ResponseCookie accessCookie = cookieService.clearAccessTokenCookie();
        ResponseCookie refreshCookie = cookieService.clearRefreshTokenCookie();
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .build();
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Returns the currently authenticated user. Authentication is done via the access token cookie.", responses = {
            @ApiResponse(responseCode = "200", description = "Current user", content = @Content(schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @SecurityRequirement(name = com.openclassrooms.mddapi.config.OpenApiConfig.BEARER_AUTH_SCHEME)
    @SecurityRequirement(name = com.openclassrooms.mddapi.config.OpenApiConfig.COOKIE_AUTH_SCHEME)
    public ResponseEntity<UserDto> me(@Parameter(hidden = true) @AuthenticationPrincipal Object principal) {
        if (principal instanceof MddUserEntity user) {
            return ResponseEntity.ok(authService.toUserDto(user));
        }
        return ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED).build();
    }

    private String extractCookieValue(HttpServletRequest request, String cookieName) {
        if (cookieName == null || cookieName.isBlank()) {
            return null;
        }
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (cookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
