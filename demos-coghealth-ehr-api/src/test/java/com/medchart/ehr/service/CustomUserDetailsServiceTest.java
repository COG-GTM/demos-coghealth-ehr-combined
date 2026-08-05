package com.medchart.ehr.service;

import com.medchart.ehr.domain.auth.User;
import com.medchart.ehr.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;

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
    @DisplayName("loadUserByUsername returns the user with ROLE_ prefixed authorities")
    void loadUserByUsernameReturnsUser() {
        User user = User.builder()
                .id(1L)
                .username("shopper")
                .password("hashed")
                .email("shopper@example.org")
                .firstName("Grace")
                .lastName("Hopper")
                .roles(Set.of(User.Role.PROVIDER))
                .build();
        when(userRepository.findByUsername("shopper")).thenReturn(Optional.of(user));

        UserDetails details = customUserDetailsService.loadUserByUsername("shopper");

        assertThat(details).isSameAs(user);
        assertThat(details.getAuthorities()).extracting("authority").containsExactly("ROLE_PROVIDER");
    }

    @Test
    @DisplayName("loadUserByUsername throws UsernameNotFoundException for an unknown user")
    void loadUserByUsernameThrowsWhenMissing() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("ghost"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("ghost");
    }
}
