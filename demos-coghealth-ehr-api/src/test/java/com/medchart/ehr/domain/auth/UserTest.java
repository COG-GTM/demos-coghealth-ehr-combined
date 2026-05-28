package com.medchart.ehr.domain.auth;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void getAuthorities_mapsRolesToGrantedAuthorities() {
        User user = User.builder()
                .username("dr.smith")
                .password("password")
                .email("dr.smith@medchart.com")
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
        assertEquals("ROLE_PROVIDER",
                authorities.iterator().next().getAuthority());
    }

    @Test
    void getAuthorities_mapsMultipleRoles() {
        User user = User.builder()
                .username("admin")
                .password("password")
                .email("admin@medchart.com")
                .firstName("Admin")
                .lastName("User")
                .roles(Set.of(User.Role.ADMIN, User.Role.PROVIDER, User.Role.STAFF))
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();

        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

        assertEquals(3, authorities.size());
        Set<String> authorityNames = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        assertTrue(authorityNames.contains("ROLE_ADMIN"));
        assertTrue(authorityNames.contains("ROLE_PROVIDER"));
        assertTrue(authorityNames.contains("ROLE_STAFF"));
    }

    @Test
    void getAuthorities_prefixesRolesWithROLE() {
        User user = User.builder()
                .username("staff")
                .password("password")
                .email("staff@medchart.com")
                .firstName("Staff")
                .lastName("Member")
                .roles(Set.of(User.Role.STAFF))
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();

        String authority = user.getAuthorities().iterator().next().getAuthority();
        assertTrue(authority.startsWith("ROLE_"));
    }

    @Test
    void isEnabled_reflectsFieldValue() {
        User enabledUser = User.builder()
                .username("u1").password("p").email("e1@e.com")
                .firstName("F").lastName("L")
                .roles(Set.of(User.Role.PROVIDER))
                .enabled(true)
                .accountNonExpired(true).accountNonLocked(true).credentialsNonExpired(true)
                .build();

        User disabledUser = User.builder()
                .username("u2").password("p").email("e2@e.com")
                .firstName("F").lastName("L")
                .roles(Set.of(User.Role.PROVIDER))
                .enabled(false)
                .accountNonExpired(true).accountNonLocked(true).credentialsNonExpired(true)
                .build();

        assertTrue(enabledUser.isEnabled());
        assertFalse(disabledUser.isEnabled());
    }

    @Test
    void isAccountNonLocked_reflectsFieldValue() {
        User lockedUser = User.builder()
                .username("locked").password("p").email("l@e.com")
                .firstName("F").lastName("L")
                .roles(Set.of(User.Role.PROVIDER))
                .enabled(true)
                .accountNonExpired(true).accountNonLocked(false).credentialsNonExpired(true)
                .build();

        assertFalse(lockedUser.isAccountNonLocked());
    }

    @Test
    void isAccountNonExpired_reflectsFieldValue() {
        User expiredUser = User.builder()
                .username("expired").password("p").email("x@e.com")
                .firstName("F").lastName("L")
                .roles(Set.of(User.Role.PROVIDER))
                .enabled(true)
                .accountNonExpired(false).accountNonLocked(true).credentialsNonExpired(true)
                .build();

        assertFalse(expiredUser.isAccountNonExpired());
    }

    @Test
    void isCredentialsNonExpired_reflectsFieldValue() {
        User credExpiredUser = User.builder()
                .username("credexp").password("p").email("c@e.com")
                .firstName("F").lastName("L")
                .roles(Set.of(User.Role.PROVIDER))
                .enabled(true)
                .accountNonExpired(true).accountNonLocked(true).credentialsNonExpired(false)
                .build();

        assertFalse(credExpiredUser.isCredentialsNonExpired());
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
                .id(42L)
                .username("dr.smith")
                .password("secret")
                .email("dr.smith@medchart.com")
                .firstName("John")
                .lastName("Smith")
                .roles(Set.of(User.Role.PROVIDER))
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();

        assertEquals(42L, user.getId());
        assertEquals("dr.smith", user.getUsername());
        assertEquals("secret", user.getPassword());
        assertEquals("dr.smith@medchart.com", user.getEmail());
        assertEquals("John", user.getFirstName());
        assertEquals("Smith", user.getLastName());
    }
}
