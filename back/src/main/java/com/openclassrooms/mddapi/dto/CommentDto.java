package com.openclassrooms.mddapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.Instant;

@Schema(description = "A comment posted by a user on an article.")
public record CommentDto(
                @Schema(description = "Comment identifier", example = "1") @Positive Long id,
                @Schema(description = "Comment content", example = "Great article, thanks for sharing!") @NotBlank @Size(max = 2000) String content,
                @Schema(description = "Creation timestamp (UTC)", example = "2024-01-02T09:30:00Z") @PastOrPresent Instant createdAt,
                @Schema(description = "Author user id", example = "1") @Positive Long authorId,
                @Schema(description = "Related article id", example = "10") @NotNull @Positive Long articleId) {
}
