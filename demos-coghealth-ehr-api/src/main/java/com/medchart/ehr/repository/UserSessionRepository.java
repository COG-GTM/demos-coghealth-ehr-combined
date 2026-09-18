package com.medchart.ehr.repository;

import com.medchart.ehr.domain.auth.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    Optional<UserSession> findBySessionId(String sessionId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM UserSession s WHERE s.sessionId = :sessionId")
    Optional<UserSession> findBySessionIdForUpdate(String sessionId);

    @Modifying
    @Query("UPDATE UserSession s SET s.revoked = true WHERE s.username = :username AND s.revoked = false")
    int revokeAllForUsername(String username);

    @Modifying
    @Query("DELETE FROM UserSession s WHERE s.absoluteExpiresAt < :cutoff")
    int deleteExpiredBefore(Instant cutoff);
}
