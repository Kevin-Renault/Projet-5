package com.openclassrooms.mddapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(description = "A discussion topic that articles can belong to.")
public record TopicDto(
                @Schema(description = "Topic identifier", example = "2") @Positive Long id,
                @Schema(description = "Topic name", example = "Java") @Size(max = 100) String name,
                @Schema(description = "Topic description", example = "All things Java and the JVM ecosystem.") @Size(max = 1000) String description) {
}
