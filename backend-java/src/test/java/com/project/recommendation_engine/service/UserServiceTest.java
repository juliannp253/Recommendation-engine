package com.project.recommendation_engine.service;

import com.project.recommendation_engine.model.User;
import com.project.recommendation_engine.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegisterUser_Success() {
        User user = new User();
        user.setUsername("user");
        user.setEmail("user@example.com");
        user.setPassword("1234");

        when(userRepository.existsByEmail("user@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("user")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User savedUser = userService.registerUser(user);

        assertNotNull(savedUser.getCreatedAt());
        assertNotEquals("1234", savedUser.getPassword()); // Password now it's encrypted
        assertTrue(new BCryptPasswordEncoder().matches("1234", savedUser.getPassword()));
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testRegisterUser_EmailAlreadyExists() {
        User user = new User();
        user.setUsername("user");
        user.setEmail("user@example.com");
        user.setPassword("1234");

        when(userRepository.existsByEmail("user@example.com")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.registerUser(user));
        assertEquals("This email already exists.", exception.getMessage());

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testRegisterUser_UsernameAlreadyExists() {
        User user = new User();
        user.setUsername("user");
        user.setEmail("user@example.com");
        user.setPassword("1234");

        when(userRepository.existsByEmail("user@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("user")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.registerUser(user));
        assertEquals("This username already exists.", exception.getMessage());

        verify(userRepository, never()).save(any(User.class));
    }
}
