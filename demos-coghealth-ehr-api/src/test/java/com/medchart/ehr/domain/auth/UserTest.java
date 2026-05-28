package com.medchart.ehr.domain.auth;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private User createUser(Set<User.Role> roles) {
        return User.builder()
                .id(1L)
                .username("drsmith")
                .password("encoded_password")
                .email("drsmith@medchart.com")
                .firstName("John")
                .lastName("Smith")
                .roles(roles)
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();
    }

    @Test
    void getAuthorities_singleRole_returnsRoleWithPrefix() {
        User user = createUser(Set.of(User.Role.PROVIDER));

        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

        assertEquals(1, authorities.size());
        assertTrue(authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_PROVIDER")));
    }

    @Test
    void getAuthorities_multipleRoles_returnsAllRolesWithPrefix() {
        User user = createUser(Set.of(User.Role.PROVIDER, User.Role.ADMIN));

        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

        assertEquals(2, authorities.size());
        Set<String> authorityNames = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        assertTrue(authorityNames.contains("ROLE_PROVIDER"));
        assertTrue(authorityNames.contains("ROLE_ADMIN"));
    }

    @Test
    void getAuthorities_staffRole_returnsRoleStaff() {
        User user = createUser(Set.of(User.Role.STAFF));

        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

        assertEquals(1, authorities.size());
        assertTrue(authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_STAFF")));
    }

    @Test
    void isEnabled_whenTrue_returnsTrue() {
        User user = createUser(Set.of(User.Role.PROVIDER));
        assertTrue(user.isEnabled());
    }

    @Test
    void isEnabled_whenFalse_returnsFalse() {
        User user = createUser(Set.of(User.Role.PROVIDER));
        user.setEnabled(false);
        assertFalse(user.isEnabled());
    }

    @Test
    void isAccountNonExpired_whenTrue_returnsTrue() {
        User user = createUser(Set.of(User.Role.PROVIDER));
        assertTrue(user.isAccountNonExpired());
    }

    @Test
    void isAccountNonExpired_whenFalse_returnsFalse() {
        User user = createUser(Set.of(User.Role.PROVIDER));
        user.setAccountNonExpired(false);
        assertFalse(user.isAccountNonExpired());
    }

    @Test
    void isAccountNonLocked_whenTrue_returnsTrue() {
        User user = createUser(Set.of(User.Role.PROVIDER));
        assertTrue(user.isAccountNonLocked());
    }

    @Test
    void isAccountNonLocked_whenFalse_returnsFalse() {
        User user = createUser(Set.of(User.Role.PROVIDER));
        user.setAccountNonLocked(false);
        assertFalse(user.isAccountNonLocked());
    }

    @Test
    void isCredentialsNonExpired_whenTrue_returnsTrue() {
        User user = createUser(Set.of(User.Role.PROVIDER));
        assertTrue(user.isCredentialsNonExpired());
    }

    @Test
    void isCredentialsNonExpired_whenFalse_returnsFalse() {
        User user = createUser(Set.of(User.Role.PROVIDER));
        user.setCredentialsNonExpired(false);
        assertFalse(user.isCredentialsNonExpired());
    }

    @Test
    void builder_setsAllFields() {
        User user = User.builder()
                .id(42L)
                .username("admin")
                .password("secret")
                .email("admin@medchart.com")
                .firstName("Admin")
                .lastName("User")
                .roles(Set.of(User.Role.ADMIN))
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();

        assertEquals(42L, user.getId());
        assertEquals("admin", user.getUsername());
        assertEquals("secret", user.getPassword());
        assertEquals("admin@medchart.com", user.getEmail());
        assertEquals("Admin", user.getFirstName());
        assertEquals("User", user.getLastName());
        assertTrue(user.getRoles().contains(User.Role.ADMIN));
    }

    @Test
    void roleEnum_containsExpectedValues() {
        assertEquals(3, User.Role.values().length);
        assertNotNull(User.Role.valueOf("PROVIDER"));
        assertNotNull(User.Role.valueOf("ADMIN"));
        assertNotNull(User.Role.valueOf("STAFF"));
    }
}
