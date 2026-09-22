package com.project.recommendation_engine.config;

import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;

import java.time.Duration;

@Configuration
public class RedisConfig {

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
        return (builder) -> {
            RedisCacheConfiguration baseConfig = RedisCacheConfiguration.defaultCacheConfig()
                    .disableCachingNullValues();

            builder
                    .withCacheConfiguration("trendingMovies",
                            baseConfig.entryTtl(Duration.ofHours(12)))

                    .withCacheConfiguration("movieDetails",
                            baseConfig.entryTtl(Duration.ofDays(7)))

                    .withCacheConfiguration("userRecommendations",
                            baseConfig.entryTtl(Duration.ofHours(24)));
        };
    }
}
