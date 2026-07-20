package com.project.recommendation_engine.controller;
import java.util.ArrayList;
import java.util.List;

import com.project.recommendation_engine.model.User;
import com.project.recommendation_engine.repository.UserRepository;
import com.project.recommendation_engine.service.RecommendationAgentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import com.project.recommendation_engine.model.TMDBResponse;
import com.project.recommendation_engine.model.UserRecommendation;
import com.project.recommendation_engine.repository.RecommendationRepository;
import com.project.recommendation_engine.service.TMDBService;
import com.project.recommendation_engine.service.UserService;


@Slf4j
@Controller
public class HomeController {

    private final TMDBService tmdbService;
    private final UserService userService;
    private final RecommendationRepository recommendationRepository;
    private final UserRepository userRepository;
    private final RecommendationAgentService agentService;

    public HomeController(TMDBService tmdbService,
                          UserService userService,
                          RecommendationRepository recommendationRepository,
                          UserRepository userRepository,
                          RecommendationAgentService agentService){
        this.tmdbService = tmdbService;
        this.userService = userService;
        this.recommendationRepository = recommendationRepository;
        this.userRepository = userRepository;
        this.agentService = agentService;
    }

    @GetMapping("/home")
    public String home(Model model,
                       Authentication authentication,
                       @RequestParam(value = "refresh", required = false) String refresh){

        String email = authentication.getName();
        
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(()-> new RuntimeException("User not found."));
        String userId = currentUser.getId();
        
        List<TMDBResponse> trendingMoviesList = tmdbService.fetchTrendingMovies();
        List<UserRecommendation.RecSection> recommendationSections = new ArrayList<>();

        try {
            var recommendationOpt = recommendationRepository.findFirstByUserIdOrderByGeneratedAtDesc(userId);

            if (recommendationOpt.isPresent()) {
                recommendationSections = recommendationOpt.get().getSections();
            } else {
                log.info("No recommendations found for user ID: " + userId);
            }
        } catch (Exception e) {
            log.error("Error fetching recommendations from DB: " + e.getMessage());
        }

        model.addAttribute("trendingMoviesList", trendingMoviesList != null ? trendingMoviesList : new ArrayList<>());
        model.addAttribute("recommendationSections", recommendationSections);
        model.addAttribute("userId", userId);

        return "home";
    }

    @GetMapping("/movie/{title}")
    public String movieView(@PathVariable String title, Model model) {
        try {
            TMDBResponse movie = tmdbService.fetchRawMovieResponse(title);
            if (movie != null && "True".equalsIgnoreCase(movie.getResponse())) {
                model.addAttribute("movie", movie);
                return "movieView";
            } else {
                log.warn("Movie not found or invalid request for: {}", title);
            }
        } catch (Exception e) {
            log.error("Error fetching movie with title: " + title + " - " + e.getMessage());
        }
        
        return "redirect:/home";
    }

    @PostMapping("/rate-movie")
    public ResponseEntity<String> rateMovie(
            @RequestParam String movieId,
            @RequestParam Double rating,
            Authentication authentication){
        try {

            if (rating < 0.0 || rating > 5.0) {
                log.warn("Rating out of range: {} for movie {}", rating, movieId);
                return ResponseEntity.badRequest().body("Invalid rating.");
            }

            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found."));
            
            userService.addOrUpdateRating(user.getId(), movieId, rating);
            return ResponseEntity.ok("Rating saved succesfully");
        } catch (Exception e) {
            log.error("Error saving rating: " + e.getMessage());
            return ResponseEntity.internalServerError().body("ERROR trying to save new rating.");
        }
    }

    @PostMapping("/api/trigger-demo-agent")
    @ResponseBody
    public ResponseEntity<String> triggerDemoAgent(@RequestParam String userId) {

        boolean success = agentService.runAgentForUserSync(userId);

        if (success) {
            return ResponseEntity.ok("Recommendations Updated");
        } else {
            return ResponseEntity.status(500).body("Error executing agent");
        }
    }
}