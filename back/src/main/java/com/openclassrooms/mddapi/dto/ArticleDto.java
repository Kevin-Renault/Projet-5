package com.openclassrooms.mddapi.dto;

import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record ArticleDto(
        @Positive Long id,
        @Size(max = 100) String title,
        String content,
        @PastOrPresent Instant createdAt,
        @Positive Long authorId,
        @Positive Long topicId) {
}
