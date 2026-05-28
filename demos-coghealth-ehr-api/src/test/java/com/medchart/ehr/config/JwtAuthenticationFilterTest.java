package com.medchart.ehr.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_setsAuthenticationForValidToken() throws ServletException, IOException {
        UserDetails userDetails = createUserDetails("dr.smith");
        request.addHeader("Authorization", "Bearer valid-token");

        when(tokenProvider.getUsernameFromToken("valid-token")).thenReturn("dr.smith");
        when(tokenProvider.validateToken("valid-token", userDetails)).thenReturn(true);
        when(userDetailsService.loadUserByUsername("dr.smith")).thenReturn(userDetails);

        filter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("dr.smith",
                SecurityContextHolder.getContext().getAuthentication().getName());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_skipsAuthenticationWhenNoAuthorizationHeader() throws ServletException, IOException {
        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_skipsAuthenticationForNonBearerToken() throws ServletException, IOException {
        request.addHeader("Authorization", "Basic dXNlcjpwYXNz");

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_skipsAuthenticationForInvalidToken() throws ServletException, IOException {
        UserDetails userDetails = createUserDetails("dr.smith");
        request.addHeader("Authorization", "Bearer invalid-token");

        when(tokenProvider.getUsernameFromToken("invalid-token")).thenReturn("dr.smith");
        when(tokenProvider.validateToken("invalid-token", userDetails)).thenReturn(false);
        when(userDetailsService.loadUserByUsername("dr.smith")).thenReturn(userDetails);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_continuesFilterChainOnException() throws ServletException, IOException {
        request.addHeader("Authorization", "Bearer bad-token");

        when(tokenProvider.getUsernameFromToken("bad-token"))
                .thenThrow(new RuntimeException("Token parse failure"));

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_setsCorrectAuthoritiesFromUserDetails() throws ServletException, IOException {
        UserDetails userDetails = createUserDetails("admin");
        request.addHeader("Authorization", "Bearer admin-token");

        when(tokenProvider.getUsernameFromToken("admin-token")).thenReturn("admin");
        when(tokenProvider.validateToken("admin-token", userDetails)).thenReturn(true);
        when(userDetailsService.loadUserByUsername("admin")).thenReturn(userDetails);

        filter.doFilterInternal(request, response, filterChain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertTrue(auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_PROVIDER")));
    }

    private UserDetails createUserDetails(String username) {
        return new User(username, "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_PROVIDER")));
    }
}
