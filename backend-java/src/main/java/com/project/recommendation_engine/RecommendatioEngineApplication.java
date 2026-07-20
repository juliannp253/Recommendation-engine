package com.project.recommendation_engine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RecommendatioEngineApplication {

	public static void main(String[] args) {
		SpringApplication.run(RecommendatioEngineApplication.class, args);
	}

}
