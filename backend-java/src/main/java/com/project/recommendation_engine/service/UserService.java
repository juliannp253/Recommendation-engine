package com.project.recommendation_engine.service;

import com.project.recommendation_engine.model.Rating;
import com.project.recommendation_engine.model.User;
import com.project.recommendation_engine.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

// import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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

        return userRepository.save(user);
    }

    public void saveFavoriteGenres(String userId, List<String> genres) {
        // Search User by ID
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            // Add Genres list to User
            user.setFavoriteGenres(genres);

            // Save User into MongoDB
            userRepository.save(user);
        } else {
            throw new RuntimeException("User with ID " + userId + " not found.");
        }
    }

    public List<String> getFavoriteGenresByUserId(String userId) {
        // User findById comes from MongoRepository
        return userRepository.findById(userId)
                .map(User::getFavoriteGenres)
                .orElseThrow(() -> new RuntimeException("User not found or genres not set."));
    }

    public void addMovieRatings(String userId, List<Rating> newRatings) {
        userRepository.findById(userId).ifPresent(user -> {

            if (user.getMovieRatings() == null) {
                user.setMovieRatings(new ArrayList<>());
            }

            user.getMovieRatings().addAll(newRatings);
            userRepository.save(user);
        });
    }
}
