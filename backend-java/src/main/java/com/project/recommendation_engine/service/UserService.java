package com.project.recommendation_engine.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.project.recommendation_engine.model.Rating;
import com.project.recommendation_engine.model.User;
import com.project.recommendation_engine.repository.UserRepository;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final RecommendationAgentService recommendationAgentService;

    @Autowired
    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, RecommendationAgentService recommendationAgentService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.recommendationAgentService = recommendationAgentService;
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

    public String getUserIdByUsername(String username) {
        // Find user by username and return their ID
        return userRepository.findByUsername(username)
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("User with username " + username + " not found."));
    }

    public List<String> getFavoriteGenresByUsername(String username) {
        // Find user by username and get their favorite genres
        return userRepository.findByUsername(username)
                .map(User::getFavoriteGenres)
                .orElseThrow(() -> new RuntimeException("User with username " + username + " not found or genres not set."));
    }

    public User findByUsername(String username) {
        // Find user by username and return the User object
        return userRepository.findByUsername(username)
                .orElse(null);
    }

    public void addMovieRatings(String userId, List<Rating> newRatings) {
        userRepository.findById(userId).ifPresent(user -> {

            if (user.getMovieRatings() == null) {
                user.setMovieRatings(new ArrayList<>());
            }

            user.getMovieRatings().addAll(newRatings);
            userRepository.save(user);
            recommendationAgentService.triggerRecommendationForUser(userId);
        });
    }

    public void addOrUpdateRating(String username, String movieId, Double ratingValue) {
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            List<Rating> ratings = user.getMovieRatings();

            if (ratings == null) {
                ratings = new ArrayList<>();
            }

            // UPSERT: Delete if already exists to overwrite
            ratings.removeIf(r -> r.getMovieId().equals(movieId));

            Rating newRating = new Rating();
            newRating.setMovieId(movieId);

            newRating.setScore(ratingValue);

            ratings.add(newRating);
            user.setMovieRatings(ratings);
            userRepository.save(user);

        } else {
            throw new RuntimeException("User not found.");
        }
    }
}
