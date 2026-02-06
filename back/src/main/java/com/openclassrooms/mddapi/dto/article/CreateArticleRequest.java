package com.openclassrooms.mddapi.dto.article;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CreateArticleRequest(
        @NotBlank @Size(max = 100) String title,
        @NotBlank String content,
        @NotNull @Positive Long topicId) {
}
