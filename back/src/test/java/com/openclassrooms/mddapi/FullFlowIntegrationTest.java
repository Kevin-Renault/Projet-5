package com.openclassrooms.mddapi;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openclassrooms.mddapi.dto.ArticleDto;
import com.openclassrooms.mddapi.dto.CommentDto;
import com.openclassrooms.mddapi.dto.TopicDto;
import com.openclassrooms.mddapi.dto.auth.AuthResponseDto;
import com.openclassrooms.mddapi.dto.auth.LoginRequest;
import com.openclassrooms.mddapi.dto.auth.RegisterRequest;
import com.openclassrooms.mddapi.dto.article.CreateArticleRequest;
import com.openclassrooms.mddapi.dto.UserDto;
import com.openclassrooms.mddapi.repository.ArticleCommentRepository;
import com.openclassrooms.mddapi.repository.ArticleRepository;
import com.openclassrooms.mddapi.repository.MddUserRepository;
import com.openclassrooms.mddapi.security.JwtCookieService;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FullFlowIntegrationTest {

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtCookieService jwtCookieService;

    @Autowired
    private MddUserRepository mddUserRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private ArticleCommentRepository articleCommentRepository;

    private String createdTestEmail;
    private String createdTestUsername;
    private Long createdUserId;
    private Long createdArticleId;
    private Long createdCommentId;

    @AfterEach
    void cleanup() {
        if (createdCommentId != null) {
            articleCommentRepository.findById(createdCommentId).ifPresent(articleCommentRepository::delete);
        }

        if (createdArticleId != null) {
            articleRepository.findById(createdArticleId).ifPresent(articleRepository::delete);
        }

        if (createdUserId != null) {
            mddUserRepository.findById(createdUserId).ifPresent(mddUserRepository::delete);
        } else if (createdTestEmail != null) {
            mddUserRepository.findByEmail(createdTestEmail).ifPresent(mddUserRepository::delete);
        } else if (createdTestUsername != null) {
            mddUserRepository.findByUsername(createdTestUsername).ifPresent(mddUserRepository::delete);
        }

        createdTestEmail = null;
        createdTestUsername = null;
        createdUserId = null;
        createdArticleId = null;
        createdCommentId = null;
    }

    @Test
    void fullIntegrationFlow_login_list_pick_comment_list_logout() throws Exception {
        String unique = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        String username = "it_" + unique;
        String email = "it_" + unique + "@example.com";
        String password = "TestP@ssw0rd1";

        createdTestUsername = username;
        createdTestEmail = email;

        // Setup: create user (register), then logout so the scenario starts with a
        // login.
        RegisterRequest registerRequest = new RegisterRequest(username, email, password);
        ResponseEntity<AuthResponseDto> registerResponse = rest.postForEntity("/api/auth/register", registerRequest,
                AuthResponseDto.class);
        Assertions.assertThat(registerResponse.getStatusCode().value()).isEqualTo(200);
        Assertions.assertThat(registerResponse.getBody()).isNotNull();
        Assertions.assertThat(registerResponse.getBody().user()).isNotNull();
        createdUserId = registerResponse.getBody().user().id();
        Assertions.assertThat(createdUserId).isNotNull();
        String cookie = extractCookie(registerResponse.getHeaders(), jwtCookieService.getCookieName());
        Assertions.assertThat(cookie).isNotBlank();

        ResponseEntity<Void> setupLogoutResponse = rest.exchange(
                "/api/auth/logout",
                HttpMethod.POST,
                new HttpEntity<>(headersWithCookie(cookie)),
                Void.class);
        Assertions.assertThat(setupLogoutResponse.getStatusCode().value()).isEqualTo(204);

        // 1) Login
        LoginRequest loginRequest = new LoginRequest(email, password);
        ResponseEntity<AuthResponseDto> loginResponse = rest.postForEntity("/api/auth/login", loginRequest,
                AuthResponseDto.class);
        Assertions.assertThat(loginResponse.getStatusCode().value()).isEqualTo(200);
        cookie = extractCookie(loginResponse.getHeaders(), jwtCookieService.getCookieName());
        Assertions.assertThat(cookie).isNotBlank();

        // 2) Get topics (needed to create article when there are no seeded articles)
        ResponseEntity<String> topicsResponse = rest.exchange(
                "/api/topics",
                HttpMethod.GET,
                new HttpEntity<>(headersWithCookie(cookie)),
                String.class);
        Assertions.assertThat(topicsResponse.getStatusCode().value()).isEqualTo(200);
        List<TopicDto> topics = objectMapper.readValue(topicsResponse.getBody(), new TypeReference<>() {
        });
        Assertions.assertThat(topics)
                .isNotNull()
                .isNotEmpty();
        // 3) Get articles
        List<ArticleDto> articles = getArticles(cookie);

        // Ensure there's at least one article to pick from.
        if (articles.isEmpty()) {
            TopicDto topic = topics.get(ThreadLocalRandom.current().nextInt(topics.size()));
            CreateArticleRequest createArticleRequest = new CreateArticleRequest(
                    "Integration test article",
                    "This article was created by an integration test.",
                    topic.id());

            ResponseEntity<ArticleDto> createArticleResponse = rest.exchange(
                    "/api/articles",
                    HttpMethod.POST,
                    new HttpEntity<>(createArticleRequest, headersWithCookie(cookie)),
                    ArticleDto.class);
            Assertions.assertThat(createArticleResponse.getStatusCode().value()).isEqualTo(201);
            Assertions.assertThat(createArticleResponse.getBody()).isNotNull();

            createdArticleId = createArticleResponse.getBody().id();

            articles = getArticles(cookie);
            Assertions.assertThat(articles).isNotEmpty();
        }

        // 4) Choose a random article
        ArticleDto chosen = articles.get(ThreadLocalRandom.current().nextInt(articles.size()));
        Assertions.assertThat(chosen).isNotNull();
        Assertions.assertThat(chosen.id()).isNotNull();

        // 5) Leave a comment
        CommentDto createCommentPayload = new CommentDto(
                null,
                "Integration test comment on article " + chosen.id(),
                null,
                null,
                chosen.id());
        ResponseEntity<CommentDto> createCommentResponse = rest.exchange(
                "/api/comments",
                HttpMethod.POST,
                new HttpEntity<>(createCommentPayload, headersWithCookie(cookie)),
                CommentDto.class);
        Assertions.assertThat(createCommentResponse.getStatusCode().value()).isEqualTo(201);
        Assertions.assertThat(createCommentResponse.getBody()).isNotNull();
        createdCommentId = createCommentResponse.getBody().id();

        // 6) Back to articles
        ResponseEntity<String> articlesAgainResponse = rest.exchange(
                "/api/articles",
                HttpMethod.GET,
                new HttpEntity<>(headersWithCookie(cookie)),
                String.class);
        Assertions.assertThat(articlesAgainResponse.getStatusCode().value()).isEqualTo(200);

        // 7) Logout
        ResponseEntity<Void> logoutResponse = rest.exchange(
                "/api/auth/logout",
                HttpMethod.POST,
                new HttpEntity<>(headersWithCookie(cookie)),
                Void.class);
        Assertions.assertThat(logoutResponse.getStatusCode().value()).isEqualTo(204);

        List<String> setCookieHeaders = logoutResponse.getHeaders().get(HttpHeaders.SET_COOKIE);
        Assertions.assertThat(setCookieHeaders).isNotNull();
        Assertions
                .assertThat(
                        setCookieHeaders.stream().anyMatch(h -> h.startsWith(jwtCookieService.getCookieName() + "=")))
                .isTrue();
    }

    @Test
    void fullIntegrationFlow_update_profile_and_relogin() {
        String unique = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        String username = "it_" + unique;
        String email = "it_" + unique + "@example.com";
        String password = "TestP@ssw0rd1";

        createdTestUsername = username;
        createdTestEmail = email;

        // Register
        RegisterRequest registerRequest = new RegisterRequest(username, email, password);
        ResponseEntity<AuthResponseDto> registerResponse = rest.postForEntity(
                "/api/auth/register",
                registerRequest,
                AuthResponseDto.class);
        Assertions.assertThat(registerResponse.getStatusCode().value()).isEqualTo(200);
        Assertions.assertThat(registerResponse.getBody()).isNotNull();
        Assertions.assertThat(registerResponse.getBody().user()).isNotNull();
        createdUserId = registerResponse.getBody().user().id();
        Assertions.assertThat(createdUserId).isNotNull();

        String cookie = extractCookie(registerResponse.getHeaders(), jwtCookieService.getCookieName());
        Assertions.assertThat(cookie).isNotBlank();

        // Login explicitly (after a logout) to validate the login step
        ResponseEntity<Void> setupLogoutResponse = rest.exchange(
                "/api/auth/logout",
                HttpMethod.POST,
                new HttpEntity<>(headersWithCookie(cookie)),
                Void.class);
        Assertions.assertThat(setupLogoutResponse.getStatusCode().value()).isEqualTo(204);

        ResponseEntity<AuthResponseDto> loginResponse = rest.postForEntity(
                "/api/auth/login",
                new LoginRequest(email, password),
                AuthResponseDto.class);
        Assertions.assertThat(loginResponse.getStatusCode().value()).isEqualTo(200);
        cookie = extractCookie(loginResponse.getHeaders(), jwtCookieService.getCookieName());
        Assertions.assertThat(cookie).isNotBlank();

        // Read profile
        ResponseEntity<UserDto> meResponse = rest.exchange(
                "/api/auth/me",
                HttpMethod.GET,
                new HttpEntity<>(headersWithCookie(cookie)),
                UserDto.class);
        Assertions.assertThat(meResponse.getStatusCode().value()).isEqualTo(200);
        Assertions.assertThat(meResponse.getBody()).isNotNull();
        Assertions.assertThat(meResponse.getBody().email()).isEqualTo(email);
        Assertions.assertThat(meResponse.getBody().username()).isEqualTo(username);

        // Update profile (username + email)
        String newUsername = "it2_" + unique;
        String newEmail = "it2_" + unique + "@example.com";
        UserDto updatePayload = new UserDto(null, newUsername, newEmail, null, null, null);

        ResponseEntity<UserDto> updateResponse = rest.exchange(
                "/api/users",
                HttpMethod.PUT,
                new HttpEntity<>(updatePayload, headersWithCookie(cookie)),
                UserDto.class);
        Assertions.assertThat(updateResponse.getStatusCode().value()).isEqualTo(200);
        Assertions.assertThat(updateResponse.getBody()).isNotNull();
        Assertions.assertThat(updateResponse.getBody().username()).isEqualTo(newUsername);
        Assertions.assertThat(updateResponse.getBody().email()).isEqualTo(newEmail);

        // Keep cleanup robust if it falls back to email/username
        createdTestUsername = newUsername;
        createdTestEmail = newEmail;

        // Logout
        ResponseEntity<Void> logoutResponse = rest.exchange(
                "/api/auth/logout",
                HttpMethod.POST,
                new HttpEntity<>(headersWithCookie(cookie)),
                Void.class);
        Assertions.assertThat(logoutResponse.getStatusCode().value()).isEqualTo(204);

        // Re-login using the new email
        ResponseEntity<AuthResponseDto> reloginResponse = rest.postForEntity(
                "/api/auth/login",
                new LoginRequest(newEmail, password),
                AuthResponseDto.class);
        Assertions.assertThat(reloginResponse.getStatusCode().value()).isEqualTo(200);
        cookie = extractCookie(reloginResponse.getHeaders(), jwtCookieService.getCookieName());
        Assertions.assertThat(cookie).isNotBlank();

        // Verify updated profile is effective
        ResponseEntity<UserDto> meAgainResponse = rest.exchange(
                "/api/auth/me",
                HttpMethod.GET,
                new HttpEntity<>(headersWithCookie(cookie)),
                UserDto.class);
        Assertions.assertThat(meAgainResponse.getStatusCode().value()).isEqualTo(200);
        Assertions.assertThat(meAgainResponse.getBody()).isNotNull();
        Assertions.assertThat(meAgainResponse.getBody().email()).isEqualTo(newEmail);
        Assertions.assertThat(meAgainResponse.getBody().username()).isEqualTo(newUsername);
    }

    private List<ArticleDto> getArticles(String cookie) throws Exception {
        ResponseEntity<String> articlesResponse = rest.exchange(
                "/api/articles",
                HttpMethod.GET,
                new HttpEntity<>(headersWithCookie(cookie)),
                String.class);
        Assertions.assertThat(articlesResponse.getStatusCode().value()).isEqualTo(200);
        List<ArticleDto> articles = objectMapper.readValue(articlesResponse.getBody(), new TypeReference<>() {
        });
        return articles == null ? List.of() : articles;
    }

    private static HttpHeaders headersWithCookie(String cookie) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.add(HttpHeaders.COOKIE, cookie);
        return headers;
    }

    private static String extractCookie(HttpHeaders headers, String cookieName) {
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
