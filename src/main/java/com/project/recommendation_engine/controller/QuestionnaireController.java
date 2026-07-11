package com.project.recommendation_engine.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.project.recommendation_engine.model.GenreMovies;
import com.project.recommendation_engine.service.TMDBService;
import com.project.recommendation_engine.service.UserService;

@Controller
@RequestMapping("/questionnaire")
public class QuestionnaireController {

    private final UserService userService;
    private final TMDBService tmdbService;

    @Autowired
    public QuestionnaireController(UserService userService, TMDBService tmdbService) {
        this.userService = userService;
        this.tmdbService = tmdbService;
    }

    @GetMapping
    public String showQuestionnaire(@RequestParam String userId, Model model) {
        if (userId == null || userId.trim().isEmpty()) {
            return "redirect:/register";
        }
        model.addAttribute("userId", userId);
        return "questionnaire";
    }


    @PostMapping
    public String handleGenreSelection(
            @RequestParam String userId,
            @RequestParam List<String> genres, // Get all genres selected and store them in a List
            RedirectAttributes redirectAttributes
    ) {
        // 1. Store genres into object User in Mongo
        userService.saveFavoriteGenres(userId, genres);

        // 2. Get a list of movies by each genre IN-PROGRESS
        List<GenreMovies> moviesForRating = tmdbService.fetchMoviesForGenres(genres);

        redirectAttributes.addFlashAttribute("genresWithMovies", moviesForRating);
        redirectAttributes.addAttribute("userId", userId);

        // Redirect
        return "redirect:/rating-form";
    }
}
