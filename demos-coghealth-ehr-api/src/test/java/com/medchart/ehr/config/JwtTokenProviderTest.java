package com.medchart.ehr.config;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
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

    private static final String SECRET = "test-secret-key-that-is-long-enough-for-hs512-algorithm-testing-purposes-only";
    private static final int EXPIRATION_MS = 86400000; // 24 hours

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
    void generateToken_embedsCorrectUsername() {
        Authentication auth = createAuthentication("dr.smith");
        String token = tokenProvider.generateToken(auth);
        assertEquals("dr.smith", tokenProvider.getUsernameFromToken(token));
    }

    @Test
    void generateTokenFromUsername_embedsCorrectUsername() {
        String token = tokenProvider.generateTokenFromUsername("nurse.jones");
        assertEquals("nurse.jones", tokenProvider.getUsernameFromToken(token));
    }

    @Test
    void generateTokenFromUsername_setsExpirationInFuture() {
        String token = tokenProvider.generateTokenFromUsername("admin");
        Date expiration = tokenProvider.getExpirationDateFromToken(token);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    void validateToken_returnsTrueForValidToken() {
        UserDetails userDetails = buildUserDetails("dr.smith");
        Authentication auth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        String token = tokenProvider.generateToken(auth);
        assertTrue(tokenProvider.validateToken(token, userDetails));
    }

    @Test
    void validateToken_returnsFalseWhenUsernameMismatch() {
        String token = tokenProvider.generateTokenFromUsername("dr.smith");
        UserDetails differentUser = buildUserDetails("dr.jones");
        assertFalse(tokenProvider.validateToken(token, differentUser));
    }

    @Test
    void validateToken_throwsForExpiredToken() {
        ReflectionTestUtils.setField(tokenProvider, "jwtExpirationInMs", -1000);
        String token = tokenProvider.generateTokenFromUsername("dr.smith");

        UserDetails userDetails = buildUserDetails("dr.smith");
        assertThrows(ExpiredJwtException.class, () -> tokenProvider.validateToken(token, userDetails));
    }

    @Test
    void getUsernameFromToken_throwsForMalformedToken() {
        assertThrows(MalformedJwtException.class,
                () -> tokenProvider.getUsernameFromToken("not-a-jwt"));
    }

    @Test
    void getUsernameFromToken_throwsForTamperedToken() {
        String token = tokenProvider.generateTokenFromUsername("dr.smith");
        String tampered = token.substring(0, token.length() - 5) + "XXXXX";
        assertThrows(SignatureException.class,
                () -> tokenProvider.getUsernameFromToken(tampered));
    }

    @Test
    void getClaimFromToken_extractsIssuedAt() {
        long beforeSeconds = System.currentTimeMillis() / 1000;
        String token = tokenProvider.generateTokenFromUsername("dr.smith");
        long afterSeconds = System.currentTimeMillis() / 1000;

        Date issuedAt = tokenProvider.getClaimFromToken(token, claims -> claims.getIssuedAt());
        long issuedAtSeconds = issuedAt.getTime() / 1000;
        assertTrue(issuedAtSeconds >= beforeSeconds && issuedAtSeconds <= afterSeconds);
    }

    @Test
    void generateToken_includesRolesInClaims() {
        List<SimpleGrantedAuthority> authorities =
                List.of(new SimpleGrantedAuthority("ROLE_PROVIDER"));
        UserDetails userDetails = new User("dr.smith", "password", authorities);
        Authentication auth = new UsernamePasswordAuthenticationToken(
                userDetails, null, authorities);

        String token = tokenProvider.generateToken(auth);
        assertNotNull(tokenProvider.getClaimFromToken(token, claims -> claims.get("roles")));
    }

    @Test
    void generateTokenFromUsername_producesUniqueTokensPerUser() {
        String token1 = tokenProvider.generateTokenFromUsername("dr.smith");
        String token2 = tokenProvider.generateTokenFromUsername("dr.jones");
        assertNotEquals(token1, token2);
    }

    private Authentication createAuthentication(String username) {
        UserDetails userDetails = buildUserDetails(username);
        return new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
    }

    private UserDetails buildUserDetails(String username) {
        return new User(username, "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_PROVIDER")));
    }
}
