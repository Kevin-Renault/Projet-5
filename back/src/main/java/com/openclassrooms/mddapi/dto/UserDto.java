package com.openclassrooms.mddapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.Instant;

@Schema(description = "A user of the application.")
public record UserDto(
        @Schema(description = "User identifier", example = "1") @Positive Long id,
        @Schema(description = "Username", example = "john_doe") @Size(max = 50) String username,
        @Schema(description = "Email address", example = "john.doe@example.com") @Email @Size(max = 254) String email,
        /**
         * Présent pour compat front, mais jamais renvoyé en clair.
         * Les réponses API doivent renvoyer une chaîne vide.
         */
        @Schema(description = "Password (request only). In API responses this must be an empty string.", example = "") @Size(max = 72) String password,
        @Schema(description = "Role", example = "USER") @Size(max = 30) String role,
        @Schema(description = "Creation timestamp (UTC)", example = "2024-01-01T12:00:00Z") @PastOrPresent Instant createdAt) {
}
