package com.project.recommendation_engine.controller;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.project.recommendation_engine.MovieResponse;
import com.project.recommendation_engine.service.OmdbService;

import jakarta.servlet.http.HttpSession;

//@RestController
@Controller
public class HomeController {

    @Autowired
    private OmdbService omdbService;

    @GetMapping("/home")
    public String home(Model model, HttpSession session){
        // Check if movies are already cached in session
        @SuppressWarnings("unchecked")
        List<MovieResponse> sciFiMovieList = (List<MovieResponse>) session.getAttribute("sciFiMovies");
        
        // If not in session, fetch from API and cache
        if (sciFiMovieList == null) {
            sciFiMovieList = new ArrayList<>();
            List<String> sciFiMovies = Arrays.asList("Blade Runner", "The Matrix", "Interstellar", "Star Wars");

            // Fetch movie data from OMDB API
            for (String movieTitle : sciFiMovies) {
                try {
                    MovieResponse movie = omdbService.fetchRawMovieResponse(movieTitle);
                    if (movie != null && "True".equals(movie.getResponse())) {
                        sciFiMovieList.add(movie);
                    }
                } catch (Exception e) {
                    // Log error and continue with other movies
                    System.err.println("Error fetching movie: " + movieTitle + " - " + e.getMessage());
                }
            }
            
            // Save the movies in the session so that no extra api calls are needed
            session.setAttribute("sciFiMovies", sciFiMovieList);
        }

        model.addAttribute("sciFiMovies", sciFiMovieList);
        return "home";
    }

    @GetMapping("/movie/{imdbId}")
    public String movieView(@PathVariable String imdbId, Model model, HttpSession session) {
        System.out.println("DEBUG: MovieView called with imdbId: " + imdbId);
        
        // Get the cached movies from session
        @SuppressWarnings("unchecked")
        List<MovieResponse> sciFiMovieList = (List<MovieResponse>) session.getAttribute("sciFiMovies");
        
        if (sciFiMovieList != null) {
            // Find the movie with matching IMDb ID from the session list
            MovieResponse selectedMovie = sciFiMovieList.stream()
                .filter(movie -> imdbId.equals(movie.getImdbID()))
                .findFirst()
                .orElse(null);
            
            if (selectedMovie != null) {
                model.addAttribute("movie", selectedMovie);
                return "movieView";
            }
        }
        
        // If movie not found in session, try to fetch it directly from API
        try {
            MovieResponse movie = omdbService.fetchRawMovieResponse(imdbId);
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