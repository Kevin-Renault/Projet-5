package com.openclassrooms.mddapi.dto.article;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Payload to create a new article.")
public record CreateArticleRequest(
                @Schema(description = "Title", example = "Why I like Spring Boot") @NotBlank @Size(max = 100) String title,
                @Schema(description = "Content", example = "Spring Boot makes it easy to create production-ready apps...") @NotBlank String content,
                @Schema(description = "Topic id", example = "2") @NotNull @Positive Long topicId) {
}
