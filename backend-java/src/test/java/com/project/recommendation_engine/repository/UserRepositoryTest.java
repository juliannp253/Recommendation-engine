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

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Save user and find by email")
    void testFindByEmail() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setPassword("1234");
        userRepository.save(user);

        Optional<User> found = userRepository.findByEmail("user@example.com");

        assertTrue(found.isPresent());
        assertEquals("user", found.get().getEmail());
    }

    @Test
    @DisplayName("Exists by email should return true if user exists")
    void testExistsByEmail() {
        User user = new User();
        user.setEmail("fernando@example.com");
        user.setPassword("safe");
        userRepository.save(user);

        boolean exists = userRepository.existsByEmail("fernando@example.com");

        assertTrue(exists);
    }
}

