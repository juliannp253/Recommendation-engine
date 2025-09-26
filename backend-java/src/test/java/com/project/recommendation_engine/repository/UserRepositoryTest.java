package com.project.recommendation_engine.repository;
/*
    Integration Test: Methods works with DB
*/

import com.project.recommendation_engine.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest   // Wake up only JPA layer (H2 and UserRepository)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Save user and find by email")
    void testFindByEmail() {
        User user = new User();
        user.setUsername("user");
        user.setEmail("user@example.com");
        user.setPassword("1234");
        userRepository.save(user);

        Optional<User> found = userRepository.findByEmail("user@example.com");

        assertTrue(found.isPresent());
        assertEquals("user", found.get().getUsername());
    }

    @Test
    @DisplayName("Save user and find by username")
    void testFindByUsername() {
        User user = new User();
        user.setUsername("ernesto");
        user.setEmail("ernesto@example.com");
        user.setPassword("safe");
        userRepository.save(user);

        Optional<User> found = userRepository.findByUsername("ernesto");

        assertTrue(found.isPresent());
        assertEquals("ernesto@example.com", found.get().getEmail());
    }

    @Test
    @DisplayName("Exists by email should return true if user exists")
    void testExistsByEmail() {
        User user = new User();
        user.setUsername("fernando");
        user.setEmail("fernando@example.com");
        user.setPassword("safe");
        userRepository.save(user);

        boolean exists = userRepository.existsByEmail("fernando@example.com");

        assertTrue(exists);
    }

    @Test
    @DisplayName("Exists by username should return true if user exists")
    void testExistsByUsername() {
        User user = new User();
        user.setUsername("julian");
        user.setEmail("julian@example.com");
        user.setPassword("safe");
        userRepository.save(user);

        boolean exists = userRepository.existsByUsername("julian");

        assertTrue(exists);
    }

    @Test
    @DisplayName("Exists by username should return false if user does not exist")
    void testExistsByUsername_UserNotFound() {
        boolean exists = userRepository.existsByUsername("carlos");

        assertFalse(exists);
    }
}

