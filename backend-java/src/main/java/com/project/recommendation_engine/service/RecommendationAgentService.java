package com.project.recommendation_engine.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Service
public class RecommendationAgentService {

    @Value("${app.python.script-path:./python_agent/batch_processor.py}")
    private String SCRIPT_PATH;

    @Value("${app.python.command:python3}")
    private String pythonCommand;

    //private final String PYTHON_CMD = "python3";

    @Async("taskExecutor") // Thread's pool defined on AsyncConfig
    public void triggerRecommendationForUser(String userId) {
        long startTime = System.currentTimeMillis();
        System.out.println("[Async] Running Python Agent for User: " + userId);

        try {
            // Build command: python batch_processor.py --user_id XXXXX
            ProcessBuilder processBuilder = new ProcessBuilder(
                    pythonCommand,
                    SCRIPT_PATH,
                    "--user_id",
                    userId
            );

            // See messages on Java Console
            processBuilder.redirectErrorStream(true);

            // Initiate process
            Process process = processBuilder.start();

            // Read what Python prints for debugging from Java
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                // TITLE to distinguish from Spring messages logs
                System.out.println("   [Python Agent]: " + line);
            }

            // Wait till it finishes
            int exitCode = process.waitFor();
            long duration = System.currentTimeMillis() - startTime;

            if (exitCode == 0) {
                System.out.println("[Async] Agent ended successfully in " + duration + "ms");
            } else {
                System.err.println("[Async] Agent fail with end code: " + exitCode);
            }

        } catch (Exception e) {
            System.err.println("[Async] CRITICAL FAIL RUNNING SCRIPT: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // For all users every two weeks (Sundays at 1:00 AM)
    public void runFullBatchProcess() {
        long startTime = System.currentTimeMillis();
        System.out.println("[Scheduler] initializing Batch for All Users...");

        try {
            // Build command without argument: --user_id
            ProcessBuilder processBuilder = new ProcessBuilder(
                    pythonCommand,
                    SCRIPT_PATH
            );

            // processBuilder.directory(new java.io.File("D:\\OneDrive\\Escritorio\\UNIVERSITY\\FALL2025\\Senior Project\\movie-agent"));

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