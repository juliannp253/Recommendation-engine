package com.project.recommendation_engine.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.util.List;

@Document(collection = "recommended_cache")
public class UserRecommendation {

    @Id
    private String id;

    @Field("user_id")
    private String userId;

    @Field("generated_at")
    private String generatedAt;
    // -------------------------------------

    private List<RecSection> sections;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(String generatedAt) { this.generatedAt = generatedAt; }

    public List<RecSection> getSections() { return sections; }
    public void setSections(List<RecSection> sections) { this.sections = sections; }

    // --- Inner Class: Section ---
    public static class RecSection {
        private String title;
        private String type;
        private List<RecMovie> movies;

        // Getters and Setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public List<RecMovie> getMovies() { return movies; }
        public void setMovies(List<RecMovie> movies) { this.movies = movies; }
    }

    // --- Inner Class: Movie ---
    public static class RecMovie {
        @Field("id")
        private Integer tmdbId;

        private String title;

        @Field("poster_path")
        private String posterPath;

        @Field("vote_average")
        private Double voteAverage;

        @Field("ai_reason")
        private String aiReason;

        // Getters and Setters
        public Integer getId() { return tmdbId; }
        public void setId(Integer tmdbId) { this.tmdbId = tmdbId; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getPosterPath() {
            if (posterPath != null && !posterPath.startsWith("http")) {
                return "https://image.tmdb.org/t/p/w500" + posterPath;
            }
            return posterPath;
        }
        public void setPosterPath(String posterPath) { this.posterPath = posterPath; }

        public String getAiReason() { return aiReason; }
        public void setAiReason(String aiReason) { this.aiReason = aiReason; }
    }
}