package com.project.recommendation_engine.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TMDBResponse implements Serializable {
    @JsonProperty("Title")
    private String title;
    @JsonProperty("Year")
    private String year;
    @JsonProperty("tmdbID")
    private String tmdbID;
    @JsonProperty("Genre")
    private String genre;
    @JsonProperty("tmdbRating")
    private String tmdbRating;
    @JsonProperty("Poster")
    private String poster;
    @JsonProperty("Plot")
    private String plot;
    @JsonProperty("Response")
    private String response;
    @JsonProperty("Director")
    private String director;
    @JsonProperty("Actors")
    private String actors;

    private List<Provider> flatrateProviders = new ArrayList<>();
    private List<Provider> rentProviders = new ArrayList<>();
    private List<Provider> buyProviders = new ArrayList<>();
    private String watchLink;

    // Getters and setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getYear() { return year; }
    public void setYear(String year) { this.year = year; }
    public String getTmdbID() { return tmdbID; }
    public void setTmdbID(String tmdbID) { this.tmdbID = tmdbID; }
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
    public String getTmdbRating() { return tmdbRating; }
    public void setTmdbRating(String tmdbRating) { this.tmdbRating = tmdbRating; }
    public String getPoster() { return poster; }
    public void setPoster(String poster) { this.poster = poster; }
    public String getPlot() { return plot; }
    public void setPlot(String plot) { this.plot = plot; }
    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }
    public String getDirector() { return director; }
    public void setDirector(String director) { this.director = director; }
    public String getActors() { return actors; }
    public void setActors(String actors) { this.actors = actors; }
    public List<Provider> getFlatrateProviders() { return flatrateProviders; }
    public void setFlatrateProviders(List<Provider> flatrateProviders) { this.flatrateProviders = flatrateProviders; }
    public List<Provider> getRentProviders() { return rentProviders; }
    public void setRentProviders(List<Provider> rentProviders) { this.rentProviders = rentProviders; }
    public List<Provider> getBuyProviders() { return buyProviders; }
    public void setBuyProviders(List<Provider> buyProviders) { this.buyProviders = buyProviders; }
    public String getWatchLink() { return watchLink; }
    public void setWatchLink(String watchLink) { this.watchLink = watchLink; }

    public static class Provider implements Serializable {
        private final String name;
        private final String logoUrl;

        public Provider(String name, String logoUrl) {
            this.name = name;
            this.logoUrl = logoUrl;
        }

        public String getName() { return name; }
        public String getLogoUrl() { return logoUrl; }
    }
}