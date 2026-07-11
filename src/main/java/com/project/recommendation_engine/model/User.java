package com.project.recommendation_engine.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "users")
public class User {
    @Id
    private String id;

    private String username;
    private String email;
    @Field("password")
    private String password;
    private List<String> favoriteGenres = new ArrayList<>();
    private LocalDateTime createdAt;

    // Rating List
    private List<Rating> movieRatings = new ArrayList<>();

    // Empty contructor needed
    public User(){}

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<Rating> getMovieRatings() {
        return movieRatings;
    }

    public void setMovieRatings(List<Rating> movieRatings) {
        this.movieRatings = movieRatings;
    }

    public List<String> getFavoriteGenres() {
        return favoriteGenres;
    }

    public void setFavoriteGenres(List<String> favoriteGenres) {
        this.favoriteGenres = favoriteGenres;
    }

    protected void onCreate(){
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
