package com.medchart.ehr.config;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtTokenProviderTest {

    // HS512 requires a key of at least 512 bits (64 bytes).
    private static final String SECRET =
            "test-secret-key-that-is-long-enough-for-hs512-signing-algorithm-1234567890";
    private static final int EXPIRATION_MS = 3_600_000; // 1 hour

    private JwtTokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(tokenProvider, "jwtSecret", SECRET);
        ReflectionTestUtils.setField(tokenProvider, "jwtExpirationInMs", EXPIRATION_MS);
    }

    private UserDetails userDetails(String username) {
        return new User(username, "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_PROVIDER")));
    }

    @Test
    void generateTokenFromUsername_roundTripsUsername() {
        String token = tokenProvider.generateTokenFromUsername("dr.house");

        assertThat(token).isNotBlank();
        assertThat(tokenProvider.getUsernameFromToken(token)).isEqualTo("dr.house");
    }

    @Test
    void generateToken_usesPrincipalUsernameAsSubject() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails("jane.doe"));

        String token = tokenProvider.generateToken(authentication);

        assertThat(tokenProvider.getUsernameFromToken(token)).isEqualTo("jane.doe");
    }

    @Test
    void getExpirationDateFromToken_isInTheFuture() {
        Date before = new Date();
        String token = tokenProvider.generateTokenFromUsername("clock.watcher");

        Date expiration = tokenProvider.getExpirationDateFromToken(token);

        assertThat(expiration).isAfter(before);
        // expiry should be roughly now + EXPIRATION_MS (allow generous slack for clock/exec time)
        long deltaMs = expiration.getTime() - before.getTime();
        assertThat(deltaMs).isBetween((long) EXPIRATION_MS - 5_000L, (long) EXPIRATION_MS + 5_000L);
    }

    @Test
    void validateToken_returnsTrueForMatchingUserAndUnexpiredToken() {
        String token = tokenProvider.generateTokenFromUsername("match.user");

        assertThat(tokenProvider.validateToken(token, userDetails("match.user"))).isTrue();
    }

    @Test
    void validateToken_returnsFalseWhenUsernameDoesNotMatch() {
        String token = tokenProvider.generateTokenFromUsername("token.user");

        assertThat(tokenProvider.validateToken(token, userDetails("different.user"))).isFalse();
    }

    @Test
    void getUsernameFromToken_throwsForTokenSignedWithDifferentKey() {
        JwtTokenProvider otherProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(otherProvider, "jwtSecret",
                "a-completely-different-secret-key-also-long-enough-for-hs512-0987654321");
        ReflectionTestUtils.setField(otherProvider, "jwtExpirationInMs", EXPIRATION_MS);
        String foreignToken = otherProvider.generateTokenFromUsername("intruder");

        assertThatThrownBy(() -> tokenProvider.getUsernameFromToken(foreignToken))
                .isInstanceOf(SignatureException.class);
    }

    @Test
    void parsingExpiredToken_throwsExpiredJwtException() {
        JwtTokenProvider expiredProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(expiredProvider, "jwtSecret", SECRET);
        // negative expiration => token expires in the past immediately
        ReflectionTestUtils.setField(expiredProvider, "jwtExpirationInMs", -1_000);
        String expiredToken = expiredProvider.generateTokenFromUsername("expired.user");

        assertThatThrownBy(() -> tokenProvider.getUsernameFromToken(expiredToken))
                .isInstanceOf(ExpiredJwtException.class);
    }
}
