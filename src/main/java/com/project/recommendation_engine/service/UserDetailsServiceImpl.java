package com.project.recommendation_engine.service;

import com.project.recommendation_engine.model.User;
import com.project.recommendation_engine.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    // Injcts repository
    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {

        // Look for user in MongoDB by username or email
        User user = userRepository.findByUsername(usernameOrEmail)
                // If username does not exists, looks by email
                .orElseGet(() -> userRepository.findByEmail(usernameOrEmail)
                        // No email nor username, throw error
                        .orElseThrow(() -> new UsernameNotFoundException(
                                "User Not Found: " + usernameOrEmail)));

        // Converts model user into UserDetails object
       return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.emptyList());
    }
}