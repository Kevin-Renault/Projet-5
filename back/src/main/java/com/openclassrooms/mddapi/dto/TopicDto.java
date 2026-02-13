package com.openclassrooms.mddapi.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record TopicDto(
        @Positive Long id,
        @Size(max = 100) String name,
        @Size(max = 1000) String description) {
}
