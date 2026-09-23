package com.project.recommendation_engine.controller;

import com.project.recommendation_engine.model.RatingRequest;
import com.project.recommendation_engine.model.User;
import com.project.recommendation_engine.repository.UserRepository;
import com.project.recommendation_engine.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RatingController {

    private final UserService userService;
    private final UserRepository userRepository;

    public RatingController(UserService userService, UserRepository userRepository ) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @PostMapping("/api/rate")
    public ResponseEntity<String> rateMovie(@RequestBody RatingRequest request, Authentication authentication) {
        String email = authentication.getName();
        if (email != null && request.getMovieId() != null) {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found."));
            userService.addOrUpdateRating(user.getId(), request.getMovieId(), request.getRating());
            return ResponseEntity.ok("Rating saved succesfully");
        } else { return ResponseEntity.status(500).body("Error saving rating"); }
    }
}