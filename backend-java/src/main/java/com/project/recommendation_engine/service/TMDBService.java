package com.project.recommendation_engine.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.project.recommendation_engine.model.GenreMovies;
import com.project.recommendation_engine.model.Movie;
import com.project.recommendation_engine.model.TMDBResponse;
import com.project.recommendation_engine.model.tmdb.TmdbApiModels.RegionInfo;
import com.project.recommendation_engine.model.tmdb.TmdbApiModels.TmdbMovie;

@Service
public class TMDBService {

    private static final Map<String, Integer> GENRE_ID_MAP = Map.of(
            "ACTION", 28,
            "COMEDY", 35,
            "DRAMA", 18,
            "ROMANCE", 10749,
            "HORROR", 27,
            "THRILLER", 53,
            "ADVENTURE", 12,
            "SCI-FI", 878
    );

    private static final String DEFAULT_REGION = "US";
    private static final int MOVIES_PER_GENRE = 10;
    private static final int MAX_DISCOVER_PAGE = 5;
    private static final int TRENDING_MOVIES_LIMIT = 10;

    private final TMDBApiClient apiClient;
    private final TMDBMapper mapper;
    private final Executor taskExecutor;

    public TMDBService(TMDBApiClient apiClient, TMDBMapper mapper, Executor taskExecutor) {
        this.apiClient = apiClient;
        this.mapper = mapper;
        this.taskExecutor = taskExecutor;
    }

    @Cacheable(value = "movieDetails", key = "#titleOrId")
    public TMDBResponse fetchRawMovieResponse(String titleOrId) {
        Long tmdbId = resolveTmdbId(titleOrId);
        if (tmdbId == null) {
            return null;
        }
        return apiClient.getMovieDetails(tmdbId)
                .map(this::toEnrichedResponse)
                .orElse(null);
    }

    public Movie searchMovie(String title) {
        List<TmdbMovie> results = apiClient.searchMoviesByTitle(title);
        return results.isEmpty() ? null : mapper.toMovie(results.get(0));
    }

    public List<GenreMovies> fetchMoviesForGenres(List<String> genres) {
        List<CompletableFuture<GenreMovies>> futures = genres.stream()
                .filter(GENRE_ID_MAP::containsKey)
                .map(genre -> CompletableFuture.supplyAsync(
                        () -> new GenreMovies(genre, fetchMoviesByGenre(GENRE_ID_MAP.get(genre))),
                        taskExecutor))
                .collect(Collectors.toList());

        return futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "trendingMovies", key = "'daily_trending'")
    public List<TMDBResponse> fetchTrendingMovies() {
        return apiClient.getPopularMovies(1).stream()
                .limit(TRENDING_MOVIES_LIMIT)
                .map(movie -> apiClient.getMovieDetails(movie.getId()).orElse(null))
                .filter(Objects::nonNull)
                .map(this::toEnrichedResponse)
                .collect(Collectors.toList());
    }

    private Long resolveTmdbId(String titleOrId) {
        if (titleOrId.startsWith("tt")) {
            return apiClient.findTmdbIdByImdbId(titleOrId).orElse(null);
        }
        try {
            return Long.parseLong(titleOrId);
        } catch (NumberFormatException e) {
            List<TmdbMovie> results = apiClient.searchMoviesByTitle(titleOrId);
            return results.isEmpty() ? null : results.get(0).getId();
        }
    }

    private TMDBResponse toEnrichedResponse(TmdbMovie movie) {
        RegionInfo watchProviders = apiClient.getWatchProviders(movie.getId(), DEFAULT_REGION).orElse(null);
        return mapper.toResponse(movie, watchProviders);
    }

    private List<Movie> fetchMoviesByGenre(Integer genreId) {
        int randomPage = (int) (Math.random() * MAX_DISCOVER_PAGE) + 1;

        List<Movie> movies = apiClient.discoverMoviesByGenre(genreId, randomPage).stream()
                .filter(movie -> movie.getPosterPath() != null)
                .map(mapper::toMovie)
                .collect(Collectors.toList());

        Collections.shuffle(movies);

        return movies.stream().limit(MOVIES_PER_GENRE).collect(Collectors.toList());
    }
}
