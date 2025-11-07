package com.project.recommendation_engine.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.recommendation_engine.model.GenreMovies;
import com.project.recommendation_engine.model.Movie;
import com.project.recommendation_engine.model.TMDBResponse;



// This service will call the TMDB Api and return the information
// as a TMDBResponse object. 
@Service
public class TMDBService {
    @Value("${tmdb.api.key}")
    private String apiKey;

    @Value("${tmdb.api.baseurl}")
    private String baseUrl;

    private final Executor taskExecutor;

    public TMDBService(@Value("${tmdb.api.key}") String apiKey, 
                      @Value("${tmdb.api.baseurl}") String baseUrl, 
                      Executor taskExecutor) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.taskExecutor = taskExecutor;
    }

    // TMDB Response class - used for both search and discover endpoints
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class TmdbMovieListResponse {
        @JsonProperty("results")
        private List<TMDBResponse.TmdbMovie> results;

        @JsonProperty("page")
        private Integer page;

        @JsonProperty("total_pages") 
        private Integer totalPages;

        @JsonProperty("total_results")
        private Integer totalResults;

        public List<TMDBResponse.TmdbMovie> getResults() { return results; }
        public void setResults(List<TMDBResponse.TmdbMovie> results) { this.results = results; }
        public Integer getPage() { return page; }
        public void setPage(Integer page) { this.page = page; }
        public Integer getTotalPages() { return totalPages; }
        public void setTotalPages(Integer totalPages) { this.totalPages = totalPages; }
        public Integer getTotalResults() { return totalResults; }
        public void setTotalResults(Integer totalResults) { this.totalResults = totalResults; }
    }

    public TMDBResponse fetchRawMovieResponse(String titleOrId) {
        RestTemplate restTemplate = new RestTemplate();
        String url;
        
        try {
            // Check if it's a numeric ID or a title
            Long movieId = Long.parseLong(titleOrId);
            // Search by TMDB ID
            url = String.format("%s/movie/%d?api_key=%s", baseUrl, movieId, apiKey);
            TMDBResponse.TmdbMovie tmdbMovie = restTemplate.getForObject(url, TMDBResponse.TmdbMovie.class);
            return mapTmdbToMovieResponse(tmdbMovie);
        } catch (NumberFormatException e) {
            // Search by title
            url = String.format("%s/search/movie?api_key=%s&query=%s", 
                               baseUrl, apiKey, titleOrId.replace(" ", "%20"));
            TmdbMovieListResponse searchResponse = restTemplate.getForObject(url, TmdbMovieListResponse.class);
            
            if (searchResponse != null && searchResponse.getResults() != null && !searchResponse.getResults().isEmpty()) {
                return mapTmdbToMovieResponse(searchResponse.getResults().get(0));
            }
        }
        
        return null;
    }

    public Movie searchMovie(String title) {
        RestTemplate restTemplate = new RestTemplate();
        String url = String.format("%s/search/movie?api_key=%s&query=%s", 
                                  baseUrl, apiKey, title.replace(" ", "%20"));

        TmdbMovieListResponse response = restTemplate.getForObject(url, TmdbMovieListResponse.class);

        if (response != null && response.getResults() != null && !response.getResults().isEmpty()) {
            return mapTmdbMovieToMovie(response.getResults().get(0));
        }
        
        return null;
    }

    public List<GenreMovies> fetchMoviesForGenres(List<String> genres) {
        // Map genre names to TMDB genre IDs
        Map<String, Integer> genreIdMap = Map.of(
                "ACTION", 28,
                "COMEDY", 35,
                "DRAMA", 18,
                "ROMANCE", 10749,
                "HORROR", 27,
                "THRILLER", 53,
                "ADVENTURE", 12,
                "SCI-FI", 878
        );

        List<CompletableFuture<GenreMovies>> futures = genres.stream()
                .filter(genreIdMap::containsKey)
                .map(genre -> {
                    Integer genreId = genreIdMap.get(genre);

                    return CompletableFuture.supplyAsync(() -> {
                        List<Movie> genreMovies = fetchMoviesByGenre(genreId);
                        return new GenreMovies(genre, genreMovies);
                    }, taskExecutor);
                })
        .collect(Collectors.toList());

        return futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }

    private List<Movie> fetchMoviesByGenre(Integer genreId) {
        RestTemplate restTemplate = new RestTemplate();
        
        // Generate random page number (1-10 for variety)
        int randomPage = (int) (Math.random() * 10) + 1;
        
        String url = String.format("%s/discover/movie?api_key=%s&with_genres=%d&sort_by=popularity.desc&page=%d", 
                                  baseUrl, apiKey, genreId, randomPage);

        try {
            TmdbMovieListResponse response = restTemplate.getForObject(url, TmdbMovieListResponse.class);
            
            if (response != null && response.getResults() != null) {
                return response.getResults().stream()
                    .filter(movie -> movie.getPosterPath() != null) // Only include movies with posters
                    .limit(10) // Limit to 10 movies per genre
                    .map(this::mapTmdbMovieToMovie)
                    .collect(Collectors.toList());
            }
        } catch (Exception e) {
            System.err.println("Error fetching movies for genre ID: " + genreId + " - " + e.getMessage());
        }
        
        return new ArrayList<>();
    }

    private TMDBResponse mapTmdbToMovieResponse(TMDBResponse.TmdbMovie tmdbMovie) {
        if (tmdbMovie == null) {
            return null;
        }
        
        TMDBResponse response = new TMDBResponse();
        response.setTitle(tmdbMovie.getTitle());
        response.setImdbID(String.valueOf(tmdbMovie.getId()));
        response.setPoster(tmdbMovie.getPosterPath() != null ? 
            "https://image.tmdb.org/t/p/w500" + tmdbMovie.getPosterPath() : null);
        response.setPlot(tmdbMovie.getOverview());
        response.setYear(tmdbMovie.getReleaseDate() != null && tmdbMovie.getReleaseDate().length() >= 4 ? 
            tmdbMovie.getReleaseDate().substring(0, 4) : null);
        response.setResponse("True");
        
        return response;
    }

    private Movie mapTmdbMovieToMovie(TMDBResponse.TmdbMovie tmdbMovie) {
        if (tmdbMovie == null) {
            return null;
        }
        
        return new Movie(
                String.valueOf(tmdbMovie.getId()),
                tmdbMovie.getTitle(),
                tmdbMovie.getPosterPath() != null ? 
                    "https://image.tmdb.org/t/p/w500" + tmdbMovie.getPosterPath() : null
        );
    }

    public List<TMDBResponse> fetchTrendingMovies() {
        String url = String.format("%s/movie/popular?api_key=%s&language=en-US&page=1",
                baseUrl, apiKey);
        RestTemplate restTemplate = new RestTemplate();

        try {
            TmdbMovieListResponse response = restTemplate.getForObject(url, TmdbMovieListResponse.class);

            if (response != null && response.getResults() != null) {
                return response.getResults().stream()
                        // Top 10 movies
                        .limit(10)
                        .map(this::mapTmdbToMovieResponse)
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            System.err.println("Error fetching trending movies: " + e.getMessage());
        }

        // Return an empty List if an error
        return new ArrayList<>();
    }
}
