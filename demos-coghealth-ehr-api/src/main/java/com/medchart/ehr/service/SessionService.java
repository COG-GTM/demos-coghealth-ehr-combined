package com.medchart.ehr.service;

import com.medchart.ehr.domain.auth.UserSession;
import com.medchart.ehr.repository.UserSessionRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

/**
 * Server-side registry of clinician sessions backing the stateless JWT layer.
 * Enforces idle and absolute session timeouts and allows immediate revocation.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SessionService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();

    private final UserSessionRepository userSessionRepository;

    @Value("${medchart.security.session.idle-timeout}")
    private long idleTimeoutMs;

    @Value("${medchart.security.session.absolute-timeout}")
    private long absoluteTimeoutMs;

    @Getter
    public static class IssuedSession {
        private final String sessionId;
        private final String refreshToken;

        IssuedSession(String sessionId, String refreshToken) {
            this.sessionId = sessionId;
            this.refreshToken = refreshToken;
        }
    }

    @Transactional
    public IssuedSession startSession(String username) {
        String sessionId = UUID.randomUUID().toString();
        String secret = newSecret();
        Instant now = Instant.now();

        userSessionRepository.save(UserSession.builder()
                .sessionId(sessionId)
                .username(username)
                .refreshTokenHash(hash(secret))
                .lastActivityAt(now)
                .absoluteExpiresAt(now.plusMillis(absoluteTimeoutMs))
                .revoked(false)
                .build());

        log.info("Started session {} for user {}", sessionId, username);
        return new IssuedSession(sessionId, sessionId + "." + secret);
    }

    /**
     * Confirms the session is still live and records the request as activity.
     * Returns empty when the session is unknown, revoked, idle-expired or past its absolute lifetime.
     */
    @Transactional
    public Optional<UserSession> validateAndTouch(String sessionId, String username) {
        Instant now = Instant.now();
        Optional<UserSession> session = userSessionRepository.findBySessionId(sessionId)
                .filter(s -> s.getUsername().equals(username))
                .filter(s -> s.isActiveAt(now, idleTimeoutMs));

        session.ifPresent(s -> {
            s.setLastActivityAt(now);
            userSessionRepository.save(s);
        });
        return session;
    }

    /**
     * Validates a refresh token and rotates its secret, invalidating the presented one.
     */
    @Transactional
    public Optional<IssuedSession> rotate(String refreshToken) {
        if (refreshToken == null || !refreshToken.contains(".")) {
            return Optional.empty();
        }
        int separator = refreshToken.indexOf('.');
        String sessionId = refreshToken.substring(0, separator);
        String secret = refreshToken.substring(separator + 1);
        Instant now = Instant.now();

        Optional<UserSession> session = userSessionRepository.findBySessionId(sessionId)
                .filter(s -> s.isActiveAt(now, idleTimeoutMs))
                .filter(s -> constantTimeEquals(s.getRefreshTokenHash(), hash(secret)));

        if (!session.isPresent()) {
            log.warn("Rejected refresh attempt for session {}", sessionId);
            return Optional.empty();
        }

        UserSession userSession = session.get();
        String rotatedSecret = newSecret();
        userSession.setRefreshTokenHash(hash(rotatedSecret));
        userSession.setLastActivityAt(now);
        userSessionRepository.save(userSession);

        return Optional.of(new IssuedSession(sessionId, sessionId + "." + rotatedSecret));
    }

    public Optional<UserSession> findActive(String sessionId) {
        return userSessionRepository.findBySessionId(sessionId)
                .filter(s -> s.isActiveAt(Instant.now(), idleTimeoutMs));
    }

    @Transactional
    public void revoke(String sessionId) {
        userSessionRepository.findBySessionId(sessionId).ifPresent(s -> {
            s.setRevoked(true);
            userSessionRepository.save(s);
            log.info("Revoked session {}", sessionId);
        });
    }

    @Transactional
    public void revokeAllForUser(String username) {
        int revoked = userSessionRepository.revokeAllForUsername(username);
        log.info("Revoked {} session(s) for user {}", revoked, username);
    }

    @Transactional
    public void purgeExpired() {
        userSessionRepository.deleteExpiredBefore(Instant.now());
    }

    private String newSecret() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return ENCODER.encodeToString(bytes);
    }

    private String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return ENCODER.encodeToString(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    private boolean constantTimeEquals(String a, String b) {
        return MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
    }
}
