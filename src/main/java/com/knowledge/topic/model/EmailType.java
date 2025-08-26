package com.knowledge.topic.model;

public enum EmailType {
    OVERVIEW("📋 Daily Topic Overview", "overview-email-template.html"),
    DETAILED("🔬 Deep Dive Knowledge", "detailed-email-template.html");

    private final String subject;
    private final String templateName;

    EmailType(String subject, String templateName) {
        this.subject = subject;
        this.templateName = templateName;
    }

    public String getSubject() {
        return subject;
    }

    public String getTemplateName() {
        return templateName;
    }
}
