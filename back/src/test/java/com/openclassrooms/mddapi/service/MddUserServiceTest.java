package com.openclassrooms.mddapi.service;

import com.openclassrooms.mddapi.dto.UserDto;
import com.openclassrooms.mddapi.entity.MddUserEntity;
import com.openclassrooms.mddapi.mapper.UserMapper;
import com.openclassrooms.mddapi.repository.MddUserRepository;
import java.time.Instant;
import java.util.List;
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
    void getAll_maps_entities() {
        MddUserEntity e1 = new MddUserEntity();
        e1.setId(1L);
        MddUserEntity e2 = new MddUserEntity();
        e2.setId(2L);

        Mockito.when(userRepository.findAll()).thenReturn(List.of(e1, e2));
        Mockito.when(mapper.toDto(e1)).thenReturn(new UserDto(1L, "u1", "e1", "", "user", Instant.now()));
        Mockito.when(mapper.toDto(e2)).thenReturn(new UserDto(2L, "u2", "e2", "", "user", Instant.now()));

        Assertions.assertThat(service.getAll()).hasSize(2);
    }

    @Test
    void getById_404_when_missing() {
        Mockito.when(userRepository.findById(99L)).thenReturn(Optional.empty());
        Assertions.assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404");
    }

    @Test
    void getById_maps_entity() {
        MddUserEntity entity = new MddUserEntity();
        entity.setId(2L);
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(entity));

        UserDto dto = new UserDto(2L, "u2", "e2", "", "user", Instant.now());
        Mockito.when(mapper.toDto(entity)).thenReturn(dto);

        Assertions.assertThat(service.getById(2L)).isSameAs(dto);
    }

    @Test
    void update_requires_auth_and_existing_user() {
        UserDto dto = new UserDto(null, "u", "e@example.com", null, null, null);

        Assertions.assertThatThrownBy(() -> service.update(new MddUserEntity(), null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("400");

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
        Mockito.when(passwordEncoder.encode("Password123!")).thenReturn("encoded");

        ArgumentCaptor<MddUserEntity> toSaveCaptor = ArgumentCaptor.forClass(MddUserEntity.class);
        Mockito.when(userRepository.save(toSaveCaptor.capture())).thenReturn(existing);
        Mockito.when(mapper.toDto(existing)).thenReturn(new UserDto(1L, "new", "new@example.com", "", "user", null));

        UserDto out = service.update(principal,
                new UserDto(null, "new", "new@example.com", "Password123!", null, null));
        Assertions.assertThat(out.username()).isEqualTo("new");
        Assertions.assertThat(existing.getUsername()).isEqualTo("new");
        Assertions.assertThat(existing.getEmail()).isEqualTo("new@example.com");

        MddUserEntity saved = toSaveCaptor.getValue();
        Assertions.assertThat(saved.getPassword()).isEqualTo("encoded");
    }

    @Test
    void update_rejects_invalid_password() {
        MddUserEntity principal = new MddUserEntity();
        principal.setId(1L);

        MddUserEntity existing = new MddUserEntity();
        existing.setId(1L);
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(existing));

        UserDto invalidPassword = new UserDto(null, null, null, "weak", null, null);
        Assertions.assertThatThrownBy(() -> service.update(principal, invalidPassword))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("400");
    }
}
