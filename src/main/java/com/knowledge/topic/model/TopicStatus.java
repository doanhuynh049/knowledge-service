package com.knowledge.topic.model;

public enum TopicStatus {
    NEW,        // Topic has not been processed yet
    PROCESSING, // Topic is currently being processed
    DONE,       // Topic has been successfully processed
    ERROR,      // Topic processing failed
    ARCHIVED    // Topic has been archived/retired
}
