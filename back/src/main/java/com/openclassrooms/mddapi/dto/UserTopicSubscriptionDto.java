package com.openclassrooms.mddapi.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UserTopicSubscriptionDto(
        @NotNull @Positive Long topicId) {
}
