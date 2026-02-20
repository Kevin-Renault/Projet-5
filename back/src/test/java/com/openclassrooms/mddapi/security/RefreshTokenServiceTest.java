package com.openclassrooms.mddapi.security;

import com.openclassrooms.mddapi.entity.RefreshTokenEntity;
import com.openclassrooms.mddapi.repository.RefreshTokenRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.server.ResponseStatusException;

class RefreshTokenServiceTest {

    private static final String TOKEN_NAME = "token";
    @Test
    void issueAndReplaceForUserSavesHashedToken() {
        RefreshTokenRepository repo = Mockito.mock(RefreshTokenRepository.class);
        Mockito.when(repo.save(Mockito.any())).thenAnswer(inv -> inv.getArgument(0));

        @SuppressWarnings("unchecked")
        ObjectProvider<RefreshTokenService> selfProvider = Mockito.mock(ObjectProvider.class);
        RefreshTokenService service = new RefreshTokenService(repo, 3600, selfProvider);

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
    void rotateMissingTokenThrowsUnauthorized() {
        RefreshTokenRepository repo = Mockito.mock(RefreshTokenRepository.class);

        @SuppressWarnings("unchecked")
        ObjectProvider<RefreshTokenService> selfProvider = Mockito.mock(ObjectProvider.class);
        RefreshTokenService service = new RefreshTokenService(repo, 3600, selfProvider);

        ResponseStatusException ex = Assertions.catchThrowableOfType(
                () -> service.rotate("  "),
                ResponseStatusException.class);
        Assertions.assertThat(ex.getStatusCode().value()).isEqualTo(401);
    }

    @Test
    void rotateUnknownTokenThrowsUnauthorized() {
        RefreshTokenRepository repo = Mockito.mock(RefreshTokenRepository.class);
        Mockito.when(repo.findByTokenHash(Mockito.anyString())).thenReturn(Optional.empty());

        @SuppressWarnings("unchecked")
        ObjectProvider<RefreshTokenService> selfProvider = Mockito.mock(ObjectProvider.class);
        RefreshTokenService service = new RefreshTokenService(repo, 3600, selfProvider);

        ResponseStatusException ex = Assertions.catchThrowableOfType(
                () -> service.rotate(TOKEN_NAME),
                ResponseStatusException.class);
        Assertions.assertThat(ex.getStatusCode().value()).isEqualTo(401);
    }

    @Test
    void rotateExpiredTokenDeletesAndThrowsUnauthorized() {
        RefreshTokenRepository repo = Mockito.mock(RefreshTokenRepository.class);
        RefreshTokenEntity existing = new RefreshTokenEntity();
        existing.setUserId(1L);
        existing.setTokenHash("h");
        existing.setExpiresAt(Instant.now().minusSeconds(10));

        Mockito.when(repo.findByTokenHash(Mockito.anyString())).thenReturn(Optional.of(existing));

    @SuppressWarnings("unchecked")
    ObjectProvider<RefreshTokenService> selfProvider = Mockito.mock(ObjectProvider.class);
    RefreshTokenService service = new RefreshTokenService(repo, 3600, selfProvider);

        ResponseStatusException ex = Assertions.catchThrowableOfType(
                () -> service.rotate(TOKEN_NAME),
                ResponseStatusException.class);
        Assertions.assertThat(ex.getStatusCode().value()).isEqualTo(401);
        Mockito.verify(repo).delete(existing);
    }

    @Test
    void rotateValidTokenRotatesAndReturnsNewToken() {
        RefreshTokenRepository repo = Mockito.mock(RefreshTokenRepository.class);
        RefreshTokenEntity existing = new RefreshTokenEntity();
        existing.setUserId(7L);
        existing.setTokenHash("h");
        existing.setExpiresAt(Instant.now().plusSeconds(60));

        Mockito.when(repo.findByTokenHash(Mockito.anyString())).thenReturn(Optional.of(existing));
        Mockito.when(repo.save(Mockito.any())).thenAnswer(inv -> inv.getArgument(0));

        @SuppressWarnings("unchecked")
        ObjectProvider<RefreshTokenService> selfProvider = Mockito.mock(ObjectProvider.class);
        AtomicReference<RefreshTokenService> selfRef = new AtomicReference<>();
        Mockito.when(selfProvider.getObject()).thenAnswer(inv -> selfRef.get());

        RefreshTokenService service = new RefreshTokenService(repo, 3600, selfProvider);
        selfRef.set(service);
        RefreshTokenService.RefreshRotationResult result = service.rotate(TOKEN_NAME);

        Assertions.assertThat(result.userId()).isEqualTo(7L);
        Assertions.assertThat(result.newRefreshToken()).isNotBlank();
        Mockito.verify(repo).delete(existing);
        Mockito.verify(repo).deleteByUserId(7L);
        Mockito.verify(repo).save(Mockito.any(RefreshTokenEntity.class));
    }
}
