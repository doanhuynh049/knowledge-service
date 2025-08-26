package com.knowledge.topic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessingStatsDto {

    private long totalTopics;
    private long newTopics;
    private long processedTopics;
    private long errorTopics;
    private long archivedTopics;
    private long processingTopics;

    private long totalContentGenerated;
    private long pendingEmailContent;
    private long contentGeneratedToday;

    private Double averageOverviewWords;
    private Double averageDetailedWords;

    private LocalDateTime lastUpdated;

    // Computed properties
    public double getProcessingSuccessRate() {
        if (totalTopics == 0) return 0.0;
        return (double) processedTopics / totalTopics * 100;
    }

    public double getErrorRate() {
        if (totalTopics == 0) return 0.0;
        return (double) errorTopics / totalTopics * 100;
    }

    public String getSystemHealth() {
        double successRate = getProcessingSuccessRate();
        if (successRate >= 90) return "EXCELLENT";
        if (successRate >= 75) return "GOOD";
        if (successRate >= 50) return "FAIR";
        return "NEEDS_ATTENTION";
    }
}
