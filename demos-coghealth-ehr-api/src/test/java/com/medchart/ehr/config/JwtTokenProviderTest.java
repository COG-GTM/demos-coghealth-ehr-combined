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
            "test-jwt-secret-key-that-is-long-enough-for-hs512-algorithm-validation-requirements-64-bytes";
    private static final int JWT_EXPIRATION_MS = 86400000; // 24 hours

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(tokenProvider, "jwtSecret", JWT_SECRET);
        ReflectionTestUtils.setField(tokenProvider, "jwtExpirationInMs", JWT_EXPIRATION_MS);
    }

    @Test
    void generateToken_returnsNonNullToken() {
        Authentication auth = createAuthentication("testuser");

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
    void getExpirationDateFromToken_returnsDateInFuture() {
        Authentication auth = createAuthentication("testuser");

        String token = tokenProvider.generateToken(auth);
        Date expiration = tokenProvider.getExpirationDateFromToken(token);

        assertTrue(expiration.after(new Date()));
    }

    @Test
    void validateToken_returnsTrueForValidToken() {
        UserDetails userDetails = createUserDetails("testuser");
        Authentication auth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        String token = tokenProvider.generateToken(auth);

        assertTrue(tokenProvider.validateToken(token, userDetails));
    }

    @Test
    void validateToken_returnsFalseForWrongUser() {
        UserDetails tokenOwner = createUserDetails("user-a");
        UserDetails differentUser = createUserDetails("user-b");
        Authentication auth = new UsernamePasswordAuthenticationToken(
                tokenOwner, null, tokenOwner.getAuthorities());

        String token = tokenProvider.generateToken(auth);

        assertFalse(tokenProvider.validateToken(token, differentUser));
    }

    @Test
    void validateToken_returnsFalseForExpiredToken() {
        JwtTokenProvider shortLivedProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(shortLivedProvider, "jwtSecret", JWT_SECRET);
        ReflectionTestUtils.setField(shortLivedProvider, "jwtExpirationInMs", -1000); // already expired

        UserDetails userDetails = createUserDetails("testuser");
        Authentication auth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        String token = shortLivedProvider.generateToken(auth);

        assertThrows(ExpiredJwtException.class,
                () -> shortLivedProvider.validateToken(token, userDetails));
    }

    @Test
    void generateTokenFromUsername_setsCorrectSubject() {
        String token = tokenProvider.generateTokenFromUsername("admin");

        assertEquals("admin", tokenProvider.getUsernameFromToken(token));
    }

    @Test
    void getUsernameFromToken_throwsForMalformedToken() {
        assertThrows(MalformedJwtException.class,
                () -> tokenProvider.getUsernameFromToken("not-a-jwt-token"));
    }

    @Test
    void generateToken_includesRolesInClaims() {
        List<SimpleGrantedAuthority> authorities =
                List.of(new SimpleGrantedAuthority("ROLE_PROVIDER"));
        UserDetails userDetails = new User("testuser", "password", authorities);
        Authentication auth = new UsernamePasswordAuthenticationToken(
                userDetails, null, authorities);

        String token = tokenProvider.generateToken(auth);

        assertNotNull(token);
        assertEquals("testuser", tokenProvider.getUsernameFromToken(token));
    }

    @Test
    void generateTokenFromUsername_producesValidatableToken() {
        UserDetails userDetails = createUserDetails("testuser");

        String token = tokenProvider.generateTokenFromUsername("testuser");

        assertTrue(tokenProvider.validateToken(token, userDetails));
    }

    private Authentication createAuthentication(String username) {
        UserDetails userDetails = createUserDetails(username);
        return new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
    }

    private UserDetails createUserDetails(String username) {
        return new User(username, "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_PROVIDER")));
    }
}
