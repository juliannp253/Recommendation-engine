package com.project.recommendation_engine.repository;

import com.project.recommendation_engine.model.UserRecommendation;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RecommendationRepository extends MongoRepository<UserRecommendation, String> {

    @Cacheable(value = "userRecommendations", key = "#userId")
    Optional<UserRecommendation> findFirstByUserIdOrderByGeneratedAtDesc(String userId);
}