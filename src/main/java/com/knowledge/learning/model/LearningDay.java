package com.knowledge.learning.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LearningDay {
    private int day;
    private String week;
    private String phase;
    private String algorithmTask;
    private String theoryTask;
    private String codingTask;
    private String reflectionTask;
    private String status;
    private String notes;
    private String lastProcessed;
    private String completedDate;
    private String emailSubject;
    
    /**
     * Get combined learning goals for the day
     */
    public String getLearningGoal() {
        return String.format("Day %d Learning Goals: Algorithms(%s), Theory(%s), Coding(%s), Reflection(%s)", 
               day, algorithmTask, theoryTask, codingTask, reflectionTask);
    }
    
    /**
     * Get formatted email subject
     */
    public String getEmailSubject() {
        if (emailSubject != null && !emailSubject.isEmpty()) {
            return emailSubject;
        }
        return String.format("🚀 Day %d Learning Path - %s", day, phase);
    }
    
    /**
     * Get combined task details
     */
    public String getCombinedTasks() {
        StringBuilder tasks = new StringBuilder();
        tasks.append("**Algorithm Practice:** ").append(algorithmTask).append("\n\n");
        tasks.append("**Theory Study:** ").append(theoryTask).append("\n\n");
        tasks.append("**Hands-on Coding:** ").append(codingTask).append("\n\n");
        tasks.append("**Reflection & Notes:** ").append(reflectionTask);
        return tasks.toString();
    }
}
