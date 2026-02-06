package com.openclassrooms.mddapi.dto;

import java.time.Instant;

public record UserTopicSubscriptionDto(
                Long userId,
                Long topicId,
                Instant subscribedAt) {
}
