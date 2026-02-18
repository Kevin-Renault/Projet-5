package com.openclassrooms.mddapi.dto.auth;

import com.openclassrooms.mddapi.dto.UserDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Authentication response containing a JWT and the authenticated user.")
public record AuthResponseDto(
        @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...") @NotBlank String token,
        @Schema(description = "Authenticated user") @NotNull @Valid UserDto user) {
}
