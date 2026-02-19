package com.openclassrooms.mddapi.service;

import com.openclassrooms.mddapi.dto.UserDto;
import com.openclassrooms.mddapi.dto.auth.AuthResponseDto;
import com.openclassrooms.mddapi.dto.auth.LoginRequest;
import com.openclassrooms.mddapi.dto.auth.RegisterRequest;
import com.openclassrooms.mddapi.entity.MddUserEntity;
import com.openclassrooms.mddapi.repository.MddUserRepository;
import com.openclassrooms.mddapi.security.JwtService;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final MddUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(MddUserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponseDto register(RegisterRequest request) {
        if (request == null || request.email() == null || request.password() == null || request.username() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid register payload");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already used");
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already used");
        }

        MddUserEntity user = new MddUserEntity();
        user.setEmail(request.email());
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));

        MddUserEntity saved = userRepository.save(user);

        String token = jwtService.generateToken(
                String.valueOf(saved.getId()),
                Map.of("email", saved.getEmail(), "username", saved.getUsername()));

        return new AuthResponseDto(token, toUserDto(saved));
    }

    @Transactional(readOnly = true)
    public AuthResponseDto login(LoginRequest request) {
        if (request == null || request.email() == null || request.password() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid login payload");
        }

        // Le front mock autorise email OU username : on supporte les deux.
        MddUserEntity user = userRepository.findByEmail(request.email())
                .or(() -> userRepository.findByUsername(request.email()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        String token = jwtService.generateToken(
                String.valueOf(user.getId()),
                Map.of("email", user.getEmail(), "username", user.getUsername()));

        return new AuthResponseDto(token, toUserDto(user));
    }

    @Transactional(readOnly = true)
    public AuthResponseDto refreshAccessToken(Long userId) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }

        MddUserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

        String token = jwtService.generateToken(
                String.valueOf(user.getId()),
                Map.of("email", user.getEmail(), "username", user.getUsername()));

        return new AuthResponseDto(token, toUserDto(user));
    }

    public UserDto toUserDto(MddUserEntity user) {
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                "",
                "user",
                user.getCreatedAt());
    }
}
