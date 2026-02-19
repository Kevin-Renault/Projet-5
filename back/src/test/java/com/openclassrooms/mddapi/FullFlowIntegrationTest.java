package com.openclassrooms.mddapi;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openclassrooms.mddapi.dto.ArticleDto;
import com.openclassrooms.mddapi.dto.CommentDto;
import com.openclassrooms.mddapi.dto.TopicDto;
import com.openclassrooms.mddapi.dto.UserTopicSubscriptionDto;
import com.openclassrooms.mddapi.dto.auth.AuthResponseDto;
import com.openclassrooms.mddapi.dto.auth.LoginRequest;
import com.openclassrooms.mddapi.dto.auth.RegisterRequest;
import com.openclassrooms.mddapi.dto.article.CreateArticleRequest;
import com.openclassrooms.mddapi.dto.UserDto;
import com.openclassrooms.mddapi.repository.ArticleCommentRepository;
import com.openclassrooms.mddapi.repository.ArticleRepository;
import com.openclassrooms.mddapi.repository.MddUserRepository;
import com.openclassrooms.mddapi.repository.UserTopicSubscriptionRepository;
import com.openclassrooms.mddapi.security.JwtCookieService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

    @Autowired
    private UserTopicSubscriptionRepository userTopicSubscriptionRepository;

    private String createdTestEmail;
    private String createdTestUsername;
    private Long createdUserId;
    private final List<Long> createdArticleIds = new ArrayList<>();
    private final List<Long> createdCommentIds = new ArrayList<>();

    @AfterEach
    void cleanup() {
        for (Long commentId : createdCommentIds) {
            if (commentId != null) {
                articleCommentRepository.findById(commentId).ifPresent(articleCommentRepository::delete);
            }
        }

        for (Long articleId : createdArticleIds) {
            if (articleId != null) {
                articleRepository.findById(articleId).ifPresent(articleRepository::delete);
            }
        }

        if (createdUserId == null) {
            if (createdTestEmail != null) {
                mddUserRepository.findByEmail(createdTestEmail).ifPresent(u -> createdUserId = u.getId());
            } else if (createdTestUsername != null) {
                mddUserRepository.findByUsername(createdTestUsername).ifPresent(u -> createdUserId = u.getId());
            }
        }

        if (createdUserId != null) {
            userTopicSubscriptionRepository.deleteAll(userTopicSubscriptionRepository.findAllByUser_Id(createdUserId));
            mddUserRepository.findById(createdUserId).ifPresent(mddUserRepository::delete);
        }

        createdTestEmail = null;
        createdTestUsername = null;
        createdUserId = null;
        createdArticleIds.clear();
        createdCommentIds.clear();
    }

    @Test
    void fullIntegrationFlow_login_list_pick_comment_list_logout() throws Exception {
        String unique = uniqueSuffix();
        String username = "it_" + unique;
        String email = "it_" + unique + "@example.com";
        String password = "TestP@ssw0rd1";

        createdTestUsername = username;
        createdTestEmail = email;

        // Setup: create user (register), then logout so the scenario starts with a
        // login.
        String cookie = registerUserAndGetCookie(username, email, password);
        logout(cookie);

        // 1) Login
        cookie = loginAndGetCookie(email, password);

        // 2) Get topics (needed to create article when there are no seeded articles)
        ResponseEntity<String> topicsResponse = rest.exchange(
                "/api/topics",
                HttpMethod.GET,
                new HttpEntity<>(null, headersWithCookie(cookie)),
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

            createdArticleIds.add(createArticleResponse.getBody().id());

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
        createdCommentIds.add(createCommentResponse.getBody().id());

        // 6) Back to articles
        ResponseEntity<String> articlesAgainResponse = rest.exchange(
                "/api/articles",
                HttpMethod.GET,
                new HttpEntity<>(null, headersWithCookie(cookie)),
                String.class);
        Assertions.assertThat(articlesAgainResponse.getStatusCode().value()).isEqualTo(200);

        // 7) Logout
        logout(cookie);
    }

    @Test
    void fullIntegrationFlow_update_profile_and_relogin() {
        String unique = uniqueSuffix();
        String username = "it_" + unique;
        String email = "it_" + unique + "@example.com";
        String password = "TestP@ssw0rd1";

        createdTestUsername = username;
        createdTestEmail = email;

        // Register
        String cookie = registerUserAndGetCookie(username, email, password);

        // Login explicitly (after a logout) to validate the login step
        logout(cookie);
        cookie = loginAndGetCookie(email, password);

        // Read profile
        UserDto me = me(cookie);
        Assertions.assertThat(me.email()).isEqualTo(email);
        Assertions.assertThat(me.username()).isEqualTo(username);

        // Update profile (username + email)
        String newUsername = "it2_" + unique;
        String newEmail = "it2_" + unique + "@example.com";
        UserDto updatePayload = new UserDto(null, newUsername, newEmail, null, null, null);

        UserDto updated = updateCurrentUser(cookie, updatePayload);
        Assertions.assertThat(updated.username()).isEqualTo(newUsername);
        Assertions.assertThat(updated.email()).isEqualTo(newEmail);

        // Keep cleanup robust if it falls back to email/username
        createdTestUsername = newUsername;
        createdTestEmail = newEmail;

        // Logout
        logout(cookie);

        // Re-login using the new email
        cookie = loginAndGetCookie(newEmail, password);

        // Verify updated profile is effective
        UserDto meAgain = me(cookie);
        Assertions.assertThat(meAgain.email()).isEqualTo(newEmail);
        Assertions.assertThat(meAgain.username()).isEqualTo(newUsername);
    }

    @Test
    void fullIntegrationFlow_subscribe_topics_visit_articles_comment_logout() throws Exception {
        String unique = uniqueSuffix();
        String username = "it_" + unique;
        String email = "it_" + unique + "@example.com";
        String password = "TestP@ssw0rd1";

        createdTestUsername = username;
        createdTestEmail = email;

        // Setup: create user (register), then logout so the scenario starts with a
        // login.
        String cookie = registerUserAndGetCookie(username, email, password);
        logout(cookie);

        // 1) Login
        cookie = loginAndGetCookie(email, password);

        // 2) List topics, subscribe to two of them
        List<TopicDto> topics = getTopics(cookie);
        Assertions.assertThat(topics).isNotNull().isNotEmpty();

        TopicDto first = topics.get(0);
        TopicDto second = topics.size() > 1 ? topics.get(1) : first;
        Set<Long> subscribedTopicIds = new HashSet<>();
        subscribedTopicIds.add(first.id());
        subscribedTopicIds.add(second.id());

        for (Long topicId : subscribedTopicIds) {
            List<UserTopicSubscriptionDto> subscriptions = subscribeToTopic(cookie, topicId);
            Assertions.assertThat(subscriptions)
                    .isNotNull()
                    .anyMatch(s -> s.topicId().equals(topicId));
        }

        // 3) Verify subscriptions are visible
        List<UserTopicSubscriptionDto> mySubscriptions = getSubscriptions(cookie);
        Assertions.assertThat(mySubscriptions)
                .isNotNull();
        for (Long topicId : subscribedTopicIds) {
            Assertions.assertThat(mySubscriptions).anyMatch(s -> s.topicId().equals(topicId));
        }

        // 4) Visit some articles from subscribed topics (create some if missing)
        List<ArticleDto> articles = getArticles(cookie);
        List<ArticleDto> subscribedArticles = articles.stream()
                .filter(a -> a != null && a.topicId() != null && subscribedTopicIds.contains(a.topicId()))
                .toList();

        if (subscribedArticles.size() < 2) {
            for (Long topicId : subscribedTopicIds) {
                boolean hasAnyForTopic = subscribedArticles.stream().anyMatch(a -> a.topicId().equals(topicId));
                if (!hasAnyForTopic) {
                    CreateArticleRequest req = new CreateArticleRequest(
                            "Integration test subscribed article",
                            "This article was created by an integration test for subscriptions.",
                            topicId);
                    ResponseEntity<ArticleDto> createArticleResponse = rest.exchange(
                            "/api/articles",
                            HttpMethod.POST,
                            new HttpEntity<>(req, headersWithCookie(cookie)),
                            ArticleDto.class);
                    Assertions.assertThat(createArticleResponse.getStatusCode().value()).isEqualTo(201);
                    Assertions.assertThat(createArticleResponse.getBody()).isNotNull();
                    createdArticleIds.add(createArticleResponse.getBody().id());
                }
            }

            articles = getArticles(cookie);
            subscribedArticles = articles.stream()
                    .filter(a -> a != null && a.topicId() != null && subscribedTopicIds.contains(a.topicId()))
                    .toList();
        }

        Assertions.assertThat(subscribedArticles).isNotNull().isNotEmpty();

        // 5) Visit up to 2 articles and post a comment on each
        int toVisit = Math.min(2, subscribedArticles.size());
        for (int i = 0; i < toVisit; i++) {
            ArticleDto target = subscribedArticles.get(i);
            Assertions.assertThat(target.id()).isNotNull();

            ResponseEntity<ArticleDto> articleById = rest.exchange(
                    "/api/articles/" + target.id(),
                    HttpMethod.GET,
                    new HttpEntity<>(null, headersWithCookie(cookie)),
                    ArticleDto.class);
            Assertions.assertThat(articleById.getStatusCode().value()).isEqualTo(200);
            Assertions.assertThat(articleById.getBody()).isNotNull();
            Assertions.assertThat(subscribedTopicIds).contains(articleById.getBody().topicId());

            CommentDto createCommentPayload = new CommentDto(
                    null,
                    "Integration test comment on subscribed article " + target.id(),
                    null,
                    null,
                    target.id());
            ResponseEntity<CommentDto> createCommentResponse = rest.exchange(
                    "/api/comments",
                    HttpMethod.POST,
                    new HttpEntity<>(createCommentPayload, headersWithCookie(cookie)),
                    CommentDto.class);
            Assertions.assertThat(createCommentResponse.getStatusCode().value()).isEqualTo(201);
            Assertions.assertThat(createCommentResponse.getBody()).isNotNull();
            createdCommentIds.add(createCommentResponse.getBody().id());
        }

        // 6) Logout
        logout(cookie);
    }

    private static String uniqueSuffix() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    private String registerUserAndGetCookie(String username, String email, String password) {
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
        return cookie;
    }

    private String loginAndGetCookie(String email, String password) {
        ResponseEntity<AuthResponseDto> loginResponse = rest.postForEntity(
                "/api/auth/login",
                new LoginRequest(email, password),
                AuthResponseDto.class);

        Assertions.assertThat(loginResponse.getStatusCode().value()).isEqualTo(200);
        String cookie = extractCookie(loginResponse.getHeaders(), jwtCookieService.getCookieName());
        Assertions.assertThat(cookie).isNotBlank();
        return cookie;
    }

    private void logout(String cookie) {
        ResponseEntity<Void> logoutResponse = rest.exchange(
                "/api/auth/logout",
                HttpMethod.POST,
                new HttpEntity<>(null, headersWithCookie(cookie)),
                Void.class);
        Assertions.assertThat(logoutResponse.getStatusCode().value()).isEqualTo(204);

        List<String> setCookieHeaders = logoutResponse.getHeaders().get(HttpHeaders.SET_COOKIE);
        Assertions.assertThat(setCookieHeaders).isNotNull();
        Assertions
                .assertThat(
                        setCookieHeaders.stream().anyMatch(h -> h.startsWith(jwtCookieService.getCookieName() + "=")))
                .isTrue();
    }

    private UserDto me(String cookie) {
        ResponseEntity<UserDto> meResponse = rest.exchange(
                "/api/auth/me",
                HttpMethod.GET,
                new HttpEntity<>(null, headersWithCookie(cookie)),
                UserDto.class);
        Assertions.assertThat(meResponse.getStatusCode().value()).isEqualTo(200);
        Assertions.assertThat(meResponse.getBody()).isNotNull();
        return meResponse.getBody();
    }

    private UserDto updateCurrentUser(String cookie, UserDto payload) {
        ResponseEntity<UserDto> updateResponse = rest.exchange(
                "/api/users",
                HttpMethod.PUT,
                new HttpEntity<>(payload, headersWithCookie(cookie)),
                UserDto.class);
        Assertions.assertThat(updateResponse.getStatusCode().value()).isEqualTo(200);
        Assertions.assertThat(updateResponse.getBody()).isNotNull();
        return updateResponse.getBody();
    }

    private List<ArticleDto> getArticles(String cookie) throws Exception {
        ResponseEntity<String> articlesResponse = rest.exchange(
                "/api/articles",
                HttpMethod.GET,
                new HttpEntity<>(null, headersWithCookie(cookie)),
                String.class);
        Assertions.assertThat(articlesResponse.getStatusCode().value()).isEqualTo(200);
        List<ArticleDto> articles = objectMapper.readValue(articlesResponse.getBody(), new TypeReference<>() {
        });
        return articles == null ? List.of() : articles;
    }

    private List<TopicDto> getTopics(String cookie) throws Exception {
        ResponseEntity<String> topicsResponse = rest.exchange(
                "/api/topics",
                HttpMethod.GET,
                new HttpEntity<>(null, headersWithCookie(cookie)),
                String.class);
        Assertions.assertThat(topicsResponse.getStatusCode().value()).isEqualTo(200);
        List<TopicDto> topics = objectMapper.readValue(topicsResponse.getBody(), new TypeReference<>() {
        });
        return topics == null ? List.of() : topics;
    }

    private List<UserTopicSubscriptionDto> getSubscriptions(String cookie) throws Exception {
        ResponseEntity<String> response = rest.exchange(
                "/api/subscriptions",
                HttpMethod.GET,
                new HttpEntity<>(null, headersWithCookie(cookie)),
                String.class);
        Assertions.assertThat(response.getStatusCode().value()).isEqualTo(200);
        List<UserTopicSubscriptionDto> subscriptions = objectMapper.readValue(response.getBody(),
                new TypeReference<>() {
                });
        return subscriptions == null ? List.of() : subscriptions;
    }

    private List<UserTopicSubscriptionDto> subscribeToTopic(String cookie, Long topicId) throws Exception {
        ResponseEntity<String> response = rest.exchange(
                "/api/subscriptions",
                HttpMethod.POST,
                new HttpEntity<>(new UserTopicSubscriptionDto(topicId), headersWithCookie(cookie)),
                String.class);
        Assertions.assertThat(response.getStatusCode().value()).isEqualTo(200);
        List<UserTopicSubscriptionDto> subscriptions = objectMapper.readValue(response.getBody(),
                new TypeReference<>() {
                });
        return subscriptions == null ? List.of() : subscriptions;
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
