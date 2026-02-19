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

        ResponseEntity<AuthResponseDto> response = rest.postForEntity(
                "/api/auth/register",
                new RegisterRequest("it_" + unique, "not-an-email", "TestP@ssw0rd1"),
                AuthResponseDto.class);

        Assertions.assertThat(response.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void register_with_duplicate_email_is_rejected() {
        String unique = uniqueSuffix();
        String email = "it_" + unique + "@example.com";
        String password = "TestP@ssw0rd1";

        AuthSession session = registerAndGetSession("it_" + unique, email, password);
        createdUserId = session.userId();

        ResponseEntity<String> second = rest.postForEntity(
                "/api/auth/register",
                new RegisterRequest("it2_" + unique, email, password),
                String.class);

        Assertions.assertThat(second.getStatusCode().value()).isIn(400, 409);
    }

    @Test
    void subscribe_with_null_topic_id_returns_400() {
        String unique = uniqueSuffix();
        AuthSession session = registerAndGetSession(
                "it_" + unique,
                "it_" + unique + "@example.com",
                "TestP@ssw0rd1");
        createdUserId = session.userId();

        ResponseEntity<String> response = rest.exchange(
                "/api/subscriptions",
                HttpMethod.POST,
                new HttpEntity<>(new UserTopicSubscriptionDto(null), headersWithCookie(session.cookie())),
                String.class);

        Assertions.assertThat(response.getStatusCode().value()).isEqualTo(400);
    }

    private static String uniqueSuffix() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }
}
