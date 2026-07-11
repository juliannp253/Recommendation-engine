package com.project.recommendation_engine.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

// This class is used to represent the data acquired from the json
// as a java object. 
public class TMDBResponse {
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

    // NEW FIELD FOR STREAMING
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

    // NEW GETTERS AND SETTERS
    public List<Provider> getFlatrateProviders() { return flatrateProviders; }
    public void setFlatrateProviders(List<Provider> flatrateProviders) { this.flatrateProviders = flatrateProviders; }

    public List<Provider> getRentProviders() { return rentProviders; }
    public void setRentProviders(List<Provider> rentProviders) { this.rentProviders = rentProviders; }

    public List<Provider> getBuyProviders() { return buyProviders; }
    public void setBuyProviders(List<Provider> buyProviders) { this.buyProviders = buyProviders; }

    public String getWatchLink() { return watchLink; }
    public void setWatchLink(String watchLink) { this.watchLink = watchLink; }

    // NEW CLASS
    public static class Provider {
        private String name;
        private String logoUrl;

        public Provider(String name, String logoUrl) {
            this.name = name;
            this.logoUrl = logoUrl;
        }

        public String getName() { return name; }
        public String getLogoUrl() { return logoUrl; }
    }

    // Static nested class for TMDB movie data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TmdbMovie {
        @JsonProperty("id")
        private Long id;
        @JsonProperty("title")
        private String title;
        @JsonProperty("poster_path")
        private String posterPath;
        @JsonProperty("overview")
        private String overview;
        @JsonProperty("release_date")
        private String releaseDate;
        @JsonProperty("vote_average")
        private Double voteAverage;
        @JsonProperty("credits")
        private Credits credits;
        @JsonProperty("genres")
        private List<Genre> genres;

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getPosterPath() { return posterPath; }
        public void setPosterPath(String posterPath) { this.posterPath = posterPath; }
        public String getOverview() { return overview; }
        public void setOverview(String overview) { this.overview = overview; }
        public String getReleaseDate() { return releaseDate; }
        public void setReleaseDate(String releaseDate) { this.releaseDate = releaseDate; }
        public Double getVoteAverage() { return voteAverage; }
        public void setVoteAverage(Double voteAverage) { this.voteAverage = voteAverage; }
        public Credits getCredits() { return credits; }
        public void setCredits(Credits credits) { this.credits = credits; }
        public List<Genre> getGenres() { return genres; }
        public void setGenres(List<Genre> genres) { this.genres = genres; }
    }

    // Genre class
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Genre {
        @JsonProperty("id")
        private Integer id;
        @JsonProperty("name")
        private String name;

        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    // Credits class to hold cast and crew
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Credits {
        @JsonProperty("cast")
        private List<Cast> cast;
        @JsonProperty("crew")
        private List<Crew> crew;

        public List<Cast> getCast() { return cast; }
        public void setCast(List<Cast> cast) { this.cast = cast; }
        public List<Crew> getCrew() { return crew; }
        public void setCrew(List<Crew> crew) { this.crew = crew; }
    }

    // Cast member class
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Cast {
        @JsonProperty("name")
        private String name;
        @JsonProperty("character")
        private String character;
        @JsonProperty("order")
        private Integer order;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getCharacter() { return character; }
        public void setCharacter(String character) { this.character = character; }
        public Integer getOrder() { return order; }
        public void setOrder(Integer order) { this.order = order; }
    }

    // Crew member class
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Crew {
        @JsonProperty("name")
        private String name;
        @JsonProperty("job")
        private String job;
        @JsonProperty("department")
        private String department;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getJob() { return job; }
        public void setJob(String job) { this.job = job; }
        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }
    }


}