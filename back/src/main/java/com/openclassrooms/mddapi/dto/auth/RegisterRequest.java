package com.openclassrooms.mddapi.dto.auth;

public record RegisterRequest(
        String username,
        String email,
        String password) {
}
