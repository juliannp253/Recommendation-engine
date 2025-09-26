package com.project.recommendation_engine.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.project.recommendation_engine.MovieResponse;


// This service will call the OMDB Api and return the information
// as a MovieResponse object. 
@Service
public class OmdbService {
    @Value("${omdb.api.key}")
    private String apiKey;

    private final String OMDB_URL = "http://www.omdbapi.com/?apikey=%s&t=%s&plot=full";

    public MovieResponse searchMovie(String title) {
        RestTemplate restTemplate = new RestTemplate();
        String url = String.format(OMDB_URL, apiKey, title.replace(" ", "+"));
        return restTemplate.getForObject(url, MovieResponse.class);
    }
}
