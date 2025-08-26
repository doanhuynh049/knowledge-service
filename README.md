# Daily Embedded Learning System

A production-ready Java Spring Boot application that sends daily learning emails about embedded systems topics. Users can define multiple topics per day, and the system generates both overview digests and detailed deep-dive lessons using OpenAI's GPT models.

## Features

- **Multi-topic Daily Planning**: Users can schedule multiple embedded systems topics per day with customizable sequencing
- **AI-Generated Content**: Leverages OpenAI API to create comprehensive overviews and detailed deep-dive lessons
- **Flexible Email Delivery**: Supports both digest-and-split mode (separate emails) and single combined emails
- **CSV Import**: Bulk import learning plans via CSV files with full validation
- **Timezone Support**: Per-user timezone settings with configurable delivery times
- **Production Ready**: Includes Docker containerization, database migrations, monitoring, and comprehensive error handling

## Tech Stack

- **Language**: Java 17
- **Framework**: Spring Boot 3 (Web, Data JPA, Validation, Mail, Scheduling)
- **Database**: PostgreSQL (production) / H2 (development)
- **Migration**: Flyway
- **Email**: Spring Mail with SMTP support (Gmail/SendGrid compatible)
- **LLM Integration**: OpenAI Chat Completions API via HttpClient
- **Build Tool**: Maven
- **Containerization**: Docker + Docker Compose
- **Testing**: JUnit 5 + Testcontainers

## Quick Start

### Prerequisites

- Java 17+
- Docker and Docker Compose
- OpenAI API key
- SMTP credentials (Gmail App Password or SendGrid API key)

### 1. Environment Setup

Copy the environment template and configure your credentials:

```bash
cp .env.example .env
```

Edit `.env` with your actual credentials:

```bash
# Required
OPENAI_API_KEY=sk-your-openai-api-key-here
SMTP_USER=your-email@gmail.com
SMTP_PASS=your-app-password
APP_API_KEY=your-secure-api-key-here

# Optional (defaults provided)
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
```

### 2. Run with Docker Compose

```bash
# Start the complete stack (PostgreSQL + Application)
docker-compose up -d

# Check service status
docker-compose ps

# View logs
docker-compose logs -f app
```

The application will be available at `http://localhost:8080`

### 3. Development Mode

For local development with H2 database:

```bash
# Set development profile
export SPRING_PROFILES_ACTIVE=dev

# Run locally
mvn spring-boot:run

# Access H2 console: http://localhost:8080/h2-console
# JDBC URL: jdbc:h2:mem:testdb
# Username: sa, Password: (empty)
```

## API Usage

All API endpoints require the `X-API-Key` header with your configured API key.

### User Management

```bash
# Create or update user
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -H "X-API-Key: your-api-key" \
  -d '{
    "email": "engineer@company.com",
    "timezone": "America/New_York",
    "deliveryHourLocal": 9
  }'

# Get user by email
curl -H "X-API-Key: your-api-key" \
  "http://localhost:8080/api/users/by-email/engineer@company.com"
```

### Learning Plan Management

```bash
# Add plan items (JSON)
curl -X POST http://localhost:8080/api/plan/items?userId=1 \
  -H "Content-Type: application/json" \
  -H "X-API-Key: your-api-key" \
  -d '[{
    "date": "2024-12-01",
    "seq": 1,
    "topicTitle": "UART Communication Fundamentals",
    "focusPoints": "Baud rate calculation, frame structure, error detection",
    "learningGoal": "Implement robust UART communication",
    "difficulty": "intermediate",
    "platforms": "STM32, Arduino",
    "outputType": "both"
  }]'

# Upload CSV file
curl -X POST http://localhost:8080/api/plan/csv-upload \
  -H "X-API-Key: your-api-key" \
  -F "userId=1" \
  -F "file=@learning-plan.csv"

# Get daily plan
curl -H "X-API-Key: your-api-key" \
  "http://localhost:8080/api/plan/daily?userId=1&date=2024-12-01"

# Skip a topic
curl -X POST http://localhost:8080/api/plan/skip \
  -H "Content-Type: application/json" \
  -H "X-API-Key: your-api-key" \
  -d '{"userId": 1, "date": "2024-12-01", "seq": 1}'
```

### Settings Configuration

```bash
# Update user settings
curl -X POST http://localhost:8080/api/settings \
  -H "Content-Type: application/json" \
  -H "X-API-Key: your-api-key" \
  -d '{
    "userId": 1,
    "emailMode": "DIGEST_AND_SPLIT",
    "maxDeepDivesPerDay": 3,
    "model": "gpt-4",
    "temperature": 0.7,
    "maxTokens": 2000
  }'
```

### Testing and Administration

```bash
# Send test email
curl -X POST http://localhost:8080/api/test/email \
  -H "Content-Type: application/json" \
  -H "X-API-Key: your-api-key" \
  -d '{"toEmail": "test@example.com"}'

# Resend lesson content
curl -X POST http://localhost:8080/api/lessons/resend \
  -H "Content-Type: application/json" \
  -H "X-API-Key: your-api-key" \
  -d '{"userId": 1, "date": "2024-12-01", "seq": 1, "type": "deepdive"}'

# Health check (no auth required)
curl http://localhost:8080/actuator/health
```

## CSV Import Format

Create CSV files with the following headers:

```csv
date,seq,topic_title,focus_points,learning_goal,difficulty,platforms,language,experience_level,output_type,authoritative_links,tags,notes
2024-12-01,1,"UART Communication","Baud rates, framing","Implement UART driver","intermediate","STM32","en","intermediate","both","https://example.com/uart-guide","communication,serial","Focus on error handling"
2024-12-01,2,"I2C Protocol","Clock stretching, addressing","Master I2C implementation","advanced","ESP32","en","advanced","deepdive","","i2c,protocols","Multi-master scenarios"
```

**Field Validation:**
- `date`: ISO format (YYYY-MM-DD)
- `seq`: Integer >= 1 (unique per user per date)
- `language`: Must be "en"
- `output_type`: "overview", "deepdive", or "both"

## Email Delivery Modes

### DIGEST_AND_SPLIT Mode
1. **Daily Digest**: Single overview email covering all topics
2. **Deep Dives**: Separate detailed emails for each topic (respects `outputType`)

### SINGLE Mode  
1. **Combined Email**: Overview + topic summaries with links to stored deep-dive content

## Scheduled Delivery

The system automatically processes emails every 10 minutes:

1. Checks each user's local time against their `deliveryHourLocal` setting
2. Processes planned topics for the current date
3. Generates content using OpenAI API with user's LLM settings
4. Sends emails according to user's `emailMode` preference
5. Marks items as `SENT` and logs all email attempts

**Delivery Window**: ±5 minutes from configured delivery hour
**Guardrails**: Respects `maxDeepDivesPerDay` limit, handles API failures gracefully

## Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `OPENAI_API_KEY` | OpenAI API key (required) | - |
| `SMTP_HOST` | SMTP server hostname | smtp.gmail.com |
| `SMTP_PORT` | SMTP server port | 587 |
| `SMTP_USER` | SMTP username | - |
| `SMTP_PASS` | SMTP password | - |
| `APP_API_KEY` | API authentication key | changeme |
| `DB_HOST` | PostgreSQL host (prod) | localhost |
| `DB_PORT` | PostgreSQL port (prod) | 5432 |
| `DB_NAME` | PostgreSQL database name | dailyembedded |
| `DB_USER` | PostgreSQL username | postgres |
| `DB_PASS` | PostgreSQL password | password |

### Application Profiles

- **dev**: H2 in-memory database, debug logging, H2 console enabled
- **prod**: PostgreSQL, optimized logging, health endpoint security

## Development

### Running Tests

```bash
# Run all tests
mvn test

# Run with Testcontainers (requires Docker)
mvn test -P integration-tests
```

### Code Formatting

```bash
# Check formatting
mvn spotless:check

# Apply formatting
mvn spotless:apply
```

### Database Migrations

```bash
# Run migrations
mvn flyway:migrate

# Migration info
mvn flyway:info
```

## Monitoring

- **Health Endpoint**: `/actuator/health`
- **Metrics**: `/actuator/metrics` 
- **Application Info**: `/actuator/info`

## Architecture

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Users/API     │────│  Spring Boot App │────│   PostgreSQL    │
└─────────────────┘    └──────────────────┘    └─────────────────┘
                               │
                       ┌───────┼───────┐
                       │       │       │
              ┌────────▼──┐ ┌──▼────┐ ┌▼──────────┐
              │ OpenAI API│ │ SMTP  │ │ Scheduler │
              └───────────┘ └───────┘ └───────────┘
```

## Support

For issues or questions:
1. Check the application logs: `docker-compose logs app`
2. Verify environment configuration
3. Test SMTP connectivity: `POST /api/test/email`
4. Validate OpenAI API key with a simple request

## License

This project is licensed under the MIT License.
