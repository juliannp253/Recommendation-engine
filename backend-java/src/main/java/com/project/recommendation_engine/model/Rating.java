package com.project.recommendation_engine.model;

import java.time.LocalDateTime;

public class Rating {
    private String movieId;
    private double score;
    private LocalDateTime ratedAt;

    public Rating() {}

    public Rating(String movieId, double score){
        this.movieId = movieId;
        this.score = score;
        this.ratedAt = LocalDateTime.now();
    }

    // Getters and Setters

    public String getMovieId() {
        return movieId;
    }

    public void setMovieId(String movieId) {
        this.movieId = movieId;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public LocalDateTime getRatedAt() {
        return ratedAt;
    }

    public void setRatedAt(LocalDateTime ratedAt) {
        this.ratedAt = ratedAt;
    }
}
