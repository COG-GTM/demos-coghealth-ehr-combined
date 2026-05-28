package com.medchart.ehr.domain.auth;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void getAuthorities_withSingleRole_returnsOneAuthority() {
        User user = User.builder()
                .username("dr.smith")
                .roles(Set.of(User.Role.PROVIDER))
                .build();

        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

        assertEquals(1, authorities.size());
        assertTrue(authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_PROVIDER")));
    }

    @Test
    void getAuthorities_withMultipleRoles_returnsAllAuthorities() {
        User user = User.builder()
                .username("admin")
                .roles(Set.of(User.Role.ADMIN, User.Role.PROVIDER, User.Role.STAFF))
                .build();

        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

        assertEquals(3, authorities.size());
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_PROVIDER")));
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_STAFF")));
    }

    @Test
    void isEnabled_defaultsToTrue() {
        User user = User.builder()
                .username("dr.smith")
                .enabled(true)
                .build();

        assertTrue(user.isEnabled());
    }

    @Test
    void isEnabled_returnsFalseWhenDisabled() {
        User user = User.builder()
                .username("dr.smith")
                .enabled(false)
                .build();

        assertFalse(user.isEnabled());
    }

    @Test
    void isAccountNonExpired_reflectsFieldValue() {
        User expired = User.builder().accountNonExpired(false).build();
        User active = User.builder().accountNonExpired(true).build();

        assertFalse(expired.isAccountNonExpired());
        assertTrue(active.isAccountNonExpired());
    }

    @Test
    void isAccountNonLocked_reflectsFieldValue() {
        User locked = User.builder().accountNonLocked(false).build();
        User unlocked = User.builder().accountNonLocked(true).build();

        assertFalse(locked.isAccountNonLocked());
        assertTrue(unlocked.isAccountNonLocked());
    }

    @Test
    void isCredentialsNonExpired_reflectsFieldValue() {
        User expired = User.builder().credentialsNonExpired(false).build();
        User valid = User.builder().credentialsNonExpired(true).build();

        assertFalse(expired.isCredentialsNonExpired());
        assertTrue(valid.isCredentialsNonExpired());
    }

    @Test
    void builder_setsAllFields() {
        User user = User.builder()
                .id(1L)
                .username("dr.smith")
                .password("encoded")
                .email("dr.smith@medchart.com")
                .firstName("John")
                .lastName("Smith")
                .roles(Set.of(User.Role.PROVIDER))
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();

        assertEquals(1L, user.getId());
        assertEquals("dr.smith", user.getUsername());
        assertEquals("encoded", user.getPassword());
        assertEquals("dr.smith@medchart.com", user.getEmail());
        assertEquals("John", user.getFirstName());
        assertEquals("Smith", user.getLastName());
        assertEquals(Set.of(User.Role.PROVIDER), user.getRoles());
    }

    @Test
    void roleEnum_containsExpectedValues() {
        User.Role[] roles = User.Role.values();
        assertEquals(3, roles.length);
        assertNotNull(User.Role.valueOf("PROVIDER"));
        assertNotNull(User.Role.valueOf("ADMIN"));
        assertNotNull(User.Role.valueOf("STAFF"));
    }
}
