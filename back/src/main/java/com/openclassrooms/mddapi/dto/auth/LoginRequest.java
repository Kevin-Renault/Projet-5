package com.openclassrooms.mddapi.dto.auth;

public record LoginRequest(
        String email,
        String password) {
}
