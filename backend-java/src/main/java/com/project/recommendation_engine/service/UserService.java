package com.project.recommendation_engine.service;

import com.project.recommendation_engine.model.User;
import com.project.recommendation_engine.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public User registerUser(User user) {
        if(userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("This email already exists.");
        }
        if(userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("This username already exists.");
        }

        String hasshedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(hasshedPassword);

        user.setCreatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }
}
