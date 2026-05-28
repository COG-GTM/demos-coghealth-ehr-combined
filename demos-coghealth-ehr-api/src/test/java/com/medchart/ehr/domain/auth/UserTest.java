package com.medchart.ehr.domain.auth;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void getAuthorities_mapsRolesToGrantedAuthorities() {
        User user = User.builder()
                .username("dr.smith")
                .password("encoded")
                .email("smith@medchart.com")
                .firstName("John")
                .lastName("Smith")
                .roles(Set.of(User.Role.PROVIDER))
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();

        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

        assertEquals(1, authorities.size());
        assertTrue(authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_PROVIDER")));
    }

    @Test
    void getAuthorities_mapsMultipleRoles() {
        User user = User.builder()
                .username("admin")
                .password("encoded")
                .email("admin@medchart.com")
                .firstName("Admin")
                .lastName("User")
                .roles(Set.of(User.Role.PROVIDER, User.Role.ADMIN))
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();

        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

        assertEquals(2, authorities.size());
        assertTrue(authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_PROVIDER")));
        assertTrue(authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void userDetailsInterface_returnsCorrectFlags() {
        User user = User.builder()
                .username("testuser")
                .password("encoded")
                .email("test@medchart.com")
                .firstName("Test")
                .lastName("User")
                .roles(Set.of(User.Role.STAFF))
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();

        assertTrue(user.isEnabled());
        assertTrue(user.isAccountNonExpired());
        assertTrue(user.isAccountNonLocked());
        assertTrue(user.isCredentialsNonExpired());
    }

    @Test
    void userDetailsInterface_disabledAccount() {
        User user = User.builder()
                .username("locked")
                .password("encoded")
                .email("locked@medchart.com")
                .firstName("Locked")
                .lastName("User")
                .roles(Set.of(User.Role.PROVIDER))
                .enabled(false)
                .accountNonExpired(true)
                .accountNonLocked(false)
                .credentialsNonExpired(true)
                .build();

        assertFalse(user.isEnabled());
        assertFalse(user.isAccountNonLocked());
    }

    @Test
    void roleEnum_containsExpectedValues() {
        User.Role[] roles = User.Role.values();
        assertEquals(3, roles.length);
        assertNotNull(User.Role.valueOf("PROVIDER"));
        assertNotNull(User.Role.valueOf("ADMIN"));
        assertNotNull(User.Role.valueOf("STAFF"));
    }

    @Test
    void builder_setsAllFields() {
        User user = User.builder()
                .id(1L)
                .username("dr.smith")
                .password("encoded")
                .email("smith@medchart.com")
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
        assertEquals("smith@medchart.com", user.getEmail());
        assertEquals("John", user.getFirstName());
        assertEquals("Smith", user.getLastName());
    }
}
