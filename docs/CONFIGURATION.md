# Configuration Reference

## Table of Contents

1. [Overview](#overview)
2. [Application Properties](#application-properties)
3. [Environment Variables](#environment-variables)
4. [Profile-Specific Configuration](#profile-specific-configuration)
5. [External Dependencies](#external-dependencies)
6. [Security Configuration](#security-configuration)
7. [Performance Tuning](#performance-tuning)
8. [Troubleshooting](#troubleshooting)

## Overview

The Knowledge Service application uses Spring Boot's configuration system with support for multiple profiles and external configuration sources.

### Configuration Hierarchy

1. **Default values** (application.properties)
2. **Profile-specific** (application-{profile}.properties)
3. **Environment variables**
4. **Command line arguments**
5. **External configuration files**

## Application Properties

### Core Application Settings

```properties
# Application Information
spring.application.name=topic-knowledge-service
spring.profiles.active=dev

# Server Configuration
server.port=8283
server.shutdown=graceful
server.tomcat.connection-timeout=20000
server.tomcat.keep-alive-timeout=15000

# Application-specific settings
app.name=Topic Knowledge Service
app.version=1.0.0
app.description=Automated educational content generation service
```

### AI Service Configuration

```properties
# Google Gemini API Configuration
app.llm-provider=https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent
app.llm-api-key=${GEMINI_API_KEY}
app.llm-timeout-seconds=60
app.llm-retry-attempts=3
app.llm-retry-delay-seconds=2

# Content Generation Settings
app.mock-ai-responses=false
app.content-cache-enabled=true
app.content-cache-ttl-hours=24
```

### Processing Configuration

```properties
# Topic Processing
app.daily-topic-limit=1
app.reprocess-after-days=30
app.max-concurrent-processing=3
app.processing-timeout-minutes=10

# Scheduling
app.schedule-enabled=true
app.schedule-cron=0 0 5 * * *
app.schedule-timezone=Asia/Ho_Chi_Minh

# Excel Integration
app.excel-file-path=topics.xlsx
app.excel-backup-enabled=true
app.excel-backup-directory=backup/excel
```

### Email Configuration

```properties
# SMTP Settings
spring.mail.host=${SMTP_HOST:smtp.gmail.com}
spring.mail.port=${SMTP_PORT:587}
spring.mail.username=${SMTP_USERNAME}
spring.mail.password=${SMTP_PASSWORD}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.connectiontimeout=5000
spring.mail.properties.mail.smtp.timeout=5000
spring.mail.properties.mail.smtp.writetimeout=5000

# Application Email Settings
app.mail-from=${MAIL_FROM}
app.mail-to=${MAIL_TO}
app.mail-enabled=true
app.mock-email-sending=false
app.email-retry-attempts=3
app.email-template-cache-enabled=true
```

### Database Configuration

```properties
# Database Connection
spring.datasource.url=${DATABASE_URL:jdbc:h2:mem:devdb}
spring.datasource.username=${DATABASE_USERNAME:sa}
spring.datasource.password=${DATABASE_PASSWORD:}
spring.datasource.driver-class-name=${DATABASE_DRIVER:org.h2.Driver}

# Connection Pool (HikariCP)
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.idle-timeout=300000
spring.datasource.hikari.connection-timeout=20000
spring.datasource.hikari.leak-detection-threshold=60000

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=${JPA_DIALECT:org.hibernate.dialect.H2Dialect}
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.use_sql_comments=true
spring.jpa.properties.hibernate.jdbc.batch_size=25
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true
```

### Logging Configuration

```properties
# Logging Levels
logging.level.com.knowledge.topic=INFO
logging.level.org.springframework.web=INFO
logging.level.org.springframework.mail=WARN
logging.level.org.springframework.security=WARN
logging.level.org.hibernate.SQL=WARN
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=WARN

# Log File Configuration
logging.file.name=${LOG_FILE_PATH:logs/knowledge-service.log}
logging.file.max-size=100MB
logging.file.max-history=30
logging.file.total-size-cap=1GB

# Log Pattern
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n
logging.pattern.file=%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n

# Logback Configuration
logging.config=classpath:logback-spring.xml
```

### Monitoring & Management

```properties
# Actuator Configuration
management.endpoints.web.exposure.include=health,info,metrics,loggers,configprops
management.endpoints.web.base-path=/actuator
management.endpoint.health.show-details=when-authorized
management.endpoint.health.probes.enabled=true
management.health.db.enabled=true
management.health.mail.enabled=true

# Metrics
management.metrics.enabled=true
management.metrics.export.prometheus.enabled=true
management.metrics.distribution.percentiles-histogram.http.server.requests=true

# Info Endpoint
management.info.env.enabled=true
management.info.git.mode=full
management.info.build.enabled=true

# Application Info
info.app.name=${spring.application.name}
info.app.version=@project.version@
info.app.description=${app.description}
info.app.build-time=@maven.build.timestamp@
```

### Security Configuration

```properties
# Basic Security (if enabled)
spring.security.user.name=${ADMIN_USERNAME:admin}
spring.security.user.password=${ADMIN_PASSWORD:admin123}
spring.security.user.roles=ADMIN

# CORS Configuration
app.cors.allowed-origins=${CORS_ALLOWED_ORIGINS:http://localhost:3000,http://localhost:8080}
app.cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS
app.cors.allowed-headers=*
app.cors.allow-credentials=true
app.cors.max-age=3600
```

## Environment Variables

### Required Environment Variables

| Variable | Description | Example | Required |
|----------|-------------|---------|----------|
| `GEMINI_API_KEY` | Google Gemini API key | `AIzaSyC...` | Yes |
| `SMTP_USERNAME` | Email username | `service@company.com` | Yes |
| `SMTP_PASSWORD` | Email password/app password | `app_password_123` | Yes |
| `MAIL_FROM` | Sender email address | `knowledge@company.com` | Yes |
| `MAIL_TO` | Recipient email address | `team@company.com` | Yes |

### Optional Environment Variables

| Variable | Description | Default | Example |
|----------|-------------|---------|---------|
| `DATABASE_URL` | Database connection URL | `jdbc:h2:mem:devdb` | `jdbc:postgresql://localhost:5432/knowledge` |
| `DATABASE_USERNAME` | Database username | `sa` | `app_user` |
| `DATABASE_PASSWORD` | Database password | (empty) | `secure_password` |
| `SMTP_HOST` | SMTP server host | `smtp.gmail.com` | `smtp.company.com` |
| `SMTP_PORT` | SMTP server port | `587` | `465` |
| `LOG_FILE_PATH` | Log file location | `logs/knowledge-service.log` | `/var/log/app.log` |
| `SPRING_PROFILES_ACTIVE` | Active Spring profiles | `dev` | `prod,monitoring` |

### Environment Variable Loading

Create `.env` file for local development:

```bash
# .env file
GEMINI_API_KEY=your_gemini_api_key_here
SMTP_USERNAME=your_email@gmail.com
SMTP_PASSWORD=your_app_password
MAIL_FROM=knowledge-service@yourcompany.com
MAIL_TO=recipient@yourcompany.com
DATABASE_URL=jdbc:postgresql://localhost:5432/knowledge_service
DATABASE_USERNAME=app_user
DATABASE_PASSWORD=your_database_password
SPRING_PROFILES_ACTIVE=dev
```

### Docker Environment Variables

```yaml
# docker-compose.yml
version: '3.8'
services:
  app:
    image: knowledge-service:latest
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - GEMINI_API_KEY=${GEMINI_API_KEY}
      - DATABASE_URL=jdbc:postgresql://database:5432/knowledge_service
      - DATABASE_USERNAME=app_user
      - DATABASE_PASSWORD=${DB_PASSWORD}
      - SMTP_HOST=${SMTP_HOST}
      - SMTP_USERNAME=${SMTP_USERNAME}
      - SMTP_PASSWORD=${SMTP_PASSWORD}
      - MAIL_FROM=${MAIL_FROM}
      - MAIL_TO=${MAIL_TO}
```

## Profile-Specific Configuration

### Development Profile (`application-dev.properties`)

```properties
# Development Database
spring.datasource.url=jdbc:h2:mem:devdb
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# Development Settings
app.schedule-enabled=false
app.mock-ai-responses=true
app.mock-email-sending=true
app.daily-topic-limit=5

# Debug Logging
logging.level.com.knowledge.topic=DEBUG
logging.level.org.springframework.web=DEBUG
logging.level.org.springframework.mail=DEBUG
logging.level.org.springframework.transaction=DEBUG

# Development Security
management.endpoints.web.exposure.include=*
management.endpoint.health.show-details=always

# Development Tools
spring.devtools.restart.enabled=true
spring.devtools.livereload.enabled=true
```

### Testing Profile (`application-test.properties`)

```properties
# Test Database
spring.datasource.url=jdbc:h2:mem:testdb
spring.jpa.hibernate.ddl-auto=create-drop

# Test Settings
app.schedule-enabled=false
app.mock-ai-responses=true
app.mock-email-sending=true
app.daily-topic-limit=10

# Test Logging
logging.level.com.knowledge.topic=WARN
logging.level.org.springframework.test=DEBUG
logging.level.org.testcontainers=INFO

# Disable banner for cleaner test output
spring.main.banner-mode=off
```

### Staging Profile (`application-staging.properties`)

```properties
# Staging Database
spring.datasource.url=jdbc:postgresql://staging-db:5432/knowledge_service_staging
spring.datasource.username=${DATABASE_USERNAME}
spring.datasource.password=${DATABASE_PASSWORD}

# Staging Settings
app.schedule-enabled=true
app.daily-topic-limit=1
app.reprocess-after-days=7

# Staging Email (use test recipients)
app.mail-to=staging-team@company.com

# Staging Logging
logging.level.com.knowledge.topic=INFO
logging.file.name=/var/log/knowledge-service/staging.log

# Staging Monitoring
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=when-authorized
```

### Production Profile (`application-prod.properties`)

```properties
# Production Database
spring.datasource.url=jdbc:postgresql://${DATABASE_HOST}:5432/${DATABASE_NAME}
spring.datasource.username=${DATABASE_USERNAME}
spring.datasource.password=${DATABASE_PASSWORD}
spring.jpa.hibernate.ddl-auto=validate

# Production Settings
app.schedule-enabled=true
app.daily-topic-limit=1
app.reprocess-after-days=30
app.processing-timeout-minutes=15

# Production Logging
logging.level.com.knowledge.topic=INFO
logging.level.org.springframework=WARN
logging.file.name=/var/log/knowledge-service/production.log

# Production Security
server.error.include-stacktrace=never
server.error.include-message=never
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=never

# Production Performance
spring.datasource.hikari.maximum-pool-size=25
spring.jpa.properties.hibernate.jdbc.batch_size=50
```

## External Dependencies

### Google Gemini API Configuration

```properties
# API Endpoint
app.llm-provider=https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent

# Authentication
app.llm-api-key=${GEMINI_API_KEY}

# Request Configuration
app.llm-timeout-seconds=60
app.llm-retry-attempts=3
app.llm-retry-delay-seconds=2
app.llm-max-tokens=8192

# Rate Limiting
app.llm-rate-limit-requests-per-minute=60
app.llm-rate-limit-enabled=true

# Content Configuration
app.llm-temperature=0.7
app.llm-top-p=0.9
app.llm-safety-settings=BLOCK_MEDIUM_AND_ABOVE
```

### SMTP Configuration Examples

#### Gmail Configuration
```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

#### Office 365 Configuration
```properties
spring.mail.host=smtp.office365.com
spring.mail.port=587
spring.mail.username=your-email@company.com
spring.mail.password=your-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

#### Custom SMTP Configuration
```properties
spring.mail.host=${SMTP_HOST}
spring.mail.port=${SMTP_PORT:587}
spring.mail.username=${SMTP_USERNAME}
spring.mail.password=${SMTP_PASSWORD}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.ssl.trust=${SMTP_HOST}
```

### Database Configuration Examples

#### PostgreSQL Configuration
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/knowledge_service
spring.datasource.username=app_user
spring.datasource.password=secure_password
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# PostgreSQL-specific settings
spring.jpa.properties.hibernate.jdbc.lob.non_contextual_creation=true
spring.jpa.properties.hibernate.temp.use_jdbc_metadata_defaults=false
```

#### MySQL Configuration
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/knowledge_service
spring.datasource.username=app_user
spring.datasource.password=secure_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect

# MySQL-specific settings
spring.jpa.properties.hibernate.jdbc.time_zone=UTC
```

#### H2 Configuration (Development)
```properties
spring.datasource.url=jdbc:h2:mem:devdb
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect
```

## Security Configuration

### Basic Authentication (Optional)

```properties
# Enable basic authentication
spring.security.user.name=${ADMIN_USERNAME:admin}
spring.security.user.password=${ADMIN_PASSWORD:change_me}
spring.security.user.roles=ADMIN

# Secure actuator endpoints
management.endpoints.web.exposure.include=health,info
management.endpoint.health.show-details=when-authorized
```

### SSL/TLS Configuration

```properties
# Enable HTTPS
server.port=8443
server.ssl.enabled=true
server.ssl.key-store=${SSL_KEYSTORE_PATH:classpath:keystore.p12}
server.ssl.key-store-password=${SSL_KEYSTORE_PASSWORD}
server.ssl.key-store-type=PKCS12
server.ssl.key-alias=${SSL_KEY_ALIAS:knowledge-service}

# SSL Protocol Configuration
server.ssl.protocol=TLS
server.ssl.enabled-protocols=TLSv1.2,TLSv1.3
server.ssl.ciphers=TLS_AES_256_GCM_SHA384,TLS_CHACHA20_POLY1305_SHA256,TLS_AES_128_GCM_SHA256
```

### CORS Configuration

```properties
# CORS Settings
app.cors.allowed-origins=${CORS_ALLOWED_ORIGINS:http://localhost:3000}
app.cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS
app.cors.allowed-headers=Authorization,Content-Type,X-Requested-With
app.cors.allow-credentials=true
app.cors.max-age=3600
```

## Performance Tuning

### JVM Configuration

```properties
# JVM Memory Settings (via JAVA_OPTS environment variable)
# JAVA_OPTS=-Xms1g -Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200

# Application Performance Settings
server.tomcat.threads.max=200
server.tomcat.threads.min-spare=50
server.tomcat.max-connections=8192
server.tomcat.accept-count=100
server.tomcat.connection-timeout=20000
```

### Database Performance

```properties
# Connection Pool Tuning
spring.datasource.hikari.maximum-pool-size=25
spring.datasource.hikari.minimum-idle=10
spring.datasource.hikari.idle-timeout=300000
spring.datasource.hikari.connection-timeout=20000
spring.datasource.hikari.leak-detection-threshold=60000

# JPA Performance
spring.jpa.properties.hibernate.jdbc.batch_size=50
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true
spring.jpa.properties.hibernate.jdbc.batch_versioned_data=true
spring.jpa.properties.hibernate.cache.use_second_level_cache=false
```

### Caching Configuration

```properties
# Spring Cache
spring.cache.type=caffeine
spring.cache.caffeine.spec=maximumSize=1000,expireAfterWrite=1h

# Application Caching
app.content-cache-enabled=true
app.content-cache-ttl-hours=24
app.content-cache-max-size=500
```

### Async Processing

```properties
# Async Configuration
spring.task.execution.pool.core-size=4
spring.task.execution.pool.max-size=16
spring.task.execution.pool.queue-capacity=100
spring.task.execution.pool.keep-alive=60s
spring.task.execution.thread-name-prefix=knowledge-task-

# Scheduling
spring.task.scheduling.pool.size=4
spring.task.scheduling.thread-name-prefix=knowledge-scheduler-
```

## Troubleshooting

### Common Configuration Issues

#### 1. Missing Environment Variables
```bash
# Check if environment variables are set
echo $GEMINI_API_KEY
echo $SMTP_USERNAME

# Verify application can read them
curl http://localhost:8283/actuator/configprops | jq '.contexts.application.beans.app-*'
```

#### 2. Database Connection Issues
```properties
# Add connection validation
spring.datasource.hikari.connection-test-query=SELECT 1
spring.datasource.hikari.validation-timeout=3000

# Enable connection debugging
logging.level.com.zaxxer.hikari=DEBUG
logging.level.org.hibernate.engine.jdbc.connections=DEBUG
```

#### 3. Email Configuration Problems
```properties
# Test email settings
spring.mail.test-connection=true

# Enable email debugging
spring.mail.properties.mail.debug=true
logging.level.org.springframework.mail=DEBUG
```

#### 4. AI API Issues
```properties
# Add request/response logging
logging.level.org.springframework.web.reactive.function.client=DEBUG

# Increase timeout
app.llm-timeout-seconds=120

# Add retry configuration
app.llm-retry-attempts=5
app.llm-retry-delay-seconds=5
```

### Configuration Validation

```bash
# Validate configuration syntax
java -jar app.jar --spring.config.location=application.properties --debug

# Check active profiles
java -jar app.jar --spring.profiles.active=prod --spring.config.on-not-found=fail

# Validate externalized configuration
java -jar app.jar --spring.config.import=file:./custom-config.properties
```

### Environment-Specific Debugging

```properties
# Development debugging
logging.level.org.springframework.boot.context.config=DEBUG
logging.level.org.springframework.core.env=DEBUG

# Configuration binding debugging
logging.level.org.springframework.boot.context.properties=DEBUG

# Profile debugging
logging.level.org.springframework.core.env.PropertySourcesPropertyResolver=DEBUG
```

---

**Last Updated**: August 27, 2025  
**Configuration Reference Version**: 1.0.0
