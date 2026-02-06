package com.openclassrooms.mddapi.dto.auth;

import com.openclassrooms.mddapi.dto.UserDto;

public record AuthResponseDto(
        String token,
        UserDto user) {
}
