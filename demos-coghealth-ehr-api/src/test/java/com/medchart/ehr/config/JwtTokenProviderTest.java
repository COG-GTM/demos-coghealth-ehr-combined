package com.medchart.ehr.config;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenProviderTest {

    private static final String SECRET =
            "test-secret-key-that-is-long-enough-for-hs512-signing-at-least-64-bytes!!";

    private JwtTokenProvider tokenProvider;
    private UserDetails userDetails;

    private static JwtTokenProvider providerWith(String secret, int expirationInMs) {
        JwtTokenProvider provider = new JwtTokenProvider();
        ReflectionTestUtils.setField(provider, "jwtSecret", secret);
        ReflectionTestUtils.setField(provider, "jwtExpirationInMs", expirationInMs);
        return provider;
    }

    @BeforeEach
    void setUp() {
        tokenProvider = providerWith(SECRET, 3_600_000);
        userDetails = new User("shopper", "hashed",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_PROVIDER")));
    }

    @Test
    @DisplayName("a token generated from an Authentication carries the username as subject")
    void generateTokenUsesPrincipalUsername() {
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        String token = tokenProvider.generateToken(authentication);

        assertThat(tokenProvider.getUsernameFromToken(token)).isEqualTo("shopper");
    }

    @Test
    @DisplayName("generateTokenFromUsername produces a token that validates for that user")
    void generateTokenFromUsernameValidates() {
        String token = tokenProvider.generateTokenFromUsername("shopper");

        assertThat(tokenProvider.validateToken(token, userDetails)).isTrue();
    }

    @Test
    @DisplayName("the expiry is the configured lifetime after issuing")
    void expirationHonoursConfiguredLifetime() {
        String token = tokenProvider.generateTokenFromUsername("shopper");

        Date expiration = tokenProvider.getExpirationDateFromToken(token);

        assertThat(expiration).isAfter(new Date());
        assertThat(expiration).isBefore(new Date(System.currentTimeMillis() + 3_600_000L + 5_000L));
    }

    @Test
    @DisplayName("validateToken rejects a token issued for a different user")
    void validateTokenRejectsOtherUser() {
        String token = tokenProvider.generateTokenFromUsername("someone-else");

        assertThat(tokenProvider.validateToken(token, userDetails)).isFalse();
    }

    @Test
    @DisplayName("an expired token is rejected while parsing")
    void expiredTokenIsRejected() {
        JwtTokenProvider expiringProvider = providerWith(SECRET, -1_000);
        String token = expiringProvider.generateTokenFromUsername("shopper");

        assertThatThrownBy(() -> expiringProvider.validateToken(token, userDetails))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    @DisplayName("a token signed with another secret fails signature verification")
    void tokenSignedWithAnotherSecretIsRejected() {
        String foreignToken = providerWith(
                "another-secret-key-that-is-also-long-enough-for-hs512-signing-64!!!!", 3_600_000)
                .generateTokenFromUsername("shopper");

        assertThatThrownBy(() -> tokenProvider.getUsernameFromToken(foreignToken))
                .isInstanceOf(SignatureException.class);
    }
}
