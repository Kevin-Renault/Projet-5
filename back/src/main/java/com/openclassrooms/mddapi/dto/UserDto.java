package com.openclassrooms.mddapi.dto;

import java.time.Instant;

public record UserDto(
        Long id,
        String username,
        String email,
        /**
         * Présent pour compat front, mais jamais renvoyé en clair.
         * Les réponses API doivent renvoyer une chaîne vide.
         */
        String password,
        String role,
        Instant createdAt) {
}
