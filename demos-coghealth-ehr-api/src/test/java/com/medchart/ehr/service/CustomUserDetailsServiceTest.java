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

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void loadUserByUsernameReturnsUserWithPrefixedRoles() {
        User user = User.builder()
                .id(1L)
                .username("sanderson")
                .password("hashed")
                .email("sanderson@example.org")
                .firstName("Sarah")
                .lastName("Anderson")
                .roles(Collections.singleton(User.Role.PROVIDER))
                .enabled(true)
                .build();
        when(userRepository.findByUsername("sanderson")).thenReturn(Optional.of(user));

        UserDetails details = customUserDetailsService.loadUserByUsername("sanderson");

        assertThat(details.getUsername()).isEqualTo("sanderson");
        assertThat(details.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_PROVIDER");
    }

    @Test
    void loadUserByUsernameThrowsWhenUserIsUnknown() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("ghost"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("ghost");
    }
}
