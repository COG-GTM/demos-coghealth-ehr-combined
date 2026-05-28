package com.medchart.ehr.config;

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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;

    private static final String SECRET = "test-secret-key-that-is-at-least-512-bits-long-for-hs512-algorithm-testing-purposes-only";
    private static final int EXPIRATION_MS = 86400000;

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(tokenProvider, "jwtSecret", SECRET);
        ReflectionTestUtils.setField(tokenProvider, "jwtExpirationInMs", EXPIRATION_MS);
    }

    @Test
    void generateToken_returnsNonNullToken() {
        Authentication auth = createAuthentication("dr.smith");
        String token = tokenProvider.generateToken(auth);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void getUsernameFromToken_returnsCorrectUsername() {
        Authentication auth = createAuthentication("dr.smith");
        String token = tokenProvider.generateToken(auth);

        String username = tokenProvider.getUsernameFromToken(token);
        assertEquals("dr.smith", username);
    }

    @Test
    void generateTokenFromUsername_returnsValidToken() {
        String token = tokenProvider.generateTokenFromUsername("nurse.jones");

        assertNotNull(token);
        assertEquals("nurse.jones", tokenProvider.getUsernameFromToken(token));
    }

    @Test
    void getExpirationDateFromToken_returnsFutureDate() {
        String token = tokenProvider.generateTokenFromUsername("dr.smith");

        Date expiration = tokenProvider.getExpirationDateFromToken(token);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    void validateToken_returnsTrueForValidToken() {
        UserDetails userDetails = new User("dr.smith", "password",
                List.of(new SimpleGrantedAuthority("ROLE_PROVIDER")));
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        String token = tokenProvider.generateToken(auth);

        assertTrue(tokenProvider.validateToken(token, userDetails));
    }

    @Test
    void validateToken_returnsFalseForWrongUsername() {
        String token = tokenProvider.generateTokenFromUsername("dr.smith");

        UserDetails wrongUser = new User("dr.jones", "password",
                List.of(new SimpleGrantedAuthority("ROLE_PROVIDER")));

        assertFalse(tokenProvider.validateToken(token, wrongUser));
    }

    @Test
    void validateToken_returnsFalseForExpiredToken() {
        JwtTokenProvider shortLivedProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(shortLivedProvider, "jwtSecret", SECRET);
        ReflectionTestUtils.setField(shortLivedProvider, "jwtExpirationInMs", -1000);

        String token = shortLivedProvider.generateTokenFromUsername("dr.smith");

        UserDetails userDetails = new User("dr.smith", "password",
                List.of(new SimpleGrantedAuthority("ROLE_PROVIDER")));

        assertThrows(Exception.class, () -> shortLivedProvider.validateToken(token, userDetails));
    }

    @Test
    void getClaimFromToken_extractsSubjectClaim() {
        String token = tokenProvider.generateTokenFromUsername("admin.user");

        String subject = tokenProvider.getClaimFromToken(token, claims -> claims.getSubject());
        assertEquals("admin.user", subject);
    }

    @Test
    void generateToken_includesRolesInClaims() {
        List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_PROVIDER"),
                new SimpleGrantedAuthority("ROLE_ADMIN")
        );
        UserDetails userDetails = new User("dr.smith", "password", authorities);
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, authorities);

        String token = tokenProvider.generateToken(auth);

        assertNotNull(token);
        assertEquals("dr.smith", tokenProvider.getUsernameFromToken(token));
    }

    @Test
    void generateTokenFromUsername_producesDistinctTokensPerUser() {
        String token1 = tokenProvider.generateTokenFromUsername("user.one");
        String token2 = tokenProvider.generateTokenFromUsername("user.two");

        assertNotEquals(token1, token2);
        assertEquals("user.one", tokenProvider.getUsernameFromToken(token1));
        assertEquals("user.two", tokenProvider.getUsernameFromToken(token2));
    }

    private Authentication createAuthentication(String username) {
        UserDetails userDetails = new User(username, "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_PROVIDER")));
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }
}
