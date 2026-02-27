package com.openclassrooms.mddapi;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SecurityBaselineIntegrationTest extends AbstractIntegrationTest {

        @Test
        void secured_endpoints_without_auth_are_rejected() {
                assertUnauthorized(rest.exchange(
                                ApiEndpoints.TOPICS,
                                HttpMethod.GET,
                                new HttpEntity<>(null, jsonHeaders()),
                                String.class));

                assertUnauthorized(rest.exchange(
                                ApiEndpoints.ARTICLES,
                                HttpMethod.GET,
                                new HttpEntity<>(null, jsonHeaders()),
                                String.class));

                assertUnauthorized(rest.exchange(
                                ApiEndpoints.SUBSCRIPTIONS,
                                HttpMethod.GET,
                                new HttpEntity<>(null, jsonHeaders()),
                                String.class));

                assertUnauthorized(rest.exchange(
                                ApiEndpoints.AUTH_ME,
                                HttpMethod.GET,
                                new HttpEntity<>(null, jsonHeaders()),
                                String.class));
        }

        private static void assertUnauthorized(ResponseEntity<?> response) {
                Assertions.assertThat(response.getStatusCode().value()).isIn(401, 403);
        }
}
