package com.medchart.ehr.config;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenProviderTest {

    private static final String SECRET =
            "test-secret-key-for-unit-tests-that-is-long-enough-for-hs512-signing-algorithm-0123456789";

    private JwtTokenProvider tokenProvider;

    private static UserDetails userDetails(String username) {
        return new User(username, "password", AuthorityUtils.createAuthorityList("ROLE_PROVIDER"));
    }

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(tokenProvider, "jwtSecret", SECRET);
        ReflectionTestUtils.setField(tokenProvider, "jwtExpirationInMs", 60_000);
    }

    @Test
    void generatedTokenCarriesAuthenticatedUsername() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails("sanderson"), "password", userDetails("sanderson").getAuthorities());

        String token = tokenProvider.generateToken(authentication);

        assertThat(tokenProvider.getUsernameFromToken(token)).isEqualTo("sanderson");
    }

    @Test
    void tokenExpiresAfterConfiguredWindow() {
        String token = tokenProvider.generateTokenFromUsername("sanderson");

        Date expiration = tokenProvider.getExpirationDateFromToken(token);

        assertThat(expiration).isAfter(new Date());
        assertThat(expiration).isBefore(new Date(System.currentTimeMillis() + 61_000));
    }

    @Test
    void validateTokenAcceptsMatchingUser() {
        String token = tokenProvider.generateTokenFromUsername("sanderson");

        assertThat(tokenProvider.validateToken(token, userDetails("sanderson"))).isTrue();
    }

    @Test
    void validateTokenRejectsDifferentUser() {
        String token = tokenProvider.generateTokenFromUsername("sanderson");

        assertThat(tokenProvider.validateToken(token, userDetails("intruder"))).isFalse();
    }

    @Test
    void expiredTokenIsRejected() {
        ReflectionTestUtils.setField(tokenProvider, "jwtExpirationInMs", -1_000);
        String token = tokenProvider.generateTokenFromUsername("sanderson");

        assertThatThrownBy(() -> tokenProvider.validateToken(token, userDetails("sanderson")))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void tokenSignedWithAnotherSecretIsRejected() {
        JwtTokenProvider otherProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(otherProvider, "jwtSecret",
                "another-secret-key-for-unit-tests-long-enough-for-hs512-signing-algorithm-9876543210");
        ReflectionTestUtils.setField(otherProvider, "jwtExpirationInMs", 60_000);
        String foreignToken = otherProvider.generateTokenFromUsername("sanderson");

        assertThatThrownBy(() -> tokenProvider.getUsernameFromToken(foreignToken))
                .isInstanceOf(SignatureException.class);
    }
}
