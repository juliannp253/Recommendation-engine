package com.project.recommendation_engine.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.project.recommendation_engine.model.tmdb.TmdbApiModels.FindResponse;
import com.project.recommendation_engine.model.tmdb.TmdbApiModels.MovieListResponse;
import com.project.recommendation_engine.model.tmdb.TmdbApiModels.RegionInfo;
import com.project.recommendation_engine.model.tmdb.TmdbApiModels.TmdbMovie;
import com.project.recommendation_engine.model.tmdb.TmdbApiModels.WatchProvidersResponse;

@Component
public class TMDBApiClient {

    private static final Logger log = LoggerFactory.getLogger(TMDBApiClient.class);
    private static final String MIN_VOTE_COUNT = "3000";

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String apiKey;

    public TMDBApiClient(RestTemplate restTemplate,
                         @Value("${tmdb.api.baseurl}") String baseUrl,
                         @Value("${tmdb.api.key}") String apiKey) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
    }

    public Optional<TmdbMovie> getMovieDetails(Long tmdbId) {
        String url = urlFor("/movie/" + tmdbId)
                .queryParam("append_to_response", "credits")
                .toUriString();
        try {
            return Optional.ofNullable(restTemplate.getForObject(url, TmdbMovie.class));
        } catch (RestClientException e) {
            log.error("Error fetching movie details for id {}: {}", tmdbId, e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<Long> findTmdbIdByImdbId(String imdbId) {
        String url = urlFor("/find/" + imdbId)
                .queryParam("external_source", "imdb_id")
                .toUriString();
        try {
            FindResponse response = restTemplate.getForObject(url, FindResponse.class);
            if (response != null && response.getMovieResults() != null && !response.getMovieResults().isEmpty()) {
                return Optional.ofNullable(response.getMovieResults().get(0).getId());
            }
        } catch (RestClientException e) {
            log.error("Error searching by IMDb id {}: {}", imdbId, e.getMessage());
        }
        return Optional.empty();
    }

    public List<TmdbMovie> searchMoviesByTitle(String title) {
        String url = urlFor("/search/movie")
                .queryParam("query", title)
                .toUriString();
        return getResultsOrEmpty(url, "searching movies by title '" + title + "'");
    }

    public List<TmdbMovie> discoverMoviesByGenre(int genreId, int page) {
        String url = urlFor("/discover/movie")
                .queryParam("with_genres", genreId)
                .queryParam("language", "en-US")
                .queryParam("sort_by", "vote_count.desc")
                .queryParam("vote_count.gte", MIN_VOTE_COUNT)
                .queryParam("page", page)
                .toUriString();
        return getResultsOrEmpty(url, "discovering movies for genre id " + genreId);
    }

    public List<TmdbMovie> getPopularMovies(int page) {
        String url = urlFor("/movie/popular")
                .queryParam("language", "en-US")
                .queryParam("page", page)
                .toUriString();
        return getResultsOrEmpty(url, "fetching popular movies");
    }

    public Optional<RegionInfo> getWatchProviders(Long tmdbId, String region) {
        String url = urlFor("/movie/" + tmdbId + "/watch/providers").toUriString();
        try {
            WatchProvidersResponse response = restTemplate.getForObject(url, WatchProvidersResponse.class);
            if (response != null && response.getResults() != null) {
                return Optional.ofNullable(response.getResults().get(region));
            }
        } catch (RestClientException e) {
            log.error("Error fetching watch providers for id {}: {}", tmdbId, e.getMessage());
        }
        return Optional.empty();
    }

    private List<TmdbMovie> getResultsOrEmpty(String url, String actionDescription) {
        try {
            MovieListResponse response = restTemplate.getForObject(url, MovieListResponse.class);
            return response != null && response.getResults() != null ? response.getResults() : Collections.emptyList();
        } catch (RestClientException e) {
            log.error("Error {}: {}", actionDescription, e.getMessage());
            return Collections.emptyList();
        }
    }

    private UriComponentsBuilder urlFor(String path) {
        return UriComponentsBuilder.fromHttpUrl(baseUrl + path)
                .queryParam("api_key", apiKey);
    }
}