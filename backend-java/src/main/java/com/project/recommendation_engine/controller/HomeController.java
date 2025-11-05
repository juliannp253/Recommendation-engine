package com.project.recommendation_engine.controller;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.project.recommendation_engine.MovieResponse;
import com.project.recommendation_engine.model.GenreMovies;
import com.project.recommendation_engine.service.OmdbService;
import com.project.recommendation_engine.service.UserService;

import jakarta.servlet.http.HttpSession;

//@RestController
@Controller
public class HomeController {

    @Autowired
    private OmdbService omdbService;
    
    @Autowired
    private UserService userService;

    @GetMapping("/home")
    public String home(Model model, HttpSession session){
        // Get current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        
        // Check if this is a different user than cached data
        String cachedUsername = (String) session.getAttribute("cachedUsername");
        
        if (!currentUsername.equals(cachedUsername)) {
            // Different user - clear all cached movie data
            session.removeAttribute("genreMovies");
            session.removeAttribute("userMovies");
            session.removeAttribute("sciFiMovies");
            session.setAttribute("cachedUsername", currentUsername);
            // System.out.println("DEBUG: Cleared cache for new user: " + currentUsername);
        }
        
        // Check if movies are already cached in session
        @SuppressWarnings("unchecked")
        List<MovieResponse> userMovieList = (List<MovieResponse>) session.getAttribute("userMovies");
        
        // If not in session, fetch from API based on user's favorite genres
        if (userMovieList == null) {
            
            try {
                // Get user's favorite genres from UserService using username
                List<String> favoriteGenres = userService.getFavoriteGenresByUsername(currentUsername);
                
                // Fetch movies for each favorite genre using OmdbService
                List<GenreMovies> genreMoviesList = omdbService.fetchMoviesForGenres(favoriteGenres);
                
                // Convert each GenreMovies to use MovieResponse instead of Movie
                List<GenreMovies> processedGenreMovies = new ArrayList<>();
                
                for (GenreMovies genreMovies : genreMoviesList) {
                    List<MovieResponse> genreMovieResponses = new ArrayList<>();
                    
                    for (com.project.recommendation_engine.model.Movie movie : genreMovies.getMovies()) {
                        // Convert Movie to MovieResponse for template compatibility
                        try {
                            MovieResponse movieResponse = omdbService.fetchRawMovieResponse(movie.getId());
                            if (movieResponse != null && "True".equals(movieResponse.getResponse())) {
                                genreMovieResponses.add(movieResponse);
                            }
                        } catch (Exception e) {
                            System.err.println("Error fetching movie details for: " + movie.getTitle() + " - " + e.getMessage());
                        }
                    }
                    
                    // Create a new GenreMovies object with MovieResponse objects
                    // String genreName = favoriteGenres.get(processedGenreMovies.size()); // Get genre name from original list
                    String genreName = genreMovies.getGenreName();
                    processedGenreMovies.add(new GenreMovies(genreName, convertToMovies(genreMovieResponses)));
                }
                
                // Cache the processed genre movies in session
                session.setAttribute("genreMovies", processedGenreMovies);
                  
            
            } catch (Exception e) {
                System.err.println("Error fetching user's favorite genres: " + e.getMessage());
                // Fallback to default sci-fi movies if user genres not found
                List<String> defaultMovies = Arrays.asList("Blade Runner", "The Matrix", "Interstellar", "Star Wars");
                userMovieList = new ArrayList<>();
                for (String movieTitle : defaultMovies) {
                    try {
                        MovieResponse movie = omdbService.fetchRawMovieResponse(movieTitle);
                        if (movie != null && "True".equals(movie.getResponse())) {
                            userMovieList.add(movie);
                        }
                    } catch (Exception ex) {
                        System.err.println("Error fetching fallback movie: " + movieTitle + " - " + ex.getMessage());
                    }
                }
                
                // Create fallback genre structure
                List<GenreMovies> fallbackGenres = new ArrayList<>();
                fallbackGenres.add(new GenreMovies("Sci-Fi", convertToMovies(userMovieList)));
                session.setAttribute("genreMovies", fallbackGenres);
            }
            
            // Save the movies in the session so that no extra api calls are needed
            session.setAttribute("userMovies", userMovieList);
        }

        // Get genre movies from session for display
        @SuppressWarnings("unchecked")
        List<GenreMovies> genreMoviesList = (List<GenreMovies>) session.getAttribute("genreMovies");
        
        // System.out.println("DEBUG: genreMoviesList size: " + (genreMoviesList != null ? genreMoviesList.size() : "null"));
        
        // Debug: Print out what's in the genreMoviesList
        // if (genreMoviesList != null) {
        //     for (int i = 0; i < genreMoviesList.size(); i++) {
        //         GenreMovies gm = genreMoviesList.get(i);
        //         System.out.println("DEBUG: GenreMovies " + i + " - Class: " + gm.getClass().getSimpleName());
        //         System.out.println("DEBUG: GenreMovies " + i + " - toString: " + gm.toString());
        //     }
        // }
        
        // Ensure genreMoviesList is not null to prevent template errors
        if (genreMoviesList == null) {
            genreMoviesList = new ArrayList<>();
        }
        
        model.addAttribute("genreMoviesList", genreMoviesList);
        model.addAttribute("sciFiMovies", userMovieList != null ? userMovieList : new ArrayList<>()); 
        return "home";
    }
    
    // Helper method to convert MovieResponse list to Movie list for use with GenreMovies 
    private List<com.project.recommendation_engine.model.Movie> convertToMovies(List<MovieResponse> movieResponses) {
        List<com.project.recommendation_engine.model.Movie> movies = new ArrayList<>();
        for (MovieResponse response : movieResponses) {
            movies.add(new com.project.recommendation_engine.model.Movie(
                response.getImdbID(),
                response.getTitle(),
                response.getPoster()
            ));
        }
        return movies;
    }

    @GetMapping("/movie/{imdbId}")
    public String movieView(@PathVariable String imdbId, Model model, HttpSession session) {
        // System.out.println("DEBUG: MovieView called with imdbId: " + imdbId);
        
        // Get the cached movies from session (check both new and old session keys for compatibility)
        @SuppressWarnings("unchecked")
        List<MovieResponse> userMovieList = (List<MovieResponse>) session.getAttribute("userMovies");
        
        if (userMovieList == null) {
            // Fallback to old session key for backward compatibility
            @SuppressWarnings("unchecked")
            List<MovieResponse> sciFiMovieList = (List<MovieResponse>) session.getAttribute("sciFiMovies");
            userMovieList = sciFiMovieList;
        }
        
        if (userMovieList != null) {
            // Find the movie with matching IMDb ID from the session list
            MovieResponse selectedMovie = userMovieList.stream()
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