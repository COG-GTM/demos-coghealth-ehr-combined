package com.medchart.ehr.config;

import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
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
            "test-secret-key-that-is-long-enough-for-hs512-signing-algorithm-0123456789";

    private JwtTokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(tokenProvider, "jwtSecret", SECRET);
        ReflectionTestUtils.setField(tokenProvider, "jwtExpirationInMs", 3600000);
    }

    private UserDetails userDetails(String username) {
        return new User(username, "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_PROVIDER")));
    }

    private Authentication authentication(String username) {
        UserDetails principal = userDetails(username);
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }

    @Test
    void generateToken_embedsUsernameAsSubject() {
        String token = tokenProvider.generateToken(authentication("dr.smith"));

        assertThat(token).isNotBlank();
        assertThat(tokenProvider.getUsernameFromToken(token)).isEqualTo("dr.smith");
    }

    @Test
    void generateTokenFromUsername_embedsUsernameAsSubject() {
        String token = tokenProvider.generateTokenFromUsername("nurse.jones");

        assertThat(tokenProvider.getUsernameFromToken(token)).isEqualTo("nurse.jones");
    }

    @Test
    void getExpirationDateFromToken_isInTheFuture() {
        String token = tokenProvider.generateTokenFromUsername("dr.smith");

        assertThat(tokenProvider.getExpirationDateFromToken(token)).isAfter(new Date());
    }

    @Test
    void validateToken_returnsTrueForMatchingUserAndUnexpiredToken() {
        String token = tokenProvider.generateTokenFromUsername("dr.smith");

        assertThat(tokenProvider.validateToken(token, userDetails("dr.smith"))).isTrue();
    }

    @Test
    void validateToken_returnsFalseWhenUsernameDoesNotMatch() {
        String token = tokenProvider.generateTokenFromUsername("dr.smith");

        assertThat(tokenProvider.validateToken(token, userDetails("someone.else"))).isFalse();
    }

    @Test
    void expiredToken_throwsExpiredJwtExceptionWhenParsed() {
        ReflectionTestUtils.setField(tokenProvider, "jwtExpirationInMs", -1000);
        String expiredToken = tokenProvider.generateTokenFromUsername("dr.smith");

        assertThatThrownBy(() -> tokenProvider.getUsernameFromToken(expiredToken))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void tokenSignedWithDifferentSecret_failsValidation() {
        String token = tokenProvider.generateTokenFromUsername("dr.smith");

        JwtTokenProvider otherProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(otherProvider, "jwtSecret",
                "a-completely-different-secret-key-that-is-also-long-enough-for-hs512-99");
        ReflectionTestUtils.setField(otherProvider, "jwtExpirationInMs", 3600000);

        assertThatThrownBy(() -> otherProvider.getUsernameFromToken(token))
                .isInstanceOf(io.jsonwebtoken.JwtException.class);
    }
}
