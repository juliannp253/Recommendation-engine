package com.project.recommendation_engine.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link Movie}.
 */
class MovieTest {

    @Test
    @DisplayName("Default constructor – all fields should be null")
    void testDefaultConstructor() {
        Movie movie = new Movie();

        assertNull(movie.getId(),     "id");
        assertNull(movie.getTitle(),  "title");
        assertNull(movie.getPosterUrl(), "posterUrl");
        assertNull(movie.getYear(),   "year");
        assertNull(movie.getGenre(),  "genre");
        assertNull(movie.getImdbRating(), "imdbRating");
        assertNull(movie.getPlot(),   "plot");
    }

    @Test
    @DisplayName("Three‑arg constructor sets id, title and posterUrl")
    void testParameterizedConstructor() {
        Movie movie = new Movie("tt1234567", "The Matrix", "http://poster.com/film.jpg");

        assertEquals("tt1234567", movie.getId());
        assertEquals("The Matrix", movie.getTitle());
        assertEquals("http://poster.com/film.jpg", movie.getPosterUrl());

        // Remaining fields stay null
        assertNull(movie.getYear());
        assertNull(movie.getGenre());
        assertNull(movie.getImdbRating());
        assertNull(movie.getPlot());
    }

    @Nested
    @DisplayName("Getters and setters")
    class GettersSetters {

        @Test
        void testSetId() {
            Movie movie = new Movie();
            movie.setId("tt9876543");
            assertEquals("tt9876543", movie.getId());
        }

        @Test
        void testSetTitle() {
            Movie movie = new Movie();
            movie.setTitle("Inception");
            assertEquals("Inception", movie.getTitle());
        }

        @Test
        void testSetPosterUrl() {
            Movie movie = new Movie();
            movie.setPosterUrl("http://poster.com/inception.jpg");
            assertEquals("http://poster.com/inception.jpg", movie.getPosterUrl());
        }

        @Test
        void testSetYear() {
            Movie movie = new Movie();
            movie.setYear("2010");
            assertEquals("2010", movie.getYear());
        }

        @Test
        void testSetGenre() {
            Movie movie = new Movie();
            movie.setGenre("Action");
            assertEquals("Action", movie.getGenre());
        }

        @Test
        void testSetImdbRating() {
            Movie movie = new Movie();
            movie.setImdbRating("8.8");
            assertEquals("8.8", movie.getImdbRating());
        }

        @Test
        void testSetPlot() {
            Movie movie = new Movie();
            movie.setPlot("A mind‑bending thriller.");
            assertEquals("A mind‑bending thriller.", movie.getPlot());
        }
    }
}