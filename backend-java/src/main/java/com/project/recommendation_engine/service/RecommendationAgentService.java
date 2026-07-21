package com.project.recommendation_engine.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Service
public class RecommendationAgentService {
    private final CacheManager cacheManager;

    public RecommendationAgentService(CacheManager cacheManager){
        this.cacheManager = cacheManager;
    }

    @Value("${app.python.script-path:./python_agent/batch_processor.py}")
    private String SCRIPT_PATH;

    @Value("${app.python.command:python3}")
    private String pythonCommand;

    @Async("taskExecutor")
    public void triggerRecommendationForUser(String userId) {
        long startTime = System.currentTimeMillis();
        System.out.println("[Async] Running Python Agent for User: " + userId);

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                    pythonCommand,
                    SCRIPT_PATH,
                    "--user_id",
                    userId
            );

            // See messages on Java Console
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("   [Python Agent]: " + line);
            }

            int exitCode = process.waitFor();
            long duration = System.currentTimeMillis() - startTime;

            if (exitCode == 0) {
                System.out.println("[Async] Agent ended successfully in " + duration + "ms");

                var cache = cacheManager.getCache("userRecommendations");
                if (cache != null) {
                    cache.evict(userId);
                    System.out.println("[Cache] Recommendations deleted on Redis for user: " + userId);
                }
            } else {
                System.err.println("[Async] Agent fail with end code: " + exitCode);
            }

        } catch (Exception e) {
            System.err.println("[Async] CRITICAL FAIL RUNNING SCRIPT: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void runFullBatchProcess() {
        long startTime = System.currentTimeMillis();
        System.out.println("[Scheduler] initializing Batch for All Users...");

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                    pythonCommand,
                    SCRIPT_PATH
            );

            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[Batch Agent]: " + line);
            }

            int exitCode = process.waitFor();
            long duration = System.currentTimeMillis() - startTime;

            if (exitCode == 0) {
                System.out.println("[Scheduler] Batch ended in " + duration + "ms");

                var cache = cacheManager.getCache("userRecommendations");
                if (cache != null) {
                    cache.clear();
                    System.out.println("[Cache] All cache from recommendations on Redis cleaned (Batch).");
                }
            } else {
                System.err.println("[Scheduler] Batch failed with code: " + exitCode);
            }

        } catch (Exception e) {
            System.err.println("[Scheduler] Fatal Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean runAgentForUserSync(String userId) {
        System.out.println("[Demo] Running Synchronous Python Agent for User: " + userId);

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                    pythonCommand,
                    SCRIPT_PATH,
                    "--user_id",
                    userId
            );

            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("   [Python Demo]: " + line);
            }

            int exitCode = process.waitFor();

            return exitCode == 0;

        } catch (Exception e) {
            System.err.println("[Demo] Error running script: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}