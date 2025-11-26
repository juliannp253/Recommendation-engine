package com.project.recommendation_engine.scheduler;

import com.project.recommendation_engine.service.RecommendationAgentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.temporal.ChronoField;
import java.time.LocalDate;

@Component
public class RecommendationScheduler {

    @Autowired
    private RecommendationAgentService agentService;

    // CRON: Second Minutes Hours DayMonth Month WeekDay
    // "0 0 1 * * SUN" = ALL Sundays at 1:00 AM
    @Scheduled(cron = "0 0 1 * * SUN")
    public void scheduleBiWeeklyTask() {

        // Logic to execute only every 2 weeks (Even weeks of the year)
        int weekOfYear = LocalDate.now().get(ChronoField.ALIGNED_WEEK_OF_YEAR);

        if (weekOfYear % 2 == 0) {
            System.out.println("Is even week (" + weekOfYear + "). Running Recommendation Batch.");
            agentService.runFullBatchProcess();
        } else {
            System.out.println("zzz It is odd week (" + weekOfYear + "). Agent rests today.");
        }
    }

    // FOR TESTING IMMEDIATELY 10 SEC AFTER RUNNING APP
    /*
    @Scheduled(initialDelay = 10000, fixedDelay = 999999999)
    public void runTestOnStartup() {
        System.out.println(" INITIAL TEST: Running Batch...");
        agentService.runFullBatchProcess();
    }
    */

}
