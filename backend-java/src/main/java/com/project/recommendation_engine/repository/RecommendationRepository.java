package com.project.recommendation_engine.repository;

import com.project.recommendation_engine.model.UserRecommendation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RecommendationRepository extends MongoRepository<UserRecommendation, String> {

    // Busca la recomendación más reciente para un usuario específico
    Optional<UserRecommendation> findFirstByUserIdOrderByGeneratedAtDesc(String userId);
}