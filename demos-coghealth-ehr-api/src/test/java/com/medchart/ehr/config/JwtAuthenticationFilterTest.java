package com.medchart.ehr.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private UserDetailsService userDetailsService;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain filterChain;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();
    }

    private UserDetails createUserDetails(String username) {
        return new User(username, "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_PROVIDER")));
    }

    @Test
    void doFilter_withValidBearerToken_setsAuthentication() throws Exception {
        String token = "valid.jwt.token";
        String username = "drsmith";
        UserDetails userDetails = createUserDetails(username);

        request.addHeader("Authorization", "Bearer " + token);

        when(tokenProvider.getUsernameFromToken(token)).thenReturn(username);
        when(tokenProvider.validateToken(eq(token), any(UserDetails.class))).thenReturn(true);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);

        filter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(username,
                ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername());
    }

    @Test
    void doFilter_withNoAuthorizationHeader_doesNotSetAuthentication() throws Exception {
        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilter_withNonBearerHeader_doesNotSetAuthentication() throws Exception {
        request.addHeader("Authorization", "Basic dXNlcjpwYXNz");

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(tokenProvider);
    }

    @Test
    void doFilter_withInvalidToken_doesNotSetAuthentication() throws Exception {
        String token = "invalid.jwt.token";
        String username = "drsmith";
        UserDetails userDetails = createUserDetails(username);

        request.addHeader("Authorization", "Bearer " + token);

        when(tokenProvider.getUsernameFromToken(token)).thenReturn(username);
        when(tokenProvider.validateToken(eq(token), any(UserDetails.class))).thenReturn(false);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilter_alwaysContinuesFilterChain() throws Exception {
        request.addHeader("Authorization", "Bearer bad.token");
        when(tokenProvider.getUsernameFromToken("bad.token")).thenThrow(new RuntimeException("bad"));

        filter.doFilterInternal(request, response, filterChain);

        assertNotNull(filterChain.getRequest());
    }

    @Test
    void doFilter_withExceptionDuringValidation_doesNotSetAuthentication() throws Exception {
        request.addHeader("Authorization", "Bearer some.token");
        when(tokenProvider.getUsernameFromToken("some.token")).thenThrow(new RuntimeException("parse error"));

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilter_withEmptyBearerToken_doesNotSetAuthentication() throws Exception {
        request.addHeader("Authorization", "Bearer ");

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(tokenProvider);
    }
}
