package com.project.recommendation_engine.repository;

import com.project.recommendation_engine.IntegrationTestBase;
import com.project.recommendation_engine.model.UserRecommendation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataMongoTest(properties = "spring.cache.type=none")
class RecommendationRepositoryIntegrationTest extends IntegrationTestBase {
    @Autowired
    private RecommendationRepository recommendationRepository;

    @BeforeEach
    void cleanRecommendations() {
        recommendationRepository.deleteAll();
    }

    @Test
    void findFirstByUserIdOrderByGeneratedAtDescReturnsLatestForRequestedUser() {
        UserRecommendation latest = recommendationRepository.save(recommendation("user-1", "2026-09-24T12:00:00"));
        recommendationRepository.save(recommendation("user-2", "2026-09-25T12:00:00"));
        recommendationRepository.save(recommendation("user-1", "2026-09-23T12:00:00"));

        assertEquals(latest.getId(), recommendationRepository
                .findFirstByUserIdOrderByGeneratedAtDesc("user-1").orElseThrow().getId());
        assertTrue(recommendationRepository.findFirstByUserIdOrderByGeneratedAtDesc("missing-user").isEmpty());
    }

    private UserRecommendation recommendation(String userId, String generatedAt) {
        UserRecommendation recommendation = new UserRecommendation();
        recommendation.setUserId(userId);
        recommendation.setGeneratedAt(generatedAt);
        return recommendation;
    }
}
