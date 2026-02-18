package com.openclassrooms.mddapi.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Registration request payload.")
public record RegisterRequest(
        @Schema(description = "Username", example = "john_doe") @NotBlank @Size(max = 50) String username,
        @Schema(description = "Email address", example = "john.doe@example.com") @NotBlank @Email @Size(max = 254) String email,
        @Schema(description = "Password (must match the configured complexity policy)", example = "StrongP@ssw0rd") @NotBlank @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[#?!@$%^&*-]).{8,}$") String password) {
}
