package com.openclassrooms.mddapi.service;

import com.openclassrooms.mddapi.dto.UserDto;
import com.openclassrooms.mddapi.entity.MddUserEntity;
import com.openclassrooms.mddapi.mapper.UserMapper;
import com.openclassrooms.mddapi.repository.MddUserRepository;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MddUserService {

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[#?!@$%^&*-]).{8,}$");

    private final MddUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public MddUserService(MddUserRepository userRepository, PasswordEncoder passwordEncoder, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    @Transactional(readOnly = true)
    public List<UserDto> getAll() {
        return userRepository.findAll().stream().map(userMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public UserDto getById(Long id) {
        MddUserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return userMapper.toDto(user);
    }

    @Transactional
    public UserDto create(UserDto request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid payload");
        }

        String username = trimToNull(request.username());
        String email = trimToNull(request.email());
        String password = trimToNull(request.password());

        if (username == null || email == null || password == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing required fields");
        }

        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already used");
        }
        if (userRepository.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already used");
        }

        requireValidPassword(password);

        MddUserEntity entity = new MddUserEntity();
        entity.setUsername(username);
        entity.setEmail(email);
        entity.setPassword(passwordEncoder.encode(password));

        MddUserEntity saved = userRepository.save(entity);
        return userMapper.toDto(saved);
    }

    @Transactional
    public UserDto update(Long id, MddUserEntity principal, UserDto request) {
        Long principalId = requireAuthenticatedUserId(principal);
        if (!principalId.equals(id)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
        }

        MddUserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid payload");
        }

        String newUsername = trimToNull(request.username());
        String newEmail = trimToNull(request.email());
        String newPassword = trimToNull(request.password());

        if (newUsername != null && !newUsername.equals(user.getUsername())) {
            userRepository.findByUsername(newUsername)
                    .filter(u -> !u.getId().equals(user.getId()))
                    .ifPresent(u -> {
                        throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already used");
                    });
            user.setUsername(newUsername);
        }

        if (newEmail != null && !newEmail.equals(user.getEmail())) {
            userRepository.findByEmail(newEmail)
                    .filter(u -> !u.getId().equals(user.getId()))
                    .ifPresent(u -> {
                        throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already used");
                    });
            user.setEmail(newEmail);
        }

        if (newPassword != null) {
            requireValidPassword(newPassword);
            user.setPassword(passwordEncoder.encode(newPassword));
        }

        MddUserEntity saved = userRepository.save(user);
        return userMapper.toDto(saved);
    }

    private static Long requireAuthenticatedUserId(MddUserEntity principal) {
        if (principal == null || principal.getId() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        return principal.getId();
    }

    private static void requireValidPassword(String password) {
        if (password == null || !PASSWORD_PATTERN.matcher(password).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password does not meet requirements");
        }
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
