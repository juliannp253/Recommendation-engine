package com.project.recommendation_engine.repository;

import com.project.recommendation_engine.IntegrationTestBase;
import com.project.recommendation_engine.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataMongoTest(properties = "spring.cache.type=none")
class UserRepositoryIntegrationTest extends IntegrationTestBase {
    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void cleanUsers() {
        userRepository.deleteAll();
    }

    @Test
    void findByEmailReturnsOnlyMatchingUser() {
        User user = new User();
        user.setEmail("alice@example.com");
        user.setPassword("stored-password");
        userRepository.save(user);

        assertEquals(user.getId(), userRepository.findByEmail("alice@example.com").orElseThrow().getId());
        assertTrue(userRepository.findByEmail("missing@example.com").isEmpty());
    }

    @Test
    void existsByEmailReflectsPersistedUsers() {
        assertFalse(userRepository.existsByEmail("alice@example.com"));

        User user = new User();
        user.setEmail("alice@example.com");
        userRepository.save(user);

        assertTrue(userRepository.existsByEmail("alice@example.com"));
        assertFalse(userRepository.existsByEmail("missing@example.com"));
    }

    @Test
    void savePersistsUserAndAssignsId() {
        User user = new User();
        user.setEmail("alice@example.com");
        user.setPassword("stored-password");

        User saved = userRepository.save(user);

        assertNotNull(saved.getId());
        User reloaded = userRepository.findById(saved.getId()).orElseThrow();
        assertEquals("alice@example.com", reloaded.getEmail());
        assertEquals("stored-password", reloaded.getPassword());
    }
}
