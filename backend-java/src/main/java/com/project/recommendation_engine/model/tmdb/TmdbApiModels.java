package com.project.recommendation_engine.model.tmdb;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Raw response shapes returned by the TMDB API itself.
 * Everything here exists purely to deserialize TMDB JSON payloads.
 * Nothing in this class is exposed to consumers of this service -
 * that's what TMDBResponse and Movie are for (see TMDBMapper).
 */
public final class TmdbApiModels {

    private TmdbApiModels() {}

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

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MovieListResponse {
        @JsonProperty("results")
        private List<TmdbMovie> results;
        @JsonProperty("page")
        private Integer page;
        @JsonProperty("total_pages")
        private Integer totalPages;
        @JsonProperty("total_results")
        private Integer totalResults;

        public List<TmdbMovie> getResults() { return results; }
        public void setResults(List<TmdbMovie> results) { this.results = results; }
        public Integer getPage() { return page; }
        public void setPage(Integer page) { this.page = page; }
        public Integer getTotalPages() { return totalPages; }
        public void setTotalPages(Integer totalPages) { this.totalPages = totalPages; }
        public Integer getTotalResults() { return totalResults; }
        public void setTotalResults(Integer totalResults) { this.totalResults = totalResults; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FindResponse {
        @JsonProperty("movie_results")
        private List<TmdbMovie> movieResults;

        public List<TmdbMovie> getMovieResults() { return movieResults; }
        public void setMovieResults(List<TmdbMovie> movieResults) { this.movieResults = movieResults; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WatchProvidersResponse {
        @JsonProperty("results")
        private Map<String, RegionInfo> results;

        public Map<String, RegionInfo> getResults() { return results; }
        public void setResults(Map<String, RegionInfo> results) { this.results = results; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RegionInfo {
        @JsonProperty("link")
        private String link;
        @JsonProperty("flatrate")
        private List<ProviderItem> flatrate;
        @JsonProperty("rent")
        private List<ProviderItem> rent;
        @JsonProperty("buy")
        private List<ProviderItem> buy;

        public String getLink() { return link; }
        public List<ProviderItem> getFlatrate() { return flatrate; }
        public List<ProviderItem> getRent() { return rent; }
        public List<ProviderItem> getBuy() { return buy; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ProviderItem {
        @JsonProperty("provider_name")
        private String providerName;
        @JsonProperty("logo_path")
        private String logoPath;

        public String getProviderName() { return providerName; }
        public String getLogoPath() { return logoPath; }
    }
}