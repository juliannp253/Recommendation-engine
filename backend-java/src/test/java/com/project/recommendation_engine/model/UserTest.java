package com.project.recommendation_engine.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void testGettersAndSetters() {
        User user = new User();

        user.setId("1L");
        user.setUsername("user1");
        user.setEmail("user1@example.com");
        user.setPassword("securePassword");

        assertEquals("1L", user.getId());
        assertEquals("user1", user.getUsername());
        assertEquals("user1@example.com", user.getEmail());
        assertEquals("securePassword", user.getPassword());
    }

    @Test
    void testOnCreateSetsCreatedAt() {
        User user = new User();
        user.onCreate();

        assertNotNull(user.getCreatedAt());
        assertTrue(user.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }
}
