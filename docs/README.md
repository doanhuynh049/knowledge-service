# Knowledge Service Documentation

## Table of Contents

1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Quick Start](#quick-start)
4. [API Documentation](#api-documentation)
5. [Configuration](#configuration)
6. [Deployment](#deployment)
7. [Monitoring](#monitoring)
8. [Troubleshooting](#troubleshooting)

## Overview

The **Topic Knowledge Service** is an intelligent educational content generation system that automatically creates comprehensive programming tutorials and delivers them via structured HTML emails. It leverages Google Gemini AI to generate high-quality, programming-focused educational content.

### Key Features

- **Automated Content Generation**: Uses AI to create detailed programming tutorials
- **Dual Email System**: Sends both overview and deep-dive emails
- **Excel Integration**: Manages topics through Excel files
- **Scheduled Processing**: Daily automatic content generation at 5:00 AM Vietnam time
- **REST API**: Manual control and monitoring endpoints
- **Rich HTML Emails**: Professional formatting with syntax highlighting
- **Robust Error Handling**: Graceful handling of AI response issues

### Technology Stack

- **Backend**: Spring Boot 3.x with Java 17+
- **Database**: PostgreSQL/H2 Database
- **AI Integration**: Google Gemini API
- **Email**: Spring Mail with HTML templates
- **Scheduling**: Spring Scheduler
- **Excel Processing**: Apache POI
- **JSON Processing**: Jackson ObjectMapper

## Architecture

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   TopicScheduler│───▶│TopicProcessing   │───▶│ContentGeneration│
│   (Cron Jobs)   │    │Service           │    │Service          │
└─────────────────┘    └──────────────────┘    └─────────────────┘
                                │                         │
                                ▼                         ▼
                       ┌──────────────────┐    ┌─────────────────┐
                       │   TopicExcel     │    │  GeminiTopic    │
                       │   Service        │    │  Client         │
                       └──────────────────┘    └─────────────────┘
                                │                         │
                                ▼                         ▼
                       ┌──────────────────┐    ┌─────────────────┐
                       │   Excel Files    │    │  Google Gemini  │
                       │   (topics.xlsx)  │    │  API            │
                       └──────────────────┘    └─────────────────┘
                                                         │
                                ┌────────────────────────┘
                                ▼
                       ┌─────────────────┐    ┌─────────────────┐
                       │ContentParser    │───▶│TopicEmail       │
                       │Service          │    │Service          │
                       └─────────────────┘    └─────────────────┘
                                                         │
                                                         ▼
                                                ┌─────────────────┐
                                                │   Email         │
                                                │   Delivery      │
                                                └─────────────────┘
```

## Quick Start

### Prerequisites

1. **Java 17** or higher
2. **Maven 3.6+**
3. **Google Gemini API Key**
4. **Email SMTP Configuration**
5. **Excel file** with topics (topics.xlsx)

### Installation

1. Clone the repository
2. Configure `application.properties`
3. Place your `topics.xlsx` file in the project root
4. Run the application

```bash
# Clone and setup
git clone <repository-url>
cd knowledge-service

# Configure (see Configuration section)
cp application.properties.example application.properties
# Edit application.properties with your settings

# Run
mvn spring-boot:run
```

### Basic Configuration

```properties
# AI Configuration
app.llm-provider=https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent
app.llm-api-key=your-gemini-api-key

# Email Configuration
app.mail-from=your-email@domain.com
app.mail-to=recipient@domain.com
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@domain.com
spring.mail.password=your-app-password

# Processing Configuration
app.daily-topic-limit=1
app.schedule-enabled=true
```

## API Documentation

### Endpoints

#### 1. Manual Processing Trigger
```http
POST /api/topics/trigger
```
- **Description**: Manually trigger immediate topic processing
- **Response**: Success/Error message
- **Example**:
  ```bash
  curl -X POST http://localhost:8283/api/topics/trigger
  ```

#### 2. Process Specific Topics
```http
POST /api/topics/process
Content-Type: application/json

["Topic Name 1", "Topic Name 2"]
```
- **Description**: Process specific topics by name
- **Body**: JSON array of topic names
- **Example**:
  ```bash
  curl -X POST http://localhost:8283/api/topics/process \
    -H "Content-Type: application/json" \
    -d '["Lambda Expressions", "Streams API"]'
  ```

#### 3. Add New Topics
```http
POST /api/topics/add
Content-Type: application/json

[
  {
    "name": "New Topic",
    "category": "Programming"
  }
]
```

#### 4. Get Processing Statistics
```http
GET /api/topics/stats
```
- **Response**: Processing statistics and counts

### Response Formats

#### Success Response
```json
{
  "message": "Topic processing triggered successfully",
  "timestamp": "2025-08-27T14:30:00Z"
}
```

#### Error Response
```json
{
  "error": "Topic processing failed",
  "details": "Specific error message",
  "timestamp": "2025-08-27T14:30:00Z"
}
```

## Configuration

### Core Configuration Properties

| Property | Description | Default | Required |
|----------|-------------|---------|----------|
| `app.llm-provider` | Google Gemini API endpoint | - | Yes |
| `app.llm-api-key` | Your Gemini API key | - | Yes |
| `app.mail-from` | Sender email address | - | Yes |
| `app.mail-to` | Recipient email address | - | Yes |
| `app.daily-topic-limit` | Topics per day | 1 | No |
| `app.schedule-enabled` | Enable scheduling | true | No |
| `app.reprocess-after-days` | Days before reprocessing | 30 | No |

### Email Configuration

```properties
# SMTP Settings
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

### Database Configuration

```properties
# H2 Database (Development)
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect

# PostgreSQL (Production)
spring.datasource.url=jdbc:postgresql://localhost:5432/knowledge_service
spring.datasource.username=postgres
spring.datasource.password=your-password
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

### Logging Configuration

```properties
# Logging Levels
logging.level.com.knowledge.topic=INFO
logging.level.org.springframework.mail=DEBUG
logging.level.org.springframework.web=INFO

# Log File
logging.file.name=logs/knowledge-service.log
logging.pattern.file=%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n
```

## Excel File Format

The `topics.xlsx` file should have the following structure:

| Column A | Column B | Column C | Column D |
|----------|----------|----------|----------|
| Topic Name | Category | Status | Last Processed |
| Lambda Expressions | Functional Programming | NEW | |
| Streams API | Functional Programming | NEW | |
| Optional & Null Safety | Functional Programming | DONE | 2025-08-27 14:30:00 |

### Status Values
- **NEW**: Ready for processing
- **PROCESSING**: Currently being processed
- **DONE**: Successfully processed
- **ERROR**: Processing failed

## Deployment

### Development Environment

```bash
# Run with development profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Or with Maven
./mvnw spring-boot:run
```

### Production Deployment

```bash
# Build JAR
mvn clean package

# Run JAR
java -jar target/topic-knowledge-service-1.0.0.jar \
  --spring.profiles.active=prod \
  --server.port=8283
```

### Docker Deployment

```dockerfile
FROM openjdk:17-jdk-slim

COPY target/topic-knowledge-service-*.jar app.jar
COPY topics.xlsx /app/topics.xlsx

WORKDIR /app
EXPOSE 8283

ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Environment Variables

```bash
# Core Configuration
LLM_API_KEY=your-gemini-api-key
MAIL_FROM=sender@domain.com
MAIL_TO=recipient@domain.com
MAIL_PASSWORD=your-app-password

# Database
DATABASE_URL=jdbc:postgresql://localhost:5432/knowledge_service
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=your-db-password
```

## Monitoring

### Application Health

The application provides several monitoring endpoints:

```bash
# Application health
curl http://localhost:8283/actuator/health

# Processing statistics
curl http://localhost:8283/api/topics/stats

# Check scheduler status
curl http://localhost:8283/actuator/scheduledtasks
```

### Log Monitoring

Key log patterns to monitor:

```bash
# Successful processing
grep "Successfully processed" logs/knowledge-service.log

# Errors
grep "ERROR" logs/knowledge-service.log

# Email delivery status
grep "email" logs/knowledge-service.log
```

### Metrics to Track

1. **Processing Success Rate**: Topics processed vs. failed
2. **Email Delivery Rate**: Emails sent vs. failed
3. **AI API Response Time**: Gemini API call duration
4. **Content Quality**: Word count and structure validation
5. **Error Patterns**: Common failure reasons

## Troubleshooting

### Common Issues

#### 1. JSON Parsing Errors
**Symptom**: `Failed to parse detailed JSON content`
**Cause**: AI response truncated or malformed
**Solution**: 
- Check Gemini API response limits
- Review prompt length
- Verify network connectivity

#### 2. Email Delivery Failures
**Symptom**: `Mail sending failed`
**Cause**: SMTP configuration issues
**Solution**:
- Verify SMTP credentials
- Check firewall/network access
- Enable "Less secure app access" for Gmail

#### 3. Excel File Issues
**Symptom**: `No topics available for processing`
**Cause**: Excel file format or location issues
**Solution**:
- Verify Excel file exists in project root
- Check column structure matches expected format
- Ensure topics have "NEW" status

#### 4. Scheduling Not Working
**Symptom**: No automatic processing at 5:00 AM
**Cause**: Scheduling disabled or timezone issues
**Solution**:
- Check `app.schedule-enabled=true`
- Verify timezone configuration
- Check application logs for scheduler health

### Debug Commands

```bash
# Check application status
curl http://localhost:8283/actuator/health

# View recent logs
tail -f logs/knowledge-service.log

# Test email configuration
curl -X POST http://localhost:8283/api/topics/trigger

# Check database content
# Connect to H2 console: http://localhost:8283/h2-console
```

### Performance Optimization

1. **AI API Calls**: Add caching for repeated topics
2. **Email Generation**: Optimize template rendering
3. **Database Queries**: Add indexes for frequently queried fields
4. **Memory Usage**: Monitor heap usage during processing

## Contributing

### Development Setup

1. Fork the repository
2. Create feature branch
3. Follow coding standards
4. Add tests for new features
5. Submit pull request

### Code Style

- Use Java 17+ features
- Follow Spring Boot conventions
- Add comprehensive logging
- Include error handling
- Write unit tests

### Testing

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=TopicProcessingServiceTest

# Generate coverage report
mvn jacoco:report
```

## License

This project is licensed under the MIT License. See LICENSE file for details.

## Support

For support and questions:
- Create GitHub issues for bugs
- Check logs for error details
- Review configuration settings
- Consult API documentation

---

**Last Updated**: August 27, 2025
**Version**: 1.0.0
