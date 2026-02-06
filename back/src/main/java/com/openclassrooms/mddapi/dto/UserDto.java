package com.openclassrooms.mddapi.dto;

import java.time.Instant;

public record UserDto(
                Long id,
                String username,
                String email,
                Instant createdAt) {
}
