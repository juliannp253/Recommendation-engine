package com.project.recommendation_engine.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.project.recommendation_engine.MovieResponse;
import com.project.recommendation_engine.model.GenreMovies;
import com.project.recommendation_engine.model.Movie;



// This service will call the OMDB Api and return the information
// as a MovieResponse object. 
@Service
public class OmdbService {
    @Value("${omdb.api.key}")
    private String apiKey;

    private final String OMDB_URL = "http://www.omdbapi.com/?apikey=%s&t=%s&plot=full";
    private static final Map<String, List<String>> GENRE_MOVIE_TITLES = new HashMap<>();
    private final Executor taskExecutor;

    public OmdbService(@Value("${omdb.api.key}") String apiKey, Executor taskExecutor) {
        this.apiKey = apiKey;
        this.taskExecutor = taskExecutor;
    }

    public MovieResponse fetchRawMovieResponse(String titleOrId) {
        RestTemplate restTemplate = new RestTemplate();

        String url;
        // Check if it's an IMDb ID (starts with "tt") or a title
        if (titleOrId.startsWith("tt")) {
            // Search by IMDb ID
            url = String.format("http://www.omdbapi.com/?apikey=%s&i=%s&plot=full", 
                               apiKey, titleOrId);
        } else {
            // Search by title
            url = String.format("http://www.omdbapi.com/?apikey=%s&t=%s&plot=full", 
                               apiKey, titleOrId.replace(" ", "+"));
        }

        return restTemplate.getForObject(url, MovieResponse.class);
    }


    public Movie searchMovie(String title) {
        RestTemplate restTemplate = new RestTemplate();
        String url = String.format(OMDB_URL, apiKey, title.replace(" ", "+"));

        MovieResponse response = restTemplate.getForObject(url, MovieResponse.class);

        return mapResponseToMovie(response);
    }

    public List<GenreMovies> fetchMoviesForGenres(List<String> genres) {
        Map<String, List<String>> genreTitlesMap = Map.of(
                "ACTION", List.of("Mad Max: Fury Road", "Inception", "The Dark Knight", "John Wick", "Gladiator"),
                "COMEDY", List.of("The Hangover", "Superbad", "Anchorman", "Dumb and Dumber", "Shaun of the Dead"),
                "DRAMA", List.of("The Shawshank Redemption", "Forrest Gump", "Pulp Fiction", "Fight Club", "The Godfather"),
                "ROMANCE", List.of("The Notebook", "Titanic", "Casablanca", "Pride and Prejudice", "Before Sunset"),
                "HORROR", List.of("The Conjuring", "Hereditary", "Get Out", "A Quiet Place", "The Exorcist"),
                "THRILLER", List.of("Se7en", "Gone Girl", "Zodiac", "Shutter Island", "No Country for Old Men"),
                "SCI-FI", List.of("Blade Runner", "The Matrix", "Interstellar", "Star Wars", "Arrival")
                // ... (More genres)
        );

        List<CompletableFuture<GenreMovies>> futures = genres.stream()
                .filter(genreTitlesMap::containsKey)
                .map(genre -> {
                    List<String> titles = genreTitlesMap.get(genre);

                    return CompletableFuture.supplyAsync(() -> {
                        List<Movie> genreMovies = new ArrayList<>();
                        for (String title : titles) {
                            try {
                                Movie movie = searchMovie(title);
                                if (movie != null && movie.getPosterUrl() != null) {
                                    genreMovies.add(movie);
                                }
                            } catch (Exception e) {
                                System.err.println("Error fetching movie: " + title);
                            }
                        }
                        return new GenreMovies(genre, genreMovies);
                    }, taskExecutor);
                })
        .collect(Collectors.toList());

        return futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }

    private Movie mapResponseToMovie(MovieResponse response) {
        if (response == null || !response.getResponse().equals("True")) {
            return null;
        }
        return new Movie(
                response.getImdbID(),
                response.getTitle(),
                response.getPoster()
        );
    }

}
