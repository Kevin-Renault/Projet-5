package com.openclassrooms.mddapi.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
                @NotBlank @Size(max = 50) String username,
                @NotBlank @Email @Size(max = 254) String email,
                @NotBlank @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[#?!@$%^&*-]).{8,}$") String password) {
}
