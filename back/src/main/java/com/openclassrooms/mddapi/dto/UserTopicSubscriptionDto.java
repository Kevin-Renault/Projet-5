package com.openclassrooms.mddapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Subscription payload linking the authenticated user to a topic.")
public record UserTopicSubscriptionDto(
                @Schema(description = "Topic id to subscribe/unsubscribe", example = "2") @NotNull @Positive Long topicId) {
}
