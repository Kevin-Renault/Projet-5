package com.openclassrooms.mddapi.dto;

import java.time.Instant;

public record CommentDto(
                Long id,
                String content,
                Instant createdAt,
                Long authorId,
                Long articleId) {
}
