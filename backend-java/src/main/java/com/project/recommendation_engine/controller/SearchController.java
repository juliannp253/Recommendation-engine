package com.project.recommendation_engine.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.project.recommendation_engine.model.TMDBResponse;
import com.project.recommendation_engine.service.TMDBService;

@Controller
public class SearchController {
    @Autowired
    private TMDBService tmdbService;
    
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
}