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
    private CustomUserDetailsService service;

    private User createTestUser(String username) {
        return User.builder()
                .id(1L)
                .username(username)
                .password("encoded_password")
                .email(username + "@medchart.com")
                .firstName("Test")
                .lastName("User")
                .roles(Set.of(User.Role.PROVIDER))
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();
    }

    @Test
    void loadUserByUsername_existingUser_returnsUserDetails() {
        User user = createTestUser("drsmith");
        when(userRepository.findByUsername("drsmith")).thenReturn(Optional.of(user));

        UserDetails result = service.loadUserByUsername("drsmith");

        assertNotNull(result);
        assertEquals("drsmith", result.getUsername());
        assertEquals("encoded_password", result.getPassword());
        assertTrue(result.isEnabled());
        assertTrue(result.isAccountNonExpired());
        assertTrue(result.isAccountNonLocked());
        assertTrue(result.isCredentialsNonExpired());
    }

    @Test
    void loadUserByUsername_existingUser_hasCorrectAuthorities() {
        User user = createTestUser("drsmith");
        when(userRepository.findByUsername("drsmith")).thenReturn(Optional.of(user));

        UserDetails result = service.loadUserByUsername("drsmith");

        assertEquals(1, result.getAuthorities().size());
        assertTrue(result.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_PROVIDER")));
    }

    @Test
    void loadUserByUsername_nonExistentUser_throwsUsernameNotFoundException() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        UsernameNotFoundException ex = assertThrows(
                UsernameNotFoundException.class,
                () -> service.loadUserByUsername("unknown")
        );
        assertTrue(ex.getMessage().contains("unknown"));
    }

    @Test
    void loadUserByUsername_delegatesToRepository() {
        User user = createTestUser("nurse.jones");
        when(userRepository.findByUsername("nurse.jones")).thenReturn(Optional.of(user));

        service.loadUserByUsername("nurse.jones");

        verify(userRepository, times(1)).findByUsername("nurse.jones");
    }
}
