package com.openclassrooms.mddapi.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import java.time.Instant;

public record UserTopicSubscriptionDto(
        @NotNull @Positive Long userId,
        @NotNull @Positive Long topicId,
        @PastOrPresent Instant subscribedAt) {
}
