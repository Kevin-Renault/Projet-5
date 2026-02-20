package com.openclassrooms.mddapi.security;

import com.openclassrooms.mddapi.entity.RefreshTokenEntity;
import com.openclassrooms.mddapi.repository.RefreshTokenRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Objects;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RefreshTokenService {

    public record RefreshRotationResult(Long userId, String newRefreshToken) {
    }

    private final RefreshTokenRepository repository;
    private final long refreshExpirationSeconds;
    private final SecureRandom secureRandom;
    private final ObjectProvider<RefreshTokenService> selfProvider;

    public RefreshTokenService(
            RefreshTokenRepository repository,
            @Value("${security.jwt.refresh-expiration-seconds:2592000}") long refreshExpirationSeconds,
            ObjectProvider<RefreshTokenService> selfProvider) {
        this.repository = repository;
        this.refreshExpirationSeconds = refreshExpirationSeconds;
        this.secureRandom = new SecureRandom();
        this.selfProvider = selfProvider;
    }

    @Transactional
    public String issueAndReplaceForUser(Long userId) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid user id");
        }

        repository.deleteByUserId(userId);
        // Ensure the UNIQUE(user_id) constraint won't be hit when inserting the
        // new token in the same transaction.
        repository.flush();

        String rawToken = generateOpaqueToken();
        RefreshTokenEntity entity = new RefreshTokenEntity();
        entity.setUserId(userId);
        entity.setTokenHash(hash(rawToken));
        entity.setExpiresAt(Instant.now().plusSeconds(refreshExpirationSeconds));
        repository.save(entity);

        return rawToken;
    }

    @Transactional
    public void revokeForUser(Long userId) {
        if (userId == null) {
            return;
        }
        repository.deleteByUserId(userId);
    }

    @Transactional
    public void revokePresentedToken(String presentedRefreshToken) {
        if (presentedRefreshToken == null || presentedRefreshToken.isBlank()) {
            return;
        }
        repository.deleteByTokenHash(hash(presentedRefreshToken));
    }

    @Transactional
    public RefreshRotationResult rotate(String presentedRefreshToken) {
        if (presentedRefreshToken == null || presentedRefreshToken.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing refresh token");
        }

        String tokenHash = hash(presentedRefreshToken);
        RefreshTokenEntity existing = repository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

        if (existing.getExpiresAt().isBefore(Instant.now())) {
            repository.delete(existing);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Expired refresh token");
        }

        Long userId = existing.getUserId();
        repository.delete(existing);
        String newRefreshToken = selfProvider.getObject().issueAndReplaceForUser(userId);
        return new RefreshRotationResult(userId, newRefreshToken);
    }

    private String generateOpaqueToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String rawToken) {
        Objects.requireNonNull(rawToken, "rawToken");
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashed);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }
}
