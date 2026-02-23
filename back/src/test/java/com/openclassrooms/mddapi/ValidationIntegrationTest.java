package com.openclassrooms.mddapi;

import com.openclassrooms.mddapi.dto.UserTopicSubscriptionDto;
import com.openclassrooms.mddapi.dto.auth.AuthResponseDto;
import com.openclassrooms.mddapi.dto.auth.RegisterRequest;
import com.openclassrooms.mddapi.repository.MddUserRepository;
import com.openclassrooms.mddapi.repository.UserTopicSubscriptionRepository;
import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ValidationIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MddUserRepository mddUserRepository;

    @Autowired
    private UserTopicSubscriptionRepository userTopicSubscriptionRepository;

    private Long createdUserId;

    @AfterEach
    void cleanup() {
        if (createdUserId != null) {
            userTopicSubscriptionRepository.deleteAll(userTopicSubscriptionRepository.findAllByUser_Id(createdUserId));
            mddUserRepository.findById(createdUserId).ifPresent(mddUserRepository::delete);
        }
        createdUserId = null;
    }

    @Test
    void register_with_invalid_email_returns_400() {
        String unique = uniqueSuffix();

        ensureCsrf();

        ResponseEntity<AuthResponseDto> response = rest.exchange(
                ApiEndpoints.AUTH_REGISTER,
                HttpMethod.POST,
                new HttpEntity<>(new RegisterRequest(
                        TestConstants.TEST_USER_PREFIX + unique,
                        TestConstants.INVALID_EMAIL,
                        TestConstants.TEST_PASSWORD),
                        headersWithCookie(null)),
                AuthResponseDto.class);

        Assertions.assertThat(response.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void register_with_duplicate_email_is_rejected() {
        String unique = uniqueSuffix();
        String email = TestConstants.TEST_USER_PREFIX + unique + TestConstants.TEST_EMAIL_DOMAIN;
        String password = TestConstants.TEST_PASSWORD;

        AuthSession session = registerAndGetSession(TestConstants.TEST_USER_PREFIX + unique, email, password);
        createdUserId = session.userId();

        ensureCsrf();
        ResponseEntity<String> second = rest.exchange(
                ApiEndpoints.AUTH_REGISTER,
                HttpMethod.POST,
                new HttpEntity<>(new RegisterRequest(
                        TestConstants.TEST_USER_PREFIX + "2_" + unique,
                        email,
                        password),
                        headersWithCookie(null)),
                String.class);

        Assertions.assertThat(second.getStatusCode().value()).isIn(400, 409);
    }

    @Test
    void subscribe_with_null_topic_id_returns_400() {
        String unique = uniqueSuffix();
        AuthSession session = registerAndGetSession(
                TestConstants.TEST_USER_PREFIX + unique,
                TestConstants.TEST_USER_PREFIX + unique + TestConstants.TEST_EMAIL_DOMAIN,
                TestConstants.TEST_PASSWORD);
        createdUserId = session.userId();

        ResponseEntity<String> response = rest.exchange(
                ApiEndpoints.SUBSCRIPTIONS,
                HttpMethod.POST,
                new HttpEntity<>(new UserTopicSubscriptionDto(null), headersWithCookie(session.cookie())),
                String.class);

        Assertions.assertThat(response.getStatusCode().value()).isEqualTo(400);
    }

    private static String uniqueSuffix() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }
}
