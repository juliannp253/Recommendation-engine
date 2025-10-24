package com.project.recommendation_engine.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link Rating}.
 */
class RatingTest {

    @Test
    @DisplayName("Default constructor should initialise fields to null/0")
    void defaultConstructorInitialisesFields() {
        Rating rating = new Rating();

        assertNull(rating.getMovieId(), "movieId should be null");
        assertEquals(0.0, rating.getScore(), "score should be 0.0");
        assertNull(rating.getRatedAt(), "ratedAt should be null");
    }

    @Test
    @DisplayName("Parameterized constructor should set movieId, score and current time")
    void parameterisedConstructorSetsFields() {
        String movieId = "tt1234567";
        double score = 4.5;

        Rating rating = new Rating(movieId, score);

        assertEquals(movieId, rating.getMovieId(), "movieId mismatch");
        assertEquals(score, rating.getScore(), "score mismatch");

        LocalDateTime now = LocalDateTime.now();
        assertNotNull(rating.getRatedAt(), "ratedAt should not be null");

        // Ensure the timestamp is close to the current instant (within 1 second)
        Duration diff = Duration.between(rating.getRatedAt(), now);
        assertTrue(
                Math.abs(diff.toMillis()) < 1000,
                "ratedAt should be within 1 second of construction time"
        );
    }

    @Test
    @DisplayName("Getters and setters should work correctly")
    void gettersAndSetters() {
        Rating rating = new Rating();

        rating.setMovieId("tt9876543");
        rating.setScore(3.0);
        LocalDateTime customTime = LocalDateTime.of(2023, 1, 1, 12, 0);
        rating.setRatedAt(customTime);

        assertEquals("tt9876543", rating.getMovieId(), "movieId mismatch");
        assertEquals(3.0, rating.getScore(), "score mismatch");
        assertEquals(customTime, rating.getRatedAt(), "ratedAt mismatch");
    }
}