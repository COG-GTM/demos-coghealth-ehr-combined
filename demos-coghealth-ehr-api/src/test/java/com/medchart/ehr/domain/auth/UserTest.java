package com.medchart.ehr.domain.auth;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    private User.UserBuilder baseUser() {
        return User.builder()
                .id(1L)
                .username("dr.smith")
                .password("encoded")
                .email("dr.smith@coghealth.test")
                .firstName("Sam")
                .lastName("Smith")
                .roles(Set.of(User.Role.PROVIDER))
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true);
    }

    @Test
    void getAuthorities_prefixesEachRoleWithRolePrefix() {
        User user = baseUser()
                .roles(Set.of(User.Role.PROVIDER, User.Role.ADMIN))
                .build();

        assertThat(user.getAuthorities())
                .extracting("authority")
                .containsExactlyInAnyOrder("ROLE_PROVIDER", "ROLE_ADMIN");
    }

    @Test
    void userDetailsFlags_reflectBackingFields() {
        User user = baseUser()
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
    void userDetailsFlags_areTrueForActiveAccount() {
        User user = baseUser().build();

        assertThat(user.isEnabled()).isTrue();
        assertThat(user.isAccountNonExpired()).isTrue();
        assertThat(user.isAccountNonLocked()).isTrue();
        assertThat(user.isCredentialsNonExpired()).isTrue();
    }
}
