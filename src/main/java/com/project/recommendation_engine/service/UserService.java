package com.project.recommendation_engine.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.recommendation_engine.model.Rating;
import com.project.recommendation_engine.model.User;
import com.project.recommendation_engine.repository.UserRepository;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final RecommendationAgentService recommendationAgentService;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, RecommendationAgentService recommendationAgentService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.recommendationAgentService = recommendationAgentService;
    }

    public User registerUser(User user) {
        if(userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("This email already exists.");
        }
        String hasshedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(hasshedPassword);

        return userRepository.save(user);
    }

    public void saveFavoriteGenres(String userId, List<String> genres) {
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setFavoriteGenres(genres);

            userRepository.save(user);
        } else {
            throw new RuntimeException("User with ID " + userId + " not found.");
        }
    }

    public List<String> getFavoriteGenresByUserId(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User " + userId + " not found."));

        List<String> genres = user.getFavoriteGenres();
        return genres != null ? genres : Collections.emptyList();
    }

    /*public String getUserIdByUsername(String username) {

        return userRepository.findByUsername(username)
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("User with username " + username + " not found."));
    }*/

    /*public List<String> getFavoriteGenresByUsername(String username) {
        // Find user by username and get their favorite genres
        return userRepository.findByUsername(username)
                .map(User::getFavoriteGenres)
                .orElseThrow(() -> new RuntimeException("User with username " + username + " not found or genres not set."));
    }*/

    /*public User findByUsername(String username) {
        // Find user by username and return the User object
        return userRepository.findByUsername(username)
                .orElse(null);
    }*/

    @Transactional
    public void addMovieRatings(String userId, List<Rating> newRatings) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User " + userId + " not found."));
        user.getMovieRatings().addAll(newRatings);
        userRepository.save(user);
        recommendationAgentService.triggerRecommendationForUser(userId); // Implementar RabbitMQ
    }

    @Transactional
    public void addOrUpdateRating(String userId, String movieId, Double ratingValue) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User " + userId + " not found."));

        List<Rating> ratings = user.getMovieRatings();

        ratings.removeIf(r -> r.getMovieId().equals(movieId));

        Rating newRating = new Rating();
        newRating.setMovieId(movieId);
        newRating.setScore(ratingValue);

        ratings.add(newRating);

        userRepository.save(user);
    }
}
