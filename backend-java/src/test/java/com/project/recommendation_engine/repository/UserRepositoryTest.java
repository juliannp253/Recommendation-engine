package com.project.recommendation_engine.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserRepositoryTest {
    @Test
    @DisplayName("UserRepository interface should declare findByEmail")
    void interfceShouldDeclareFindByEmail() throws NoSuchMethodException {
        assertNotNull(UserRepository.class.getMethod("findByEmail", String.class));
    }

    @Test
    @DisplayName("UserRepository interface should declar existsByEmail")
    void interfaceShouldDeclareExistsByEmail() throws NoSuchMethodException {
        assertNotNull(UserRepository.class.getMethod("existsByEmail", String.class));
    }
}

