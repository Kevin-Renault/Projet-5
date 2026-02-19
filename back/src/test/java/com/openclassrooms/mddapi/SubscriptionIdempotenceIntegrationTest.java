package com.openclassrooms.mddapi;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openclassrooms.mddapi.dto.TopicDto;
import com.openclassrooms.mddapi.dto.UserTopicSubscriptionDto;
import com.openclassrooms.mddapi.repository.MddUserRepository;
import com.openclassrooms.mddapi.repository.UserTopicSubscriptionRepository;
import java.util.List;
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
class SubscriptionIdempotenceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

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
    void subscribe_is_idempotent_for_same_topic() throws Exception {
        String unique = uniqueSuffix();
        String username = "it_" + unique;
        String email = "it_" + unique + "@example.com";
        String password = "TestP@ssw0rd1";

        AuthSession session = registerAndGetSession(username, email, password);
        createdUserId = session.userId();
        String cookie = session.cookie();

        List<TopicDto> topics = getTopics(cookie);
        Assertions.assertThat(topics).isNotNull().isNotEmpty();
        Long topicId = topics.get(0).id();
        Assertions.assertThat(topicId).isNotNull();

        List<UserTopicSubscriptionDto> afterFirst = subscribe(cookie, topicId);
        Assertions.assertThat(afterFirst).anyMatch(s -> s.topicId().equals(topicId));
        Assertions.assertThat(afterFirst).hasSize(1);

        List<UserTopicSubscriptionDto> afterSecond = subscribe(cookie, topicId);
        Assertions.assertThat(afterSecond).anyMatch(s -> s.topicId().equals(topicId));

        long count = afterSecond.stream().filter(s -> s.topicId().equals(topicId)).count();
        Assertions.assertThat(count).isEqualTo(1);
        Assertions.assertThat(afterSecond).hasSize(1);
    }

    private static String uniqueSuffix() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
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

    private List<UserTopicSubscriptionDto> subscribe(String cookie, Long topicId) throws Exception {
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
}
