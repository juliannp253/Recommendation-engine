package com.project.recommendation_engine;

import com.fasterxml.jackson.annotation.JsonProperty;


// This class is used to represent the data aquired from the json
// as a java object. 
public class MovieResponse {
    @JsonProperty("Title")
    private String title;

    @JsonProperty("Year")
    private String year;

    @JsonProperty("imdbID")
    private String imdbID;

    @JsonProperty("Genre")
    private String genre;

    @JsonProperty("imdbRating")
    private String imdbRating;

    @JsonProperty("Poster")
    private String poster;

    @JsonProperty("Plot")
    private String plot;

    @JsonProperty("Response")
    private String response;

    // Getters and setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getYear() { return year; }
    public void setYear(String year) { this.year = year; }

    public String getImdbID() { return imdbID; }
    public void setImdbID(String imdbID) { this.imdbID = imdbID; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public String getImdbRating() { return imdbRating; }
    public void setImdbRating(String imdbRating) { this.imdbRating = imdbRating; }

    public String getPoster() { return poster; }
    public void setPoster(String poster) { this.poster = poster; }

    public String getPlot() { return plot; }
    public void setPlot(String plot) { this.plot = plot; }

    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }
}
