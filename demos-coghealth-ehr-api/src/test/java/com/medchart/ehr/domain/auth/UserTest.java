package com.medchart.ehr.domain.auth;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    private User.UserBuilder baseUser() {
        return User.builder()
                .username("dr.house")
                .password("hashed")
                .email("house@example.com")
                .firstName("Gregory")
                .lastName("House")
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true);
    }

    @Test
    void getAuthorities_prefixesEachRoleWithRole() {
        User user = baseUser()
                .roles(Set.of(User.Role.PROVIDER, User.Role.ADMIN))
                .build();

        Set<String> authorities = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        assertThat(authorities).containsExactlyInAnyOrder("ROLE_PROVIDER", "ROLE_ADMIN");
    }

    @Test
    void getAuthorities_isEmptyWhenUserHasNoRoles() {
        User user = baseUser().roles(Set.of()).build();

        assertThat(user.getAuthorities()).isEmpty();
    }

    @Test
    void userDetailsFlags_reflectBackingFields() {
        User user = baseUser()
                .roles(Set.of(User.Role.STAFF))
                .enabled(false)
                .accountNonExpired(false)
                .accountNonLocked(false)
                .credentialsNonExpired(false)
                .build();

        assertThat(user.isEnabled()).isFalse();
        assertThat(user.isAccountNonExpired()).isFalse();
        assertThat(user.isAccountNonLocked()).isFalse();
        assertThat(user.isCredentialsNonExpired()).isFalse();
    }

    @Test
    void userDetailsAccessors_returnUsernameAndPassword() {
        User user = baseUser().roles(Set.of(User.Role.PROVIDER)).build();

        assertThat(user.getUsername()).isEqualTo("dr.house");
        assertThat(user.getPassword()).isEqualTo("hashed");
    }
}
