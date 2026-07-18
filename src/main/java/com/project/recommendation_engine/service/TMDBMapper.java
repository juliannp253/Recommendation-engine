package com.project.recommendation_engine.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.project.recommendation_engine.model.Movie;
import com.project.recommendation_engine.model.TMDBResponse;
import com.project.recommendation_engine.model.tmdb.TmdbApiModels.Cast;
import com.project.recommendation_engine.model.tmdb.TmdbApiModels.Crew;
import com.project.recommendation_engine.model.tmdb.TmdbApiModels.Genre;
import com.project.recommendation_engine.model.tmdb.TmdbApiModels.ProviderItem;
import com.project.recommendation_engine.model.tmdb.TmdbApiModels.RegionInfo;
import com.project.recommendation_engine.model.tmdb.TmdbApiModels.TmdbMovie;

@Component
public class TMDBMapper {

    private static final String POSTER_BASE_URL = "https://image.tmdb.org/t/p/w500";
    private static final String LOGO_BASE_URL = "https://image.tmdb.org/t/p/original";
    private static final int MAX_BILLED_ACTORS = 5;

    public TMDBResponse toResponse(TmdbMovie movie, RegionInfo watchProviders) {
        if (movie == null) {
            return null;
        }

        TMDBResponse response = new TMDBResponse();
        response.setTitle(movie.getTitle());
        response.setTmdbID(String.valueOf(movie.getId()));
        response.setPoster(buildPosterUrl(movie.getPosterPath()));
        response.setPlot(movie.getOverview());
        response.setYear(extractYear(movie.getReleaseDate()));
        response.setTmdbRating(formatRating(movie.getVoteAverage()));
        response.setResponse("True");
        response.setGenre(joinGenres(movie));
        response.setDirector(joinDirectors(movie));
        response.setActors(joinTopActors(movie));

        if (watchProviders != null) {
            response.setWatchLink(watchProviders.getLink());
            response.setFlatrateProviders(toProviders(watchProviders.getFlatrate()));
            response.setRentProviders(toProviders(watchProviders.getRent()));
            response.setBuyProviders(toProviders(watchProviders.getBuy()));
        }

        return response;
    }

    public Movie toMovie(TmdbMovie movie) {
        if (movie == null) {
            return null;
        }
        return new Movie(String.valueOf(movie.getId()), movie.getTitle(), buildPosterUrl(movie.getPosterPath()));
    }

    private String buildPosterUrl(String posterPath) {
        return posterPath != null ? POSTER_BASE_URL + posterPath : null;
    }

    private String extractYear(String releaseDate) {
        return releaseDate != null && releaseDate.length() >= 4 ? releaseDate.substring(0, 4) : null;
    }

    private String formatRating(Double voteAverage) {
        return voteAverage != null ? String.format("%.1f", voteAverage) : "N/A";
    }

    private String joinGenres(TmdbMovie movie) {
        if (movie.getGenres() == null || movie.getGenres().isEmpty()) {
            return "N/A";
        }
        return movie.getGenres().stream()
                .map(Genre::getName)
                .collect(Collectors.joining(", "));
    }

    private String joinDirectors(TmdbMovie movie) {
        if (movie.getCredits() == null || movie.getCredits().getCrew() == null) {
            return "N/A";
        }
        String directors = movie.getCredits().getCrew().stream()
                .filter(crew -> "Director".equals(crew.getJob()))
                .map(Crew::getName)
                .collect(Collectors.joining(", "));
        return directors.isEmpty() ? "N/A" : directors;
    }

    private String joinTopActors(TmdbMovie movie) {
        if (movie.getCredits() == null || movie.getCredits().getCast() == null) {
            return "N/A";
        }
        String actors = movie.getCredits().getCast().stream()
                .filter(cast -> cast.getOrder() != null && cast.getOrder() < MAX_BILLED_ACTORS)
                .sorted((a, b) -> Integer.compare(a.getOrder(), b.getOrder()))
                .map(Cast::getName)
                .collect(Collectors.joining(", "));
        return actors.isEmpty() ? "N/A" : actors;
    }

    private List<TMDBResponse.Provider> toProviders(List<ProviderItem> items) {
        if (items == null) {
            return new ArrayList<>();
        }
        return items.stream()
                .map(item -> new TMDBResponse.Provider(item.getProviderName(), LOGO_BASE_URL + item.getLogoPath()))
                .collect(Collectors.toList());
    }
}