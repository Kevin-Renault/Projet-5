package com.openclassrooms.mddapi.security;

import com.openclassrooms.mddapi.entity.RefreshTokenEntity;
import com.openclassrooms.mddapi.repository.RefreshTokenRepository;
import java.time.Instant;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.web.server.ResponseStatusException;

class RefreshTokenServiceTest {

    @Test
    void issueAndReplaceForUser_saves_hashed_token() {
        RefreshTokenRepository repo = Mockito.mock(RefreshTokenRepository.class);
        Mockito.when(repo.save(Mockito.any())).thenAnswer(inv -> inv.getArgument(0));

        RefreshTokenService service = new RefreshTokenService(repo, 3600);

        Instant before = Instant.now();
        String token = service.issueAndReplaceForUser(12L);

        Assertions.assertThat(token).isNotBlank();

        ArgumentCaptor<RefreshTokenEntity> captor = ArgumentCaptor.forClass(RefreshTokenEntity.class);
        Mockito.verify(repo).deleteByUserId(12L);
        Mockito.verify(repo).save(captor.capture());

        RefreshTokenEntity saved = captor.getValue();
        Assertions.assertThat(saved.getUserId()).isEqualTo(12L);
        Assertions.assertThat(saved.getTokenHash()).isNotBlank();
        Assertions.assertThat(saved.getExpiresAt()).isAfter(before);
    }

    @Test
    void rotate_missing_token_throws_unauthorized() {
        RefreshTokenRepository repo = Mockito.mock(RefreshTokenRepository.class);
        RefreshTokenService service = new RefreshTokenService(repo, 3600);

        ResponseStatusException ex = Assertions.catchThrowableOfType(
                () -> service.rotate("  "),
                ResponseStatusException.class);
        Assertions.assertThat(ex.getStatusCode().value()).isEqualTo(401);
    }

    @Test
    void rotate_unknown_token_throws_unauthorized() {
        RefreshTokenRepository repo = Mockito.mock(RefreshTokenRepository.class);
        Mockito.when(repo.findByTokenHash(Mockito.anyString())).thenReturn(Optional.empty());

        RefreshTokenService service = new RefreshTokenService(repo, 3600);

        ResponseStatusException ex = Assertions.catchThrowableOfType(
                () -> service.rotate("token"),
                ResponseStatusException.class);
        Assertions.assertThat(ex.getStatusCode().value()).isEqualTo(401);
    }

    @Test
    void rotate_expired_token_deletes_and_throws_unauthorized() {
        RefreshTokenRepository repo = Mockito.mock(RefreshTokenRepository.class);
        RefreshTokenEntity existing = new RefreshTokenEntity();
        existing.setUserId(1L);
        existing.setTokenHash("h");
        existing.setExpiresAt(Instant.now().minusSeconds(10));

        Mockito.when(repo.findByTokenHash(Mockito.anyString())).thenReturn(Optional.of(existing));

        RefreshTokenService service = new RefreshTokenService(repo, 3600);

        ResponseStatusException ex = Assertions.catchThrowableOfType(
                () -> service.rotate("token"),
                ResponseStatusException.class);
        Assertions.assertThat(ex.getStatusCode().value()).isEqualTo(401);
        Mockito.verify(repo).delete(existing);
    }

    @Test
    void rotate_valid_token_rotates_and_returns_new_token() {
        RefreshTokenRepository repo = Mockito.mock(RefreshTokenRepository.class);
        RefreshTokenEntity existing = new RefreshTokenEntity();
        existing.setUserId(7L);
        existing.setTokenHash("h");
        existing.setExpiresAt(Instant.now().plusSeconds(60));

        Mockito.when(repo.findByTokenHash(Mockito.anyString())).thenReturn(Optional.of(existing));
        Mockito.when(repo.save(Mockito.any())).thenAnswer(inv -> inv.getArgument(0));

        RefreshTokenService service = new RefreshTokenService(repo, 3600);
        RefreshTokenService.RefreshRotationResult result = service.rotate("token");

        Assertions.assertThat(result.userId()).isEqualTo(7L);
        Assertions.assertThat(result.newRefreshToken()).isNotBlank();
        Mockito.verify(repo).delete(existing);
        Mockito.verify(repo).deleteByUserId(7L);
        Mockito.verify(repo).save(Mockito.any(RefreshTokenEntity.class));
    }
}
