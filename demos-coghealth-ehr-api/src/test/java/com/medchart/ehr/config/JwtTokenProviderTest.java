package com.medchart.ehr.config;

import io.jsonwebtoken.Claims;
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

    private static final String SECRET =
            "test-secret-key-for-unit-tests-that-is-long-enough-for-hs512-signing-algorithm";
    private static final int EXPIRATION_MS = 60_000;

    private JwtTokenProvider tokenProvider;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(tokenProvider, "jwtSecret", SECRET);
        ReflectionTestUtils.setField(tokenProvider, "jwtExpirationInMs", EXPIRATION_MS);

        userDetails = new User("dr.smith", "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_PHYSICIAN")));
    }

    @Test
    void generateToken_encodesAuthenticatedUsernameAndRoles() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        String token = tokenProvider.generateToken(authentication);

        assertThat(tokenProvider.getUsernameFromToken(token)).isEqualTo("dr.smith");
        assertThat(tokenProvider.getClaimFromToken(token, claims -> claims.get("roles")).toString())
                .contains("ROLE_PHYSICIAN");
    }

    @Test
    void generateTokenFromUsername_roundTripsSubject() {
        String token = tokenProvider.generateTokenFromUsername("dr.smith");

        assertThat(tokenProvider.getUsernameFromToken(token)).isEqualTo("dr.smith");
    }

    @Test
    void getExpirationDateFromToken_reflectsConfiguredExpiration() {
        String token = tokenProvider.generateTokenFromUsername("dr.smith");

        Date expiration = tokenProvider.getExpirationDateFromToken(token);

        assertThat(expiration).isAfter(new Date());
        assertThat(expiration).isBefore(new Date(System.currentTimeMillis() + EXPIRATION_MS + 5_000));
    }

    @Test
    void getClaimFromToken_exposesIssuedAt() {
        String token = tokenProvider.generateTokenFromUsername("dr.smith");

        assertThat(tokenProvider.getClaimFromToken(token, Claims::getIssuedAt)).isNotNull();
    }

    @Test
    void validateToken_trueForMatchingUnexpiredToken() {
        String token = tokenProvider.generateTokenFromUsername("dr.smith");

        assertThat(tokenProvider.validateToken(token, userDetails)).isTrue();
    }

    @Test
    void validateToken_falseWhenUsernameDoesNotMatch() {
        String token = tokenProvider.generateTokenFromUsername("someone.else");

        assertThat(tokenProvider.validateToken(token, userDetails)).isFalse();
    }

    @Test
    void expiredToken_isRejectedWhenParsed() {
        ReflectionTestUtils.setField(tokenProvider, "jwtExpirationInMs", -1_000);
        String token = tokenProvider.generateTokenFromUsername("dr.smith");

        assertThatThrownBy(() -> tokenProvider.validateToken(token, userDetails))
                .isInstanceOf(io.jsonwebtoken.ExpiredJwtException.class);
    }

    @Test
    void tokenSignedWithAnotherSecret_isRejected() {
        JwtTokenProvider otherProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(otherProvider, "jwtSecret",
                "a-completely-different-secret-key-that-is-also-long-enough-for-hs512-use");
        ReflectionTestUtils.setField(otherProvider, "jwtExpirationInMs", EXPIRATION_MS);
        String foreignToken = otherProvider.generateTokenFromUsername("dr.smith");

        assertThatThrownBy(() -> tokenProvider.getUsernameFromToken(foreignToken))
                .isInstanceOf(SignatureException.class);
    }
}
