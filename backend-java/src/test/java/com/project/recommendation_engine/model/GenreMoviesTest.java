package com.project.recommendation_engine.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link GenreMovies} model.
 */
public class GenreMoviesTest {

    /**
     * Verify that the constructor correctly assigns {@code genreName}
     * and {@code movies}, and that the getters return those values.
     */
    @Test
    public void testConstructorAndGetters() {
        // Prepare some dummy movies
        List<Movie> movies = new ArrayList<>();
        movies.add(new Movie("tt1234567", "The Matrix", "http://example.com/matrix.jpg"));
        movies.add(new Movie("tt9876543", "Blade Runner", "http://example.com/bladerunner.jpg"));

        // Create the object under test
        GenreMovies genreMovies = new GenreMovies("Sci-Fi", movies);

        // Assertions
        assertEquals("Sci-Fi", genreMovies.getGenreName(), "Genre name should be set by constructor");
        assertSame(movies, genreMovies.getMovies(), "Movies list should be the same instance passed to constructor");
    }

    /**
     * Verify that the setters correctly update the internal state.
     */
    @Test
    public void testSetters() {
        // Start with a simple instance
        GenreMovies genreMovies = new GenreMovies("Action", new ArrayList<>());

        // Change the genre name
        genreMovies.setGenreName("Thriller");
        assertEquals("Thriller", genreMovies.getGenreName(), "setGenreName should update the field");

        // Replace the movies list
        List<Movie> newMovies = new ArrayList<>();
        newMovies.add(new Movie("tt1111111", "Die Hard", "http://example.com/diehard.jpg"));
        genreMovies.setMovies(newMovies);
        assertSame(newMovies, genreMovies.getMovies(), "setMovies should replace the list reference");
    }
}