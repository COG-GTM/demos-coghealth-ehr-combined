package com.medchart.ehr.domain.auth;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "user_sessions", indexes = {
        @Index(name = "idx_user_sessions_session_id", columnList = "session_id"),
        @Index(name = "idx_user_sessions_username", columnList = "username")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false, unique = true, length = 64)
    private String sessionId;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(name = "refresh_token_hash", nullable = false, length = 128)
    private String refreshTokenHash;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "last_activity_at", nullable = false)
    private Instant lastActivityAt;

    @Column(name = "absolute_expires_at", nullable = false)
    private Instant absoluteExpiresAt;

    @Column(nullable = false)
    private Boolean revoked = false;

    public boolean isActiveAt(Instant now, long idleTimeoutMs) {
        return !Boolean.TRUE.equals(revoked)
                && now.isBefore(absoluteExpiresAt)
                && now.isBefore(lastActivityAt.plusMillis(idleTimeoutMs));
    }
}
