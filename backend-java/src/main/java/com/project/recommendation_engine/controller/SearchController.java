package com.project.recommendation_engine.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.project.recommendation_engine.MovieResponse;
import com.project.recommendation_engine.service.OmdbService;

@Controller
public class SearchController {
    @Autowired
    private OmdbService omdbService;
    
    @GetMapping("/search")
    public String search() {
        return "search";
    }
    

    @PostMapping("/search")
    public String searchMovies(@RequestParam("movieTitle") String movieTitle, Model model) {
        model.addAttribute("searchQuery", movieTitle);
        MovieResponse response = omdbService.searchMovie(movieTitle); // Call the OmdbService
        if (response != null && "True".equalsIgnoreCase(response.getResponse())) { // if we get a response then send the data to the page
            model.addAttribute("movie", response);
            model.addAttribute("searchResults", "found");
        } else { 
            model.addAttribute("searchResults", "No results found for: " + movieTitle);
        }
        return "search"; // render the search page again
    }
}