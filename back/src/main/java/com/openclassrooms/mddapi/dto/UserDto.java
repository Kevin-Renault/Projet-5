package com.openclassrooms.mddapi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record UserDto(
                @Positive Long id,
                @Size(max = 50) String username,
                @Email @Size(max = 254) String email,
                /**
                 * Présent pour compat front, mais jamais renvoyé en clair.
                 * Les réponses API doivent renvoyer une chaîne vide.
                 */
                @Size(max = 72) String password,
                @Size(max = 30) String role,
                @PastOrPresent Instant createdAt) {
}
