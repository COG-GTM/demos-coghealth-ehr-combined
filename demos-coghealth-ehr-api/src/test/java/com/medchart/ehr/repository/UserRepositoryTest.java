package com.medchart.ehr.repository;

import com.medchart.ehr.domain.auth.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .username("johndoe")
                .password("encodedPassword")
                .email("john.doe@example.com")
                .firstName("John")
                .lastName("Doe")
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();

        entityManager.persist(testUser);
        entityManager.flush();
    }

    @Test
    void testFindByUsername() {
        Optional<User> found = userRepository.findByUsername("johndoe");

        assertTrue(found.isPresent());
        assertEquals("johndoe", found.get().getUsername());
        assertEquals("john.doe@example.com", found.get().getEmail());
    }

    @Test
    void testFindByUsernameNotFound() {
        Optional<User> found = userRepository.findByUsername("nonexistent");

        assertFalse(found.isPresent());
    }

    @Test
    void testFindByEmail() {
        Optional<User> found = userRepository.findByEmail("john.doe@example.com");

        assertTrue(found.isPresent());
        assertEquals("john.doe@example.com", found.get().getEmail());
        assertEquals("johndoe", found.get().getUsername());
    }

    @Test
    void testFindByEmailNotFound() {
        Optional<User> found = userRepository.findByEmail("nonexistent@example.com");

        assertFalse(found.isPresent());
    }

    @Test
    void testExistsByUsername() {
        assertTrue(userRepository.existsByUsername("johndoe"));
        assertFalse(userRepository.existsByUsername("nonexistent"));
    }

    @Test
    void testExistsByEmail() {
        assertTrue(userRepository.existsByEmail("john.doe@example.com"));
        assertFalse(userRepository.existsByEmail("nonexistent@example.com"));
    }

    @Test
    void testSaveAndFindById() {
        User newUser = User.builder()
                .username("janedoe")
                .password("encodedPassword")
                .email("jane.doe@example.com")
                .firstName("Jane")
                .lastName("Doe")
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();

        User saved = userRepository.save(newUser);
        
        Optional<User> found = userRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals("janedoe", found.get().getUsername());
        assertEquals("jane.doe@example.com", found.get().getEmail());
    }

    @Test
    void testDeleteUser() {
        Long id = testUser.getId();
        
        userRepository.delete(testUser);
        
        Optional<User> found = userRepository.findById(id);
        assertFalse(found.isPresent());
    }
}