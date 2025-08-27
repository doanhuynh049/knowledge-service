# Deployment Guide

## Table of Contents

1. [Overview](#overview)
2. [Environment Requirements](#environment-requirements)
3. [Configuration Management](#configuration-management)
4. [Deployment Options](#deployment-options)
5. [Production Setup](#production-setup)
6. [Monitoring & Health Checks](#monitoring--health-checks)
7. [Backup & Recovery](#backup--recovery)
8. [Troubleshooting](#troubleshooting)

## Overview

This guide covers deploying the Knowledge Service application across different environments, from local development to production systems.

### Deployment Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Load Balancer │    │  Application    │    │    Database     │
│   (Optional)    │───▶│   Instances     │───▶│  PostgreSQL     │
│                 │    │                 │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                │
                                ▼
                       ┌─────────────────┐
                       │   External      │
                       │   Services      │
                       │  - Gemini API   │
                       │  - SMTP Server  │
                       └─────────────────┘
```

## Environment Requirements

### Minimum System Requirements

| Environment | CPU | RAM | Storage | Java |
|-------------|-----|-----|---------|------|
| Development | 2 cores | 4 GB | 10 GB | OpenJDK 17+ |
| Testing | 2 cores | 4 GB | 20 GB | OpenJDK 17+ |
| Staging | 2 cores | 8 GB | 50 GB | OpenJDK 17+ |
| Production | 4 cores | 16 GB | 100 GB | OpenJDK 17+ |

### Software Dependencies

```bash
# Required
- Java 17 or higher
- Maven 3.6+ (for building)
- PostgreSQL 12+ (production database)

# Optional
- Docker 20.10+
- Kubernetes 1.20+ (for container orchestration)
- Nginx (reverse proxy)
- Redis (caching, future feature)
```

### Network Requirements

- **Outbound HTTPS (443)**: Google Gemini API access
- **Outbound SMTP (587/465)**: Email delivery
- **Inbound HTTP (8283)**: Application access
- **Database**: PostgreSQL port (5432)

## Configuration Management

### Environment-Specific Configurations

#### Development (`application-dev.properties`)
```properties
# Database
spring.datasource.url=jdbc:h2:mem:devdb
spring.h2.console.enabled=true

# AI & Email (Mock)
app.mock-ai-responses=true
app.mock-email-sending=true

# Scheduling
app.schedule-enabled=false

# Logging
logging.level.com.knowledge.topic=DEBUG
```

#### Testing (`application-test.properties`)
```properties
# Database
spring.datasource.url=jdbc:h2:mem:testdb
spring.jpa.hibernate.ddl-auto=create-drop

# AI & Email (Mock)
app.mock-ai-responses=true
app.mock-email-sending=true

# Testing
app.schedule-enabled=false
logging.level.org.springframework.test=DEBUG
```

#### Staging (`application-staging.properties`)
```properties
# Database
spring.datasource.url=jdbc:postgresql://staging-db:5432/knowledge_service
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

# AI Configuration
app.llm-provider=${GEMINI_API_URL}
app.llm-api-key=${GEMINI_API_KEY}

# Email Configuration
spring.mail.host=${SMTP_HOST}
spring.mail.username=${SMTP_USERNAME}
spring.mail.password=${SMTP_PASSWORD}

# Scheduling
app.schedule-enabled=true
app.daily-topic-limit=1

# Monitoring
management.endpoints.web.exposure.include=health,info,metrics
```

#### Production (`application-prod.properties`)
```properties
# Database
spring.datasource.url=jdbc:postgresql://${DB_HOST}:5432/${DB_NAME}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5

# AI Configuration
app.llm-provider=${GEMINI_API_URL}
app.llm-api-key=${GEMINI_API_KEY}

# Email Configuration
spring.mail.host=${SMTP_HOST}
spring.mail.port=${SMTP_PORT}
spring.mail.username=${SMTP_USERNAME}
spring.mail.password=${SMTP_PASSWORD}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# Application Settings
app.schedule-enabled=true
app.daily-topic-limit=1
app.reprocess-after-days=30

# Security
server.port=8283
server.shutdown=graceful

# Logging
logging.level.com.knowledge.topic=INFO
logging.file.name=/var/log/knowledge-service/application.log
logging.pattern.file=%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n

# Monitoring
management.endpoints.web.exposure.include=health,info,metrics,prometheus
management.endpoint.health.show-details=when-authorized
```

### Environment Variables

Create environment-specific variable files:

#### `.env.staging`
```bash
# Database
DB_HOST=staging-postgresql.example.com
DB_NAME=knowledge_service_staging
DB_USERNAME=app_user
DB_PASSWORD=secure_staging_password

# AI Service
GEMINI_API_URL=https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent
GEMINI_API_KEY=your_staging_gemini_api_key

# Email
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=staging@yourcompany.com
SMTP_PASSWORD=staging_email_password

# Application
MAIL_FROM=staging@yourcompany.com
MAIL_TO=test-team@yourcompany.com
```

#### `.env.production`
```bash
# Database
DB_HOST=prod-postgresql.example.com
DB_NAME=knowledge_service
DB_USERNAME=app_user
DB_PASSWORD=highly_secure_production_password

# AI Service
GEMINI_API_URL=https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent
GEMINI_API_KEY=your_production_gemini_api_key

# Email
SMTP_HOST=smtp.company.com
SMTP_PORT=587
SMTP_USERNAME=knowledge-service@company.com
SMTP_PASSWORD=production_email_password

# Application
MAIL_FROM=knowledge-service@company.com
MAIL_TO=team@company.com

# Security
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8283
```

## Deployment Options

### 1. Traditional JAR Deployment

#### Build Application
```bash
# Clone repository
git clone <repository-url>
cd knowledge-service

# Build for production
mvn clean package -Pprod -DskipTests

# The JAR will be created at:
# target/topic-knowledge-service-1.0.0.jar
```

#### Deploy to Server
```bash
# Copy JAR to server
scp target/topic-knowledge-service-*.jar user@server:/opt/knowledge-service/

# Copy configuration
scp topics.xlsx user@server:/opt/knowledge-service/
scp .env.production user@server:/opt/knowledge-service/.env

# SSH to server and start application
ssh user@server
cd /opt/knowledge-service

# Load environment variables
source .env

# Start application
java -jar topic-knowledge-service-*.jar \
  --spring.profiles.active=prod \
  --server.port=8283
```

#### SystemD Service Setup
```bash
# Create service file
sudo vim /etc/systemd/system/knowledge-service.service
```

```ini
[Unit]
Description=Topic Knowledge Service
After=syslog.target network.target

[Service]
Type=simple
User=knowledge-service
Group=knowledge-service
WorkingDirectory=/opt/knowledge-service
EnvironmentFile=/opt/knowledge-service/.env
ExecStart=/usr/bin/java -jar /opt/knowledge-service/topic-knowledge-service-1.0.0.jar --spring.profiles.active=prod
ExecStop=/bin/kill -15 $MAINPID
Restart=always
RestartSec=30

# Health check
ExecStartPost=/bin/sleep 30
ExecStartPost=/bin/bash -c 'curl -f http://localhost:8283/actuator/health || exit 1'

# Logging
StandardOutput=journal
StandardError=journal
SyslogIdentifier=knowledge-service

[Install]
WantedBy=multi-user.target
```

```bash
# Enable and start service
sudo systemctl daemon-reload
sudo systemctl enable knowledge-service
sudo systemctl start knowledge-service

# Check status
sudo systemctl status knowledge-service
sudo journalctl -u knowledge-service -f
```

### 2. Docker Deployment

#### Dockerfile
```dockerfile
FROM openjdk:17-jdk-slim

# Create application user
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Set working directory
WORKDIR /app

# Copy dependencies
COPY target/topic-knowledge-service-*.jar app.jar
COPY topics.xlsx topics.xlsx

# Create logs directory
RUN mkdir -p /var/log/knowledge-service && \
    chown -R appuser:appuser /app /var/log/knowledge-service

# Switch to application user
USER appuser

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8283/actuator/health || exit 1

# Expose port
EXPOSE 8283

# Start application
ENTRYPOINT ["java", "-jar", "app.jar"]
CMD ["--spring.profiles.active=prod"]
```

#### Build and Run
```bash
# Build Docker image
docker build -t knowledge-service:1.0.0 .

# Run container
docker run -d \
  --name knowledge-service \
  --env-file .env.production \
  -p 8283:8283 \
  -v /var/log/knowledge-service:/var/log/knowledge-service \
  --restart unless-stopped \
  knowledge-service:1.0.0
```

#### Docker Compose
```yaml
version: '3.8'

services:
  app:
    build: .
    container_name: knowledge-service
    ports:
      - "8283:8283"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DB_HOST=database
    env_file:
      - .env.production
    volumes:
      - ./logs:/var/log/knowledge-service
      - ./topics.xlsx:/app/topics.xlsx
    depends_on:
      - database
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8283/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3

  database:
    image: postgres:15
    container_name: knowledge-service-db
    environment:
      POSTGRES_DB: knowledge_service
      POSTGRES_USER: app_user
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./init.sql:/docker-entrypoint-initdb.d/init.sql
    ports:
      - "5432:5432"
    restart: unless-stopped

  nginx:
    image: nginx:alpine
    container_name: knowledge-service-nginx
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf
      - ./ssl:/etc/nginx/ssl
    depends_on:
      - app
    restart: unless-stopped

volumes:
  postgres_data:
```

### 3. Kubernetes Deployment

#### Namespace
```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: knowledge-service
```

#### ConfigMap
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: knowledge-service-config
  namespace: knowledge-service
data:
  application-prod.properties: |
    spring.profiles.active=prod
    server.port=8283
    management.endpoints.web.exposure.include=health,info,metrics
    logging.level.com.knowledge.topic=INFO
```

#### Secret
```yaml
apiVersion: v1
kind: Secret
metadata:
  name: knowledge-service-secrets
  namespace: knowledge-service
type: Opaque
data:
  # Base64 encoded values
  GEMINI_API_KEY: <base64-encoded-api-key>
  DB_PASSWORD: <base64-encoded-password>
  SMTP_PASSWORD: <base64-encoded-smtp-password>
```

#### Deployment
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: knowledge-service
  namespace: knowledge-service
spec:
  replicas: 2
  selector:
    matchLabels:
      app: knowledge-service
  template:
    metadata:
      labels:
        app: knowledge-service
    spec:
      containers:
      - name: knowledge-service
        image: knowledge-service:1.0.0
        ports:
        - containerPort: 8283
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: DB_HOST
          value: "postgresql-service"
        - name: GEMINI_API_KEY
          valueFrom:
            secretKeyRef:
              name: knowledge-service-secrets
              key: GEMINI_API_KEY
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: knowledge-service-secrets
              key: DB_PASSWORD
        volumeMounts:
        - name: config-volume
          mountPath: /app/config
        - name: topics-volume
          mountPath: /app/topics.xlsx
          subPath: topics.xlsx
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8283
          initialDelaySeconds: 60
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8283
          initialDelaySeconds: 30
          periodSeconds: 10
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "2Gi"
            cpu: "1000m"
      volumes:
      - name: config-volume
        configMap:
          name: knowledge-service-config
      - name: topics-volume
        configMap:
          name: topics-xlsx
```

#### Service
```yaml
apiVersion: v1
kind: Service
metadata:
  name: knowledge-service
  namespace: knowledge-service
spec:
  selector:
    app: knowledge-service
  ports:
  - port: 80
    targetPort: 8283
  type: ClusterIP
```

#### Ingress
```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: knowledge-service-ingress
  namespace: knowledge-service
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /
    cert-manager.io/cluster-issuer: "letsencrypt-prod"
spec:
  tls:
  - hosts:
    - knowledge-service.yourcompany.com
    secretName: knowledge-service-tls
  rules:
  - host: knowledge-service.yourcompany.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: knowledge-service
            port:
              number: 80
```

## Production Setup

### Database Setup

#### PostgreSQL Installation
```bash
# Ubuntu/Debian
sudo apt update
sudo apt install postgresql postgresql-contrib

# CentOS/RHEL
sudo yum install postgresql-server postgresql-contrib
sudo postgresql-setup initdb

# Start service
sudo systemctl start postgresql
sudo systemctl enable postgresql
```

#### Database Configuration
```sql
-- Connect as postgres user
sudo -u postgres psql

-- Create database and user
CREATE DATABASE knowledge_service;
CREATE USER app_user WITH PASSWORD 'secure_password';
GRANT ALL PRIVILEGES ON DATABASE knowledge_service TO app_user;

-- Create schema (run as app_user)
\c knowledge_service app_user
CREATE SCHEMA IF NOT EXISTS knowledge;
```

#### Backup Strategy
```bash
# Daily backup script
#!/bin/bash
BACKUP_DIR="/backup/knowledge-service"
DATE=$(date +%Y%m%d_%H%M%S)
DB_NAME="knowledge_service"

# Create backup
pg_dump -h localhost -U app_user -d $DB_NAME > $BACKUP_DIR/backup_$DATE.sql

# Compress backup
gzip $BACKUP_DIR/backup_$DATE.sql

# Remove backups older than 30 days
find $BACKUP_DIR -name "backup_*.sql.gz" -mtime +30 -delete

# Upload to cloud storage (optional)
# aws s3 cp $BACKUP_DIR/backup_$DATE.sql.gz s3://your-backup-bucket/
```

### Reverse Proxy Setup (Nginx)

```nginx
upstream knowledge_service {
    server 127.0.0.1:8283;
    # For multiple instances:
    # server 127.0.0.1:8283;
    # server 127.0.0.1:8284;
}

server {
    listen 80;
    server_name knowledge-service.yourcompany.com;
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name knowledge-service.yourcompany.com;

    # SSL Configuration
    ssl_certificate /etc/ssl/certs/knowledge-service.crt;
    ssl_certificate_key /etc/ssl/private/knowledge-service.key;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers ECDHE-RSA-AES256-GCM-SHA512:DHE-RSA-AES256-GCM-SHA512:ECDHE-RSA-AES256-GCM-SHA384:DHE-RSA-AES256-GCM-SHA384;
    ssl_prefer_server_ciphers off;

    # Security Headers
    add_header X-Frame-Options DENY;
    add_header X-Content-Type-Options nosniff;
    add_header X-XSS-Protection "1; mode=block";
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains; preload";

    # Proxy Configuration
    location / {
        proxy_pass http://knowledge_service;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # Timeouts
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
        
        # Health check
        proxy_next_upstream error timeout invalid_header http_500 http_502 http_503;
    }

    # Health check endpoint
    location /health {
        access_log off;
        proxy_pass http://knowledge_service/actuator/health;
    }

    # Static assets (if any)
    location /static/ {
        alias /var/www/knowledge-service/static/;
        expires 1y;
        add_header Cache-Control "public, immutable";
    }
}
```

### SSL Certificate Setup

#### Using Let's Encrypt
```bash
# Install Certbot
sudo apt install certbot python3-certbot-nginx

# Obtain certificate
sudo certbot --nginx -d knowledge-service.yourcompany.com

# Automatic renewal
sudo crontab -e
# Add: 0 12 * * * /usr/bin/certbot renew --quiet
```

## Monitoring & Health Checks

### Application Monitoring

#### Prometheus Configuration
```yaml
# prometheus.yml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'knowledge-service'
    static_configs:
      - targets: ['localhost:8283']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 30s
```

#### Health Check Scripts
```bash
#!/bin/bash
# health-check.sh

ENDPOINT="http://localhost:8283/actuator/health"
TIMEOUT=10

# Check application health
response=$(curl -s -w "%{http_code}" --max-time $TIMEOUT $ENDPOINT)
http_code="${response: -3}"

if [ "$http_code" -eq 200 ]; then
    echo "✅ Knowledge Service is healthy"
    exit 0
else
    echo "❌ Knowledge Service is unhealthy (HTTP: $http_code)"
    exit 1
fi
```

#### Log Monitoring
```bash
#!/bin/bash
# log-monitor.sh

LOG_FILE="/var/log/knowledge-service/application.log"
ERROR_THRESHOLD=5
WARN_THRESHOLD=10

# Count errors in last hour
error_count=$(grep -c "ERROR" $LOG_FILE | tail -n 60)
warn_count=$(grep -c "WARN" $LOG_FILE | tail -n 60)

if [ "$error_count" -gt "$ERROR_THRESHOLD" ]; then
    echo "⚠️  High error rate detected: $error_count errors in last hour"
    # Send alert
fi

if [ "$warn_count" -gt "$WARN_THRESHOLD" ]; then
    echo "⚠️  High warning rate detected: $warn_count warnings in last hour"
fi
```

### External Service Monitoring

#### Database Health
```sql
-- Database health query
SELECT 
    schemaname,
    tablename,
    n_tup_ins as inserts,
    n_tup_upd as updates,
    n_tup_del as deletes
FROM pg_stat_user_tables 
WHERE schemaname = 'public';
```

#### Email Service Test
```bash
#!/bin/bash
# email-test.sh

curl -X POST http://localhost:8283/api/topics/trigger \
  -H "Content-Type: application/json" \
  --timeout 120

if [ $? -eq 0 ]; then
    echo "✅ Email service test passed"
else
    echo "❌ Email service test failed"
fi
```

## Backup & Recovery

### Application Data Backup

#### Database Backup
```bash
#!/bin/bash
# backup-database.sh

BACKUP_DIR="/backup/knowledge-service/$(date +%Y/%m/%d)"
mkdir -p $BACKUP_DIR

# Full database backup
pg_dump -h $DB_HOST -U $DB_USERNAME -d knowledge_service \
  > $BACKUP_DIR/full-backup-$(date +%H%M%S).sql

# Compress
gzip $BACKUP_DIR/full-backup-*.sql

# Table-specific backups
pg_dump -h $DB_HOST -U $DB_USERNAME -d knowledge_service \
  --table=topic --table=knowledge_content \
  > $BACKUP_DIR/data-backup-$(date +%H%M%S).sql

gzip $BACKUP_DIR/data-backup-*.sql
```

#### Configuration Backup
```bash
#!/bin/bash
# backup-config.sh

CONFIG_BACKUP="/backup/knowledge-service/config/$(date +%Y%m%d)"
mkdir -p $CONFIG_BACKUP

# Application configuration
cp /opt/knowledge-service/.env $CONFIG_BACKUP/
cp /opt/knowledge-service/topics.xlsx $CONFIG_BACKUP/

# System configuration
cp /etc/systemd/system/knowledge-service.service $CONFIG_BACKUP/
cp /etc/nginx/sites-available/knowledge-service $CONFIG_BACKUP/

# Create archive
tar -czf $CONFIG_BACKUP/../config-backup-$(date +%Y%m%d).tar.gz -C $CONFIG_BACKUP .
```

### Recovery Procedures

#### Database Recovery
```bash
#!/bin/bash
# restore-database.sh

BACKUP_FILE=$1
if [ -z "$BACKUP_FILE" ]; then
    echo "Usage: $0 <backup-file.sql.gz>"
    exit 1
fi

# Stop application
sudo systemctl stop knowledge-service

# Drop and recreate database
psql -h $DB_HOST -U postgres -c "DROP DATABASE IF EXISTS knowledge_service;"
psql -h $DB_HOST -U postgres -c "CREATE DATABASE knowledge_service;"
psql -h $DB_HOST -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE knowledge_service TO app_user;"

# Restore from backup
gunzip -c $BACKUP_FILE | psql -h $DB_HOST -U $DB_USERNAME -d knowledge_service

# Start application
sudo systemctl start knowledge-service

echo "Database recovery completed"
```

#### Application Recovery
```bash
#!/bin/bash
# restore-application.sh

# Stop services
sudo systemctl stop knowledge-service nginx

# Restore configuration
tar -xzf config-backup-*.tar.gz -C /opt/knowledge-service/

# Restore database (if needed)
# ./restore-database.sh backup-file.sql.gz

# Start services
sudo systemctl start nginx
sudo systemctl start knowledge-service

# Verify health
sleep 30
curl -f http://localhost:8283/actuator/health
```

## Troubleshooting

### Common Production Issues

#### 1. Application Won't Start
```bash
# Check service status
sudo systemctl status knowledge-service

# View logs
sudo journalctl -u knowledge-service -f

# Check configuration
java -jar app.jar --spring.boot.run.arguments=--debug

# Common causes:
# - Missing environment variables
# - Database connection issues
# - Port already in use
# - Invalid SSL certificates
```

#### 2. High Memory Usage
```bash
# Check JVM memory
jps -l
jstat -gc <pid>

# Adjust JVM settings
java -Xms1g -Xmx2g -jar app.jar

# Monitor heap dumps
java -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp app.jar
```

#### 3. Database Connection Issues
```bash
# Test database connectivity
psql -h $DB_HOST -U $DB_USERNAME -d knowledge_service -c "SELECT 1;"

# Check connection pool
curl http://localhost:8283/actuator/metrics/hikaricp.connections

# Adjust pool settings
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=20000
```

#### 4. Email Delivery Failures
```bash
# Test SMTP connectivity
telnet $SMTP_HOST $SMTP_PORT

# Check email logs
grep "mail" /var/log/knowledge-service/application.log

# Verify credentials
curl -v --url "smtps://$SMTP_HOST:465" --user "$SMTP_USERNAME:$SMTP_PASSWORD"
```

### Performance Optimization

#### JVM Tuning
```bash
# Production JVM settings
java -server \
  -Xms2g -Xmx4g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+UseStringDeduplication \
  -Djava.awt.headless=true \
  -Dfile.encoding=UTF-8 \
  -Duser.timezone=Asia/Ho_Chi_Minh \
  -jar app.jar
```

#### Database Optimization
```sql
-- Create indexes for performance
CREATE INDEX idx_topic_status ON topic(status);
CREATE INDEX idx_topic_last_processed ON topic(last_processed);
CREATE INDEX idx_knowledge_content_created_at ON knowledge_content(created_at);

-- Analyze query performance
EXPLAIN ANALYZE SELECT * FROM topic WHERE status = 'NEW';
```

### Monitoring Commands

```bash
# System monitoring
htop
iostat -x 1
netstat -tulpn | grep 8283

# Application monitoring
curl http://localhost:8283/actuator/health
curl http://localhost:8283/actuator/metrics
curl http://localhost:8283/api/topics/stats

# Log analysis
tail -f /var/log/knowledge-service/application.log
grep ERROR /var/log/knowledge-service/application.log | tail -20
```

---

**Last Updated**: August 27, 2025  
**Deployment Guide Version**: 1.0.0
