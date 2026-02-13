package com.openclassrooms.mddapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record CommentDto(
        @Positive Long id,
        @NotBlank @Size(max = 2000) String content,
        @PastOrPresent Instant createdAt,
        @Positive Long authorId,
        @NotNull @Positive Long articleId) {
}
