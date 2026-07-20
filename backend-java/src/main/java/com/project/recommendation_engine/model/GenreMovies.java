package com.project.recommendation_engine.model;

import java.util.List;

public class GenreMovies {

    private String genreName;
    private List<Movie> movies;

    // Constructor, Getters y Setters
    public GenreMovies(String genreName, List<Movie> movies) {
        this.genreName = genreName;
        this.movies = movies;
    }

    public String getGenreName() {
        return genreName;
    }

    public void setGenreName(String genreName) {
        this.genreName = genreName;
    }

    public List<Movie> getMovies() {
        return movies;
    }

    public void setMovies(List<Movie> movies) {
        this.movies = movies;
    }
}