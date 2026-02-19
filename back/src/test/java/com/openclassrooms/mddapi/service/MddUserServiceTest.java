package com.openclassrooms.mddapi.service;

import com.openclassrooms.mddapi.dto.UserDto;
import com.openclassrooms.mddapi.entity.MddUserEntity;
import com.openclassrooms.mddapi.mapper.UserMapper;
import com.openclassrooms.mddapi.repository.MddUserRepository;
import java.time.Instant;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class MddUserServiceTest {

    @Mock
    private MddUserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper mapper;

    @InjectMocks
    private MddUserService service;

    @Test
    void create_rejects_null_and_missing_fields() {
        Assertions.assertThatThrownBy(() -> service.create(null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("400");

        UserDto missing = new UserDto(null, null, null, null, null, null);
        Assertions.assertThatThrownBy(() -> service.create(missing))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("400");
    }

    @Test
    void create_rejects_conflicts() {
        UserDto dto = new UserDto(null, "u", "e@example.com", "Password123!", null, null);

        Mockito.when(userRepository.existsByEmail("e@example.com")).thenReturn(true);
        Assertions.assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("409");

        Mockito.when(userRepository.existsByEmail("e@example.com")).thenReturn(false);
        Mockito.when(userRepository.existsByUsername("u")).thenReturn(true);
        Assertions.assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("409");
    }

    @Test
    void create_happy_path_encodes_and_maps() {
        UserDto dto = new UserDto(null, "u", "e@example.com", "Password123!", null, null);

        Mockito.when(userRepository.existsByEmail("e@example.com")).thenReturn(false);
        Mockito.when(userRepository.existsByUsername("u")).thenReturn(false);
        Mockito.when(passwordEncoder.encode("Password123!")).thenReturn("encoded");

        MddUserEntity saved = new MddUserEntity();
        saved.setId(1L);
        saved.setCreatedAt(Instant.now());

        ArgumentCaptor<MddUserEntity> toSaveCaptor = ArgumentCaptor.forClass(MddUserEntity.class);
        Mockito.when(userRepository.save(toSaveCaptor.capture())).thenReturn(saved);

        UserDto mapped = new UserDto(1L, "u", "e@example.com", "", "user", saved.getCreatedAt());
        Mockito.when(mapper.toDto(saved)).thenReturn(mapped);

        UserDto out = service.create(dto);
        Assertions.assertThat(out).isSameAs(mapped);

        MddUserEntity toSave = toSaveCaptor.getValue();
        Assertions.assertThat(toSave.getUsername()).isEqualTo("u");
        Assertions.assertThat(toSave.getEmail()).isEqualTo("e@example.com");
        Assertions.assertThat(toSave.getPassword()).isEqualTo("encoded");
    }

    @Test
    void update_requires_auth_and_existing_user() {
        UserDto dto = new UserDto(null, "u", "e@example.com", null, null, null);

        Assertions.assertThatThrownBy(() -> service.update(null, dto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("401");

        MddUserEntity principal = new MddUserEntity();
        principal.setId(1L);

        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.empty());
        Assertions.assertThatThrownBy(() -> service.update(principal, dto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404");
    }

    @Test
    void update_checks_conflicts_and_updates_fields() {
        MddUserEntity principal = new MddUserEntity();
        principal.setId(1L);

        MddUserEntity existing = new MddUserEntity();
        existing.setId(1L);
        existing.setUsername("old");
        existing.setEmail("old@example.com");
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(existing));

        // username conflict
        MddUserEntity other = new MddUserEntity();
        other.setId(2L);
        Mockito.when(userRepository.findByUsername("new")).thenReturn(Optional.of(other));

        UserDto userDto = new UserDto(null, "new", "old@example.com", null, null, null);

        Assertions
                .assertThatThrownBy(
                        () -> service.update(principal, userDto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("409");

        // email conflict
        Mockito.when(userRepository.findByUsername("new")).thenReturn(Optional.empty());
        Mockito.when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.of(other));
        UserDto emailConflictDto = new UserDto(null, "old", "new@example.com", null, null, null);
        Assertions
                .assertThatThrownBy(
                        () -> service.update(principal, emailConflictDto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("409");

        // happy update
        Mockito.when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        Mockito.when(userRepository.save(existing)).thenReturn(existing);
        Mockito.when(mapper.toDto(existing)).thenReturn(new UserDto(1L, "new", "new@example.com", "", "user", null));

        UserDto out = service.update(principal, new UserDto(null, "new", "new@example.com", null, null, null));
        Assertions.assertThat(out.username()).isEqualTo("new");
        Assertions.assertThat(existing.getUsername()).isEqualTo("new");
        Assertions.assertThat(existing.getEmail()).isEqualTo("new@example.com");
    }
}
