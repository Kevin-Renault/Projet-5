package com.openclassrooms.mddapi.dto.auth;

import com.openclassrooms.mddapi.dto.UserDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AuthResponseDto(
                @NotBlank String token,
                @NotNull @Valid UserDto user) {
}
