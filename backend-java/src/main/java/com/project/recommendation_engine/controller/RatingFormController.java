package com.project.recommendation_engine.controller;

import com.project.recommendation_engine.model.GenreMovies;
import com.project.recommendation_engine.model.Movie;
import com.project.recommendation_engine.model.Rating;
import com.project.recommendation_engine.service.UserService;
import com.project.recommendation_engine.service.OmdbService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/rating-form")
public class RatingFormController {

    private final UserService userService;
    private final OmdbService omdbService;

    @Autowired
    public RatingFormController(UserService userService, OmdbService omdbService) {
        this.userService = userService;
        this.omdbService = omdbService;
    }

    @GetMapping
    public String showRatingForm(@RequestParam String userId, Model model) {

        // Check for userId presence
        if (userId == null || userId.trim().isEmpty()) {
            return "redirect:/register";
        }

        try {
            // Get favorite genres from userId
            List<String> favoriteGenres = userService.getFavoriteGenresByUserId(userId);

            if (favoriteGenres == null || favoriteGenres.isEmpty()) {
                // If no genres
                return "redirect:/questionnaire?userId=" + userId;
            }

            // Give me movies for each of these genres
            List<GenreMovies> genresWithMovies = omdbService.fetchMoviesForGenres(favoriteGenres);

            // Give data to the model in Thymeleaf
            model.addAttribute("userId", userId);
            model.addAttribute("genresWithMovies", genresWithMovies);

        } catch (RuntimeException e) {
            System.err.println("Error fetching user data or movies: " + e.getMessage());
            return "redirect:/register";
        }

        return "rating-form";
    }

    @PostMapping("/save-initial-ratings")
    public String saveInitialRatings(@RequestParam String userId,
                                     @RequestParam Map<String, String> allParams)
    {

        if (userId == null || userId.trim().isEmpty()) {
            return "redirect:/register";
        }

        List<Rating> ratingsToSave = new ArrayList<>();

        for (Map.Entry<String, String> entry : allParams.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if (key.startsWith("rating_") && !value.equals("0")) {
                try {
                    String movieId = key.substring("rating_".length()); // Movie ID
                    double score = Double.parseDouble(value); // Movie rating

                    ratingsToSave.add(new Rating(movieId, score));
                } catch (NumberFormatException e) {
                    System.err.println("Skipping invalid rating value: " + value);
                }
            }
        }

        // At least review 5 movies
        if (ratingsToSave.size() < 5) {
            System.err.println("Warning: User did not rate the minimum number of movies.");
        }

        // Save ratings
        userService.addMovieRatings(userId, ratingsToSave);

        // Redirect to Home Page once finished the Registration IN-PROGRESS
        return "redirect:/home";
    }

    @PostMapping("/search-manual")
    public String searchManualMovie(
            @RequestParam String userId,
            @RequestParam String manualTitle,
            Model model) {


        // Give data to render view (Keep original data when searching a movie)
        List<String> favoriteGenres = userService.getFavoriteGenresByUserId(userId);
        List<GenreMovies> genresWithMovies = omdbService.fetchMoviesForGenres(favoriteGenres);

        model.addAttribute("userId", userId);
        model.addAttribute("genresWithMovies", genresWithMovies);

        // Search a new movie
        Movie foundMovie = omdbService.searchMovie(manualTitle);

        if (foundMovie != null) {
            model.addAttribute("manualMovie", foundMovie);
        } else {
            model.addAttribute("searchError", "No Results For Movie: " + manualTitle);
        }

        return "rating-form";
    }


    @GetMapping("/api/search")
    @ResponseBody
    public ResponseEntity<Movie> searchMovieApi(@RequestParam String title) {

        Movie foundMovie = omdbService.searchMovie(title);

        if (foundMovie != null) {
            return ResponseEntity.ok(foundMovie);
        } else {
            // HTTP 404 (Not Found)
            return ResponseEntity.notFound().build();
        }
    }
}