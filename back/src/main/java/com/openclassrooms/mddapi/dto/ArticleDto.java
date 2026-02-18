package com.openclassrooms.mddapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.Instant;

@Schema(description = "An article published by a user within a topic.")
public record ArticleDto(
                @Schema(description = "Article identifier", example = "1") @Positive Long id,
                @Schema(description = "Title", example = "My first article") @Size(max = 100) String title,
                @Schema(description = "Content (markdown/plain text)", example = "Hello world! This is my first post.") String content,
                @Schema(description = "Creation timestamp (UTC)", example = "2024-01-01T12:00:00Z") @PastOrPresent Instant createdAt,
                @Schema(description = "Author user id", example = "1") @Positive Long authorId,
                @Schema(description = "Topic id", example = "2") @Positive Long topicId) {
}
