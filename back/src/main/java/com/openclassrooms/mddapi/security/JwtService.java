package com.openclassrooms.mddapi.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final String secret;
    private final long expirationSeconds;

    public JwtService(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.expiration-seconds:86400}") long expirationSeconds) {
        this.secret = secret;
        this.expirationSeconds = expirationSeconds;
    }

    public String generateToken(String subject, Map<String, Object> extraClaims) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(expirationSeconds);

        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiry))
                .addClaims(extraClaims)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractSubject(String token) {
        return parse(token).getBody().getSubject();
    }

    public boolean isTokenValid(String token) {
        try {
            parse(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public Claims extractAllClaims(String token) {
        return parse(token).getBody();
    }

    private Jws<Claims> parse(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
    }

    private Key getSigningKey() {
        byte[] keyBytes = decodeSecret(secret);
        // HS256 => clé >= 256 bits (32 bytes). Keys.hmacShaKeyFor validera aussi,
        // mais on préfère un message explicite.
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException(
                    "JWT secret is too short: expected at least 32 bytes for HS256. "
                            + "Provide a longer JWT_SECRET (raw text) or a Base64/Base64URL encoded secret.");
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private static byte[] decodeSecret(String secret) {
        if (secret == null) {
            throw new IllegalArgumentException("JWT_SECRET is missing");
        }

        String trimmed = secret.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("JWT_SECRET is blank");
        }

        // Enforce standard Base64 ONLY.
        // This avoids ambiguity and prevents URL-safe secrets (containing '_' or '-')
        // from silently behaving differently.
        if (trimmed.contains("_") || trimmed.contains("-")) {
            throw new IllegalArgumentException(
                    "JWT_SECRET must be standard Base64 (characters A-Z a-z 0-9 + / and optional = padding). "
                            + "It looks like Base64URL (contains '_' or '-'). "
                            + "Generate a standard Base64 secret instead.");
        }

        boolean looksBase64 = trimmed.matches("^[A-Za-z0-9+/=]+$");
        if (!looksBase64) {
            throw new IllegalArgumentException(
                    "JWT_SECRET must be standard Base64 (characters A-Z a-z 0-9 + / and optional = padding). "
                            + "Raw text secrets are not accepted in this project.");
        }

        return Decoders.BASE64.decode(trimmed);
    }
}
