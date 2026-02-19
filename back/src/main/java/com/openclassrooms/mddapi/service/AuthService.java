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

    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_USERNAME = "username";

    private static final String MSG_INVALID_REGISTER_PAYLOAD = "Invalid register payload";
    private static final String MSG_INVALID_LOGIN_PAYLOAD = "Invalid login payload";
    private static final String MSG_INVALID_CREDENTIALS = "Invalid credentials";
    private static final String MSG_INVALID_REFRESH_TOKEN = "Invalid refresh token";
    private static final String MSG_EMAIL_ALREADY_USED = "Email already used";
    private static final String MSG_USERNAME_ALREADY_USED = "Username already used";

    private static final String DEFAULT_ROLE = "user";

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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, MSG_INVALID_REGISTER_PAYLOAD);
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, MSG_EMAIL_ALREADY_USED);
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, MSG_USERNAME_ALREADY_USED);
        }

        MddUserEntity user = new MddUserEntity();
        user.setEmail(request.email());
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));

        MddUserEntity saved = userRepository.save(user);

        String token = jwtService.generateToken(
                String.valueOf(saved.getId()),
                Map.of(CLAIM_EMAIL, saved.getEmail(), CLAIM_USERNAME, saved.getUsername()));

        return new AuthResponseDto(token, toUserDto(saved));
    }

    @Transactional(readOnly = true)
    public AuthResponseDto login(LoginRequest request) {
        if (request == null || request.email() == null || request.password() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, MSG_INVALID_LOGIN_PAYLOAD);
        }

        // Le front mock autorise email OU username : on supporte les deux.
        MddUserEntity user = userRepository.findByEmail(request.email())
                .or(() -> userRepository.findByUsername(request.email()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, MSG_INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, MSG_INVALID_CREDENTIALS);
        }

        String token = jwtService.generateToken(
                String.valueOf(user.getId()),
                Map.of(CLAIM_EMAIL, user.getEmail(), CLAIM_USERNAME, user.getUsername()));

        return new AuthResponseDto(token, toUserDto(user));
    }

    @Transactional(readOnly = true)
    public AuthResponseDto refreshAccessToken(Long userId) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, MSG_INVALID_REFRESH_TOKEN);
        }

        MddUserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, MSG_INVALID_REFRESH_TOKEN));

        String token = jwtService.generateToken(
                String.valueOf(user.getId()),
                Map.of(CLAIM_EMAIL, user.getEmail(), CLAIM_USERNAME, user.getUsername()));

        return new AuthResponseDto(token, toUserDto(user));
    }

    public UserDto toUserDto(MddUserEntity user) {
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                "",
                DEFAULT_ROLE,
                user.getCreatedAt());
    }
}
