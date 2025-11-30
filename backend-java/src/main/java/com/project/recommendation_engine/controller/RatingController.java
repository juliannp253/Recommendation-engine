package com.project.recommendation_engine.controller;

import com.project.recommendation_engine.model.RatingRequest;
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

    @Autowired
    private UserService userService;

    @PostMapping("/api/rate")
    public ResponseEntity<String> rateMovie(@RequestBody RatingRequest request) {
        // Get current user from session
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        if (username != null && request.getMovieId() != null) {
            // Save rating
            userService.addOrUpdateRating(username, request.getMovieId(), request.getRating());
            return ResponseEntity.ok("Rating saved successfully");
        }

        return ResponseEntity.badRequest().body("Invalid data");
    }
}