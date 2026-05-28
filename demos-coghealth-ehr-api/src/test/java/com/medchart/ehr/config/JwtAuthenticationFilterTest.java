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
import static org.mockito.ArgumentMatchers.anyString;
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

    @Test
    void doFilterInternal_setsAuthenticationForValidToken() throws Exception {
        UserDetails userDetails = buildUserDetails("dr.smith");

        request.addHeader("Authorization", "Bearer valid-token");
        when(tokenProvider.getUsernameFromToken("valid-token")).thenReturn("dr.smith");
        when(tokenProvider.validateToken(eq("valid-token"), any(UserDetails.class))).thenReturn(true);
        when(userDetailsService.loadUserByUsername("dr.smith")).thenReturn(userDetails);

        filter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("dr.smith",
                SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @Test
    void doFilterInternal_doesNotSetAuthenticationWhenNoToken() throws Exception {
        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_doesNotSetAuthenticationForInvalidToken() throws Exception {
        UserDetails userDetails = buildUserDetails("dr.smith");

        request.addHeader("Authorization", "Bearer invalid-token");
        when(tokenProvider.getUsernameFromToken("invalid-token")).thenReturn("dr.smith");
        when(tokenProvider.validateToken(eq("invalid-token"), any(UserDetails.class))).thenReturn(false);
        when(userDetailsService.loadUserByUsername("dr.smith")).thenReturn(userDetails);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_continuesFilterChainEvenOnException() throws Exception {
        request.addHeader("Authorization", "Bearer bad-token");
        when(userDetailsService.loadUserByUsername(anyString()))
                .thenThrow(new RuntimeException("parse error"));

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertNotNull(filterChain.getRequest());
    }

    @Test
    void doFilterInternal_ignoresNonBearerAuthorizationHeader() throws Exception {
        request.addHeader("Authorization", "Basic dXNlcjpwYXNz");

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(tokenProvider, never()).getUsernameFromToken(anyString());
    }

    @Test
    void doFilterInternal_handlesEmptyBearerToken() throws Exception {
        request.addHeader("Authorization", "Bearer ");

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_setsCorrectAuthoritiesFromUserDetails() throws Exception {
        UserDetails userDetails = new User("admin", "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));

        request.addHeader("Authorization", "Bearer valid-token");
        when(tokenProvider.getUsernameFromToken("valid-token")).thenReturn("admin");
        when(tokenProvider.validateToken(eq("valid-token"), any(UserDetails.class))).thenReturn(true);
        when(userDetailsService.loadUserByUsername("admin")).thenReturn(userDetails);

        filter.doFilterInternal(request, response, filterChain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertTrue(auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    private UserDetails buildUserDetails(String username) {
        return new User(username, "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_PROVIDER")));
    }
}
