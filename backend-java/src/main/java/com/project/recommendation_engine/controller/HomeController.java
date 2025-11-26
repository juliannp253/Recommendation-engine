package com.project.recommendation_engine.controller;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

// import com.project.recommendation_engine.model.GenreMovies; DELETE
import com.project.recommendation_engine.model.TMDBResponse;
import com.project.recommendation_engine.service.TMDBService;
import com.project.recommendation_engine.service.UserService;
import com.project.recommendation_engine.model.UserRecommendation;
import com.project.recommendation_engine.repository.RecommendationRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {

    @Autowired
    private TMDBService tmdbService;
    
    @Autowired
    private UserService userService;

    @Autowired
    private RecommendationRepository recommendationRepository;

    @GetMapping("/home")
    public String home(Model model, HttpSession session, @RequestParam(value = "refresh", required = false) String refresh){
        // Get current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        
        // Check if this is a different user than cached data
        String cachedUsername = (String) session.getAttribute("cachedUsername");
        
        // Check cache timestamp for automatic refresh (every 2 minutes)
        Long lastCacheTime = (Long) session.getAttribute("lastCacheTime");
        long currentTime = System.currentTimeMillis();
        long cacheExpiryTime = 2 * 60 * 1000; // 2 minutes in milliseconds
        
        boolean cacheExpired = lastCacheTime == null || (currentTime - lastCacheTime) > cacheExpiryTime;
        boolean manualRefresh = "true".equals(refresh);
        
        if (!currentUsername.equals(cachedUsername) || cacheExpired || manualRefresh) {
            // Clear cache if different user OR cache expired (for fresh movies)
            session.removeAttribute("trendingMovies");
            // session.removeAttribute("userMovies");
            // session.removeAttribute("sciFiMovies");
            session.setAttribute("cachedUsername", currentUsername);
            session.setAttribute("lastCacheTime", currentTime);
            // String reason = manualRefresh ? "Manual refresh" : (cacheExpired ? "Cache expired" : "New user: " + currentUsername);
        }
        
        // Check if movies are already cached in session
        @SuppressWarnings("unchecked")
        List<TMDBResponse> trendingMoviesList = (List<TMDBResponse>) session.getAttribute("trendingMovies");
        if (trendingMoviesList == null) {
            try {
                trendingMoviesList = tmdbService.fetchTrendingMovies();
                session.setAttribute("trendingMovies", trendingMoviesList);
            } catch (Exception e) {
                System.err.println("Error fetching trending movies: " + e.getMessage());
                trendingMoviesList = new ArrayList<>();
            }
        }

        List<UserRecommendation.RecSection> recommendationSections = new ArrayList<>();

        try {
            // Get user's ID based on its username
            String userId = userService.getUserIdByUsername(currentUsername);

            // Search most recent recommendation into 'recommended_cache' collection
            var recommendationOpt = recommendationRepository.findFirstByUserIdOrderByGeneratedAtDesc(userId);

            if (recommendationOpt.isPresent()) {
                recommendationSections = recommendationOpt.get().getSections();
            } else {
                System.out.println("DEBUG: No recommendations found for user ID: " + userId);
            }
        } catch (Exception e) {
            System.err.println("Error fetching recommendations from DB: " + e.getMessage());
        }

        // Send attributes to the view (home.html)
        model.addAttribute("trendingMoviesList", trendingMoviesList != null ? trendingMoviesList : new ArrayList<>());

        // This variable "recommendationSections" is the one we will loop now in the HTML
        model.addAttribute("recommendationSections", recommendationSections);

        return "home";
    }

    @GetMapping("/movie/{imdbId}")
    public String movieView(@PathVariable String imdbId, Model model, HttpSession session) {
        // System.out.println("DEBUG: MovieView called with imdbId: " + imdbId);
        
        // Get the cached movies from session (check both new and old session keys for compatibility)
        @SuppressWarnings("unchecked")
        List<TMDBResponse> trendingList = (List<TMDBResponse>) session.getAttribute("trendingMovies");
        TMDBResponse selectedMovie = null;

        if (trendingList != null) {
            selectedMovie = trendingList.stream()
                    .filter(movie -> imdbId.equals(movie.getImdbID()))
                    .findFirst()
                    .orElse(null);
        }

        if (selectedMovie != null) {
            model.addAttribute("movie", selectedMovie);
            return "movieView";
        }
        
        // If movie not found in session, try to fetch it directly from API
        try {
            TMDBResponse movie = (TMDBResponse) tmdbService.fetchRawMovieResponse(imdbId);
            if (movie != null && "True".equals(movie.getResponse())) {
                model.addAttribute("movie", movie);
                return "movieView";
            }
        } catch (Exception e) {
            System.err.println("Error fetching movie with ID: " + imdbId + " - " + e.getMessage());
        }
        
        // If all fails, redirect back to home
        return "redirect:/home";
    }
}