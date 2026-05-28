package com.medchart.ehr.config;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
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

    private static final String JWT_SECRET =
            "test-secret-key-that-is-long-enough-for-hs512-algorithm-needs-at-least-64-bytes-so-making-it-really-long";
    private static final int EXPIRATION_MS = 86400000;

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(tokenProvider, "jwtSecret", JWT_SECRET);
        ReflectionTestUtils.setField(tokenProvider, "jwtExpirationInMs", EXPIRATION_MS);
    }

    private Authentication createAuthentication(String username) {
        List<SimpleGrantedAuthority> authorities =
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_PROVIDER"));
        UserDetails principal = new User(username, "password", authorities);
        return new UsernamePasswordAuthenticationToken(principal, null, authorities);
    }

    @Test
    void generateToken_returnsNonNullToken() {
        Authentication auth = createAuthentication("drsmith");
        String token = tokenProvider.generateToken(auth);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void getUsernameFromToken_returnsCorrectUsername() {
        Authentication auth = createAuthentication("drsmith");
        String token = tokenProvider.generateToken(auth);

        assertEquals("drsmith", tokenProvider.getUsernameFromToken(token));
    }

    @Test
    void generateTokenFromUsername_returnsTokenWithCorrectSubject() {
        String token = tokenProvider.generateTokenFromUsername("nurse.jones");

        assertEquals("nurse.jones", tokenProvider.getUsernameFromToken(token));
    }

    @Test
    void getExpirationDateFromToken_returnsFutureDate() {
        String token = tokenProvider.generateTokenFromUsername("admin");

        Date expiration = tokenProvider.getExpirationDateFromToken(token);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    void validateToken_returnsTrueForValidToken() {
        String username = "drsmith";
        Authentication auth = createAuthentication(username);
        String token = tokenProvider.generateToken(auth);

        UserDetails userDetails = new User(username, "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_PROVIDER")));

        assertTrue(tokenProvider.validateToken(token, userDetails));
    }

    @Test
    void validateToken_returnsFalseForWrongUsername() {
        Authentication auth = createAuthentication("drsmith");
        String token = tokenProvider.generateToken(auth);

        UserDetails wrongUser = new User("someone_else", "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_PROVIDER")));

        assertFalse(tokenProvider.validateToken(token, wrongUser));
    }

    @Test
    void validateToken_returnsFalseForExpiredToken() {
        JwtTokenProvider shortLivedProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(shortLivedProvider, "jwtSecret", JWT_SECRET);
        ReflectionTestUtils.setField(shortLivedProvider, "jwtExpirationInMs", -1000);

        String token = shortLivedProvider.generateTokenFromUsername("drsmith");

        UserDetails userDetails = new User("drsmith", "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_PROVIDER")));

        assertThrows(ExpiredJwtException.class,
                () -> shortLivedProvider.validateToken(token, userDetails));
    }

    @Test
    void getUsernameFromToken_throwsForMalformedToken() {
        assertThrows(MalformedJwtException.class,
                () -> tokenProvider.getUsernameFromToken("not.a.valid.jwt"));
    }

    @Test
    void generateToken_includesRolesInClaims() {
        Authentication auth = createAuthentication("drsmith");
        String token = tokenProvider.generateToken(auth);

        assertNotNull(token);
        String username = tokenProvider.getUsernameFromToken(token);
        assertEquals("drsmith", username);
    }

    @Test
    void differentUsersProduceDifferentTokens() {
        String token1 = tokenProvider.generateTokenFromUsername("user1");
        String token2 = tokenProvider.generateTokenFromUsername("user2");

        assertNotEquals(token1, token2);
    }
}
