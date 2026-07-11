package com.project.recommendation_engine.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.project.recommendation_engine.model.TMDBResponse;
import com.project.recommendation_engine.service.TMDBService;
import com.project.recommendation_engine.service.UserService;

@Controller
public class SearchController {
    @Autowired
    private TMDBService tmdbService;
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/search")
    public String search() {
        return "search";
    }
    

    @PostMapping("/search")
    public String searchMovies(@RequestParam("movieTitle") String movieTitle, Model model) {
        model.addAttribute("searchQuery", movieTitle);
        TMDBResponse movieResult = (TMDBResponse) tmdbService.fetchRawMovieResponse(movieTitle); // Call the TMDBService
        if (movieResult != null && "True".equals(movieResult.getResponse())) { // Check for successful response
            model.addAttribute("movie", movieResult);
            model.addAttribute("searchResults", "found");
        } else { 
            model.addAttribute("searchResults", "No results found for: " + movieTitle);
        }
        return "search"; // render the search page again
    }

    @PostMapping("/search/rate-movie")
    @ResponseBody
    public ResponseEntity<String> rateMovie(
            @RequestParam String movieId,
            @RequestParam Double rating) {
        
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication.getName();
            
            userService.addOrUpdateRating(currentUsername, movieId, rating);
            
            return ResponseEntity.ok("Rating saved successfully");
        } catch (Exception e) {
            System.err.println("Error saving rating: " + e.getMessage());
            return ResponseEntity.status(500).body("Error saving rating");
        }
    }
}