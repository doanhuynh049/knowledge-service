package com.knowledge.stock.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockLearningDay {
    private int day;
    private String week;
    private String phase;
    private String topic;
    private String learningGoal;
    private String emailSubject;
    private String practiceTask;
    private String status;
    private String notes;
    private String lastProcessed;
    private String completedDate;
}
