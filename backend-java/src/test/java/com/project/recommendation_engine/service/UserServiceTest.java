package com.project.recommendation_engine.service;

import com.project.recommendation_engine.model.User;
import com.project.recommendation_engine.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private BCryptPasswordEncoder passwordEncoder;
    @Mock
    private RecommendationAgentService recommendationAgentService;

    @InjectMocks
    private UserService userService;

    @Test
    void testRegisterUser_Success() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setPassword("1234");

        when(userRepository.existsByEmail("user@example.com")).thenReturn(false);
        when(passwordEncoder.encode("1234")).thenReturn("$2a$10$hashedvalue");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User savedUser = userService.registerUser(user);

        assertNotEquals("1234", savedUser.getPassword());
        assertEquals("$2a$10$hashedvalue", savedUser.getPassword());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testRegisterUser_EmailAlreadyExists() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setPassword("1234");

        when(userRepository.existsByEmail("user@example.com")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.registerUser(user));
        assertEquals("This email already exists.", exception.getMessage());

        verify(userRepository, never()).save(any(User.class));
    }

}
