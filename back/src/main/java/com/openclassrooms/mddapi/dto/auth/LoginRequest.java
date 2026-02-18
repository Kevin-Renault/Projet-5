package com.openclassrooms.mddapi.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Login request payload.")
public record LoginRequest(
        @Schema(description = "Email address", example = "john.doe@example.com") @NotBlank @Size(max = 254) String email,
        @Schema(description = "Password", example = "StrongP@ssw0rd") @NotBlank @Size(max = 72) String password) {
}
