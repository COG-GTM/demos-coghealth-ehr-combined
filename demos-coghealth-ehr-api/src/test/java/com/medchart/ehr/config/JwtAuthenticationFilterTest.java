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

import java.util.List;

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
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_withValidToken_setsAuthentication() throws Exception {
        UserDetails userDetails = new User("dr.smith", "password",
                List.of(new SimpleGrantedAuthority("ROLE_PROVIDER")));

        request.addHeader("Authorization", "Bearer valid-jwt-token");

        when(tokenProvider.getUsernameFromToken("valid-jwt-token")).thenReturn("dr.smith");
        when(tokenProvider.validateToken(eq("valid-jwt-token"), any(UserDetails.class))).thenReturn(true);
        when(userDetailsService.loadUserByUsername("dr.smith")).thenReturn(userDetails);

        filter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("dr.smith", SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @Test
    void doFilterInternal_withNoToken_doesNotSetAuthentication() throws Exception {
        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_withInvalidBearerFormat_doesNotSetAuthentication() throws Exception {
        request.addHeader("Authorization", "Basic credentials");

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(tokenProvider);
    }

    @Test
    void doFilterInternal_withInvalidToken_doesNotSetAuthentication() throws Exception {
        UserDetails userDetails = new User("dr.smith", "password",
                List.of(new SimpleGrantedAuthority("ROLE_PROVIDER")));

        request.addHeader("Authorization", "Bearer invalid-token");

        when(tokenProvider.getUsernameFromToken("invalid-token")).thenReturn("dr.smith");
        when(tokenProvider.validateToken(eq("invalid-token"), any(UserDetails.class))).thenReturn(false);
        when(userDetailsService.loadUserByUsername("dr.smith")).thenReturn(userDetails);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_whenExceptionThrown_continuesFilterChain() throws Exception {
        request.addHeader("Authorization", "Bearer bad-token");

        when(tokenProvider.getUsernameFromToken("bad-token")).thenThrow(new RuntimeException("Parse error"));

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(200, response.getStatus());
    }

    @Test
    void doFilterInternal_alwaysCallsFilterChain() throws Exception {
        MockFilterChain spyChain = spy(new MockFilterChain());

        filter.doFilterInternal(request, response, spyChain);

        verify(spyChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_withEmptyBearerToken_doesNotSetAuthentication() throws Exception {
        request.addHeader("Authorization", "Bearer ");

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
