package com.openclassrooms.mddapi.service;

import com.openclassrooms.mddapi.dto.auth.LoginRequest;
import com.openclassrooms.mddapi.dto.auth.RegisterRequest;
import com.openclassrooms.mddapi.entity.MddUserEntity;
import com.openclassrooms.mddapi.repository.MddUserRepository;
import com.openclassrooms.mddapi.security.JwtService;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private MddUserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService service;

    @Test
    void register_rejects_null_and_missing_fields() {
        Assertions.assertThatThrownBy(() -> service.register(null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("400");

        RegisterRequest missingUsername = new RegisterRequest(null, "e@example.com", "Password123!");
        Assertions.assertThatThrownBy(() -> service.register(missingUsername))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("400");

        RegisterRequest missingEmail = new RegisterRequest("u", null, "Password123!");
        Assertions.assertThatThrownBy(() -> service.register(missingEmail))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("400");

        RegisterRequest missingPassword = new RegisterRequest("u", "e@example.com", null);
        Assertions.assertThatThrownBy(() -> service.register(missingPassword))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("400");
    }

    @Test
    void register_rejects_username_conflict() {
        RegisterRequest request = new RegisterRequest("u", "e@example.com", "Password123!");

        Mockito.when(userRepository.existsByEmail("e@example.com")).thenReturn(false);
        Mockito.when(userRepository.existsByUsername("u")).thenReturn(true);

        Assertions.assertThatThrownBy(() -> service.register(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("409");
    }

    @Test
    void login_rejects_null_and_missing_fields() {
        Assertions.assertThatThrownBy(() -> service.login(null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("400");

        LoginRequest missingEmail = new LoginRequest(null, "Password123!");
        Assertions.assertThatThrownBy(() -> service.login(missingEmail))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("400");

        LoginRequest missingPassword = new LoginRequest("e@example.com", null);
        Assertions.assertThatThrownBy(() -> service.login(missingPassword))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("400");
    }

    @Test
    void login_rejects_when_password_does_not_match() {
        LoginRequest request = new LoginRequest("e@example.com", "Password123!");

        MddUserEntity user = new MddUserEntity();
        user.setId(1L);
        user.setEmail("e@example.com");
        user.setUsername("u");
        user.setPassword("hashed");

        Mockito.when(userRepository.findByEmail("e@example.com")).thenReturn(Optional.of(user));
        Mockito.when(passwordEncoder.matches("Password123!", "hashed")).thenReturn(false);

        Assertions.assertThatThrownBy(() -> service.login(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("401");

        Mockito.verify(jwtService, Mockito.never()).generateToken(Mockito.anyString(), Mockito.anyMap());
    }
}
