package com.medchart.ehr.service;

import com.medchart.ehr.domain.auth.User;
import com.medchart.ehr.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    @Test
    void loadUserByUsername_returnsUserWhenFound() {
        User user = User.builder()
                .id(1L)
                .username("dr.smith")
                .password("encoded-password")
                .email("dr.smith@medchart.com")
                .firstName("John")
                .lastName("Smith")
                .roles(Set.of(User.Role.PROVIDER))
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();

        when(userRepository.findByUsername("dr.smith")).thenReturn(Optional.of(user));

        UserDetails result = userDetailsService.loadUserByUsername("dr.smith");

        assertNotNull(result);
        assertEquals("dr.smith", result.getUsername());
        assertEquals("encoded-password", result.getPassword());
    }

    @Test
    void loadUserByUsername_throwsWhenUserNotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        UsernameNotFoundException ex = assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("unknown"));

        assertTrue(ex.getMessage().contains("unknown"));
    }

    @Test
    void loadUserByUsername_returnsCorrectAuthorities() {
        User user = User.builder()
                .id(2L)
                .username("admin")
                .password("encoded")
                .email("admin@medchart.com")
                .firstName("Admin")
                .lastName("User")
                .roles(Set.of(User.Role.ADMIN, User.Role.PROVIDER))
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

        UserDetails result = userDetailsService.loadUserByUsername("admin");

        assertEquals(2, result.getAuthorities().size());
        assertTrue(result.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        assertTrue(result.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_PROVIDER")));
    }

    @Test
    void loadUserByUsername_queriesRepositoryWithExactUsername() {
        when(userRepository.findByUsername("dr.smith")).thenReturn(Optional.of(
                User.builder()
                        .username("dr.smith")
                        .password("p")
                        .email("e@e.com")
                        .firstName("F")
                        .lastName("L")
                        .roles(Set.of(User.Role.PROVIDER))
                        .enabled(true)
                        .accountNonExpired(true)
                        .accountNonLocked(true)
                        .credentialsNonExpired(true)
                        .build()
        ));

        userDetailsService.loadUserByUsername("dr.smith");

        verify(userRepository).findByUsername("dr.smith");
        verify(userRepository, times(1)).findByUsername(anyString());
    }
}
