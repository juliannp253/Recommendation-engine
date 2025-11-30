package com.project.recommendation_engine.model;

public class RatingRequest {
    private String movieId; // ID from TMDB
    private Double rating;

    // Getters and Setters
    public String getMovieId() { return movieId; }
    public void setMovieId(String movieId) { this.movieId = movieId; }
    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }
}