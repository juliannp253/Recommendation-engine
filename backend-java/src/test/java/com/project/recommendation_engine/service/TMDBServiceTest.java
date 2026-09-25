package com.project.recommendation_engine.service;

import com.project.recommendation_engine.model.GenreMovies;
import com.project.recommendation_engine.model.Movie;
import com.project.recommendation_engine.model.TMDBResponse;
import com.project.recommendation_engine.model.tmdb.TmdbApiModels.TmdbMovie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TMDBServiceTest {

    @Mock
    private TMDBApiClient tmdbApiClient;

    @Mock
    private TMDBMapper tmdbMapper;

    private final Executor taskExecutor = Runnable::run;

    private TMDBService tmdbService;

    @BeforeEach
    void setUp() {
        tmdbService = new TMDBService(tmdbApiClient, tmdbMapper, taskExecutor);
    }

    @Test
    void fetchTrendingMovies_handlesEmptyList() {
        when(tmdbApiClient.getPopularMovies(1)).thenReturn(Collections.emptyList());

        List<TMDBResponse> result = tmdbService.fetchTrendingMovies();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(tmdbApiClient, times(1)).getPopularMovies(1);
        verifyNoInteractions(tmdbMapper);
    }

    @Test
    void searchMovie_returnsNullWhenNoResults() {
        when(tmdbApiClient.searchMoviesByTitle("Unknown Title")).thenReturn(Collections.emptyList());

        Movie result = tmdbService.searchMovie("Unknown Title");

        assertNull(result);
        verify(tmdbApiClient, times(1)).searchMoviesByTitle("Unknown Title");
        verifyNoInteractions(tmdbMapper);
    }

    @Test
    void fetchMoviesForGenres_filtersUnknownGenres() {
        TmdbMovie actionMovie = new TmdbMovie();
        actionMovie.setId(101L);
        actionMovie.setTitle("Die Hard");
        actionMovie.setPosterPath("/diehard.jpg");

        Movie mappedMovie = new Movie("101", "Die Hard", "https://image.tmdb.org/t/p/w500/diehard.jpg");

        when(tmdbApiClient.discoverMoviesByGenre(eq(28), anyInt())).thenReturn(List.of(actionMovie));
        when(tmdbMapper.toMovie(actionMovie)).thenReturn(mappedMovie);

        List<String> inputGenres = List.of("ACTION", "UNKNOWN_GENRE", "NON_EXISTING");

        List<GenreMovies> result = tmdbService.fetchMoviesForGenres(inputGenres);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("ACTION", result.get(0).getGenreName());
        assertEquals(1, result.get(0).getMovies().size());
        assertEquals("Die Hard", result.get(0).getMovies().get(0).getTitle());

        verify(tmdbApiClient, times(1)).discoverMoviesByGenre(eq(28), anyInt());
    }
}
