package com.openclassrooms.mddapi.dto;

import java.time.Instant;

public record ArticleDto(
                Long id,
                String title,
                String content,
                Instant createdAt,
                Long authorId,
                Long topicId) {
}
