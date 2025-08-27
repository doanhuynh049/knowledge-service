# API Reference Documentation

## Overview

The Topic Knowledge Service provides REST APIs for managing and controlling the content generation process. All endpoints return JSON responses and follow RESTful conventions.

## Base URL

```
http://localhost:8283/api/topics
```

## Authentication

Currently, the API does not require authentication. In production, consider implementing:
- API Keys
- JWT tokens
- OAuth 2.0
- IP whitelisting

## Endpoints

### 1. Trigger Immediate Processing

**Endpoint**: `POST /api/topics/trigger`

**Description**: Manually triggers immediate processing of the next available topic from the Excel file.

**Request**:
```http
POST /api/topics/trigger
Host: localhost:8283
Content-Type: application/json
```

**Response**:
```json
{
  "status": "success",
  "message": "Topic processing triggered successfully. Check logs for details.",
  "timestamp": "2025-08-27T14:30:00.123Z"
}
```

**Error Response**:
```json
{
  "status": "error",
  "message": "Topic processing failed: No topics available for processing",
  "timestamp": "2025-08-27T14:30:00.123Z"
}
```

**Example**:
```bash
curl -X POST http://localhost:8283/api/topics/trigger \
  -H "Content-Type: application/json"
```

---

### 2. Process Specific Topics

**Endpoint**: `POST /api/topics/process`

**Description**: Process specific topics by providing their names in the request body.

**Request**:
```http
POST /api/topics/process
Host: localhost:8283
Content-Type: application/json

[
  "Lambda Expressions",
  "Streams API",
  "Optional & Null Safety"
]
```

**Response**:
```json
{
  "status": "success",
  "message": "Successfully processed 3 topics: Lambda Expressions, Streams API, Optional & Null Safety",
  "processedTopics": [
    {
      "name": "Lambda Expressions",
      "category": "Functional Programming",
      "status": "completed",
      "wordCount": 1250,
      "processingTime": "45 seconds"
    }
  ],
  "timestamp": "2025-08-27T14:30:00.123Z"
}
```

**Request Parameters**:
- **Body**: JSON array of topic names (strings)
- **Content-Type**: `application/json`

**Validation**:
- Topic names cannot be empty or null
- Maximum 10 topics per request
- Topic names must be valid strings

**Example**:
```bash
curl -X POST http://localhost:8283/api/topics/process \
  -H "Content-Type: application/json" \
  -d '["Lambda Expressions", "Streams API"]'
```

---

### 3. Add New Topics

**Endpoint**: `POST /api/topics/add`

**Description**: Add new topics to both the Excel file and database.

**Request**:
```http
POST /api/topics/add
Host: localhost:8283
Content-Type: application/json

[
  {
    "name": "Microservices Architecture",
    "category": "System Design"
  },
  {
    "name": "Docker Containers",
    "category": "DevOps"
  }
]
```

**Response**:
```json
{
  "status": "success",
  "message": "Successfully added 2 new topics",
  "addedTopics": [
    {
      "id": 15,
      "name": "Microservices Architecture",
      "category": "System Design",
      "status": "NEW",
      "createdAt": "2025-08-27T14:30:00.123Z"
    }
  ],
  "timestamp": "2025-08-27T14:30:00.123Z"
}
```

**Request Schema**:
```json
{
  "type": "array",
  "items": {
    "type": "object",
    "properties": {
      "name": {
        "type": "string",
        "minLength": 3,
        "maxLength": 100
      },
      "category": {
        "type": "string",
        "minLength": 3,
        "maxLength": 50
      }
    },
    "required": ["name", "category"]
  }
}
```

**Example**:
```bash
curl -X POST http://localhost:8283/api/topics/add \
  -H "Content-Type: application/json" \
  -d '[{"name": "Kubernetes", "category": "DevOps"}]'
```

---

### 4. Get Processing Statistics

**Endpoint**: `GET /api/topics/stats`

**Description**: Retrieve comprehensive statistics about topic processing.

**Request**:
```http
GET /api/topics/stats
Host: localhost:8283
```

**Response**:
```json
{
  "status": "success",
  "data": {
    "totalTopics": 45,
    "newTopics": 12,
    "processingTopics": 1,
    "completedTopics": 30,
    "errorTopics": 2,
    "todayProcessed": 1,
    "weeklyProcessed": 7,
    "monthlyProcessed": 28,
    "averageProcessingTime": "42 seconds",
    "averageWordCount": 1150,
    "lastProcessedTopic": {
      "name": "Lambda Expressions",
      "category": "Functional Programming",
      "processedAt": "2025-08-27T05:00:00.123Z",
      "wordCount": 1250
    },
    "topCategories": [
      {
        "category": "Functional Programming",
        "count": 8
      },
      {
        "category": "System Design",
        "count": 6
      }
    ],
    "upcomingTopics": [
      {
        "name": "Streams API",
        "category": "Functional Programming",
        "estimatedProcessing": "2025-08-28T05:00:00.123Z"
      }
    ]
  },
  "timestamp": "2025-08-27T14:30:00.123Z"
}
```

**Example**:
```bash
curl -X GET http://localhost:8283/api/topics/stats
```

---

### 5. Get Topic Details

**Endpoint**: `GET /api/topics/{id}`

**Description**: Get detailed information about a specific topic.

**Request**:
```http
GET /api/topics/15
Host: localhost:8283
```

**Response**:
```json
{
  "status": "success",
  "data": {
    "id": 15,
    "name": "Lambda Expressions",
    "category": "Functional Programming",
    "status": "DONE",
    "createdAt": "2025-08-20T10:00:00.123Z",
    "lastProcessed": "2025-08-27T05:00:00.123Z",
    "processingTimeSeconds": 45,
    "content": {
      "overviewWordCount": 450,
      "detailedWordCount": 1250,
      "emailSent": true,
      "emailSentAt": "2025-08-27T05:01:30.123Z"
    },
    "generationStats": {
      "attempts": 1,
      "successfulGenerations": 1,
      "lastError": null
    }
  },
  "timestamp": "2025-08-27T14:30:00.123Z"
}
```

---

### 6. Update Topic Status

**Endpoint**: `PUT /api/topics/{id}/status`

**Description**: Manually update the status of a topic.

**Request**:
```http
PUT /api/topics/15/status
Host: localhost:8283
Content-Type: application/json

{
  "status": "NEW",
  "reason": "Request for reprocessing"
}
```

**Response**:
```json
{
  "status": "success",
  "message": "Topic status updated successfully",
  "topic": {
    "id": 15,
    "name": "Lambda Expressions",
    "previousStatus": "DONE",
    "newStatus": "NEW",
    "updatedAt": "2025-08-27T14:30:00.123Z"
  },
  "timestamp": "2025-08-27T14:30:00.123Z"
}
```

**Valid Status Values**:
- `NEW`: Ready for processing
- `PROCESSING`: Currently being processed
- `DONE`: Successfully completed
- `ERROR`: Failed processing

---

## Status Codes

| Code | Description | Usage |
|------|-------------|-------|
| 200 | OK | Successful GET requests |
| 201 | Created | Successful POST requests |
| 400 | Bad Request | Invalid request parameters |
| 404 | Not Found | Topic not found |
| 500 | Internal Server Error | Server processing error |

## Error Responses

All error responses follow this format:

```json
{
  "status": "error",
  "error": {
    "code": "PROCESSING_FAILED",
    "message": "Detailed error description",
    "details": "Additional technical details",
    "suggestions": [
      "Check your API key configuration",
      "Verify Excel file format"
    ]
  },
  "timestamp": "2025-08-27T14:30:00.123Z",
  "path": "/api/topics/trigger"
}
```

### Common Error Codes

| Code | Description | Resolution |
|------|-------------|-----------|
| `INVALID_TOPIC_NAME` | Topic name is invalid | Provide valid topic name |
| `PROCESSING_FAILED` | Content generation failed | Check AI API configuration |
| `EMAIL_FAILED` | Email delivery failed | Verify SMTP settings |
| `EXCEL_READ_ERROR` | Cannot read Excel file | Check file format and location |
| `DATABASE_ERROR` | Database operation failed | Check database connection |

## Rate Limiting

Currently no rate limiting is implemented. For production:
- Implement per-IP rate limiting
- Add API key quotas
- Monitor usage patterns

## SDK Examples

### JavaScript/Node.js

```javascript
const axios = require('axios');

class TopicKnowledgeAPI {
  constructor(baseURL = 'http://localhost:8283/api/topics') {
    this.client = axios.create({ baseURL });
  }

  async triggerProcessing() {
    const response = await this.client.post('/trigger');
    return response.data;
  }

  async processTopics(topicNames) {
    const response = await this.client.post('/process', topicNames);
    return response.data;
  }

  async getStats() {
    const response = await this.client.get('/stats');
    return response.data;
  }

  async addTopics(topics) {
    const response = await this.client.post('/add', topics);
    return response.data;
  }
}

// Usage
const api = new TopicKnowledgeAPI();
api.triggerProcessing().then(console.log);
```

### Python

```python
import requests
import json

class TopicKnowledgeAPI:
    def __init__(self, base_url='http://localhost:8283/api/topics'):
        self.base_url = base_url

    def trigger_processing(self):
        response = requests.post(f'{self.base_url}/trigger')
        return response.json()

    def process_topics(self, topic_names):
        response = requests.post(
            f'{self.base_url}/process',
            json=topic_names,
            headers={'Content-Type': 'application/json'}
        )
        return response.json()

    def get_stats(self):
        response = requests.get(f'{self.base_url}/stats')
        return response.json()

    def add_topics(self, topics):
        response = requests.post(
            f'{self.base_url}/add',
            json=topics,
            headers={'Content-Type': 'application/json'}
        )
        return response.json()

# Usage
api = TopicKnowledgeAPI()
result = api.trigger_processing()
print(result)
```

### Java

```java
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

public class TopicKnowledgeAPI {
    private final RestTemplate restTemplate = new RestTemplate();
    private final String baseUrl;

    public TopicKnowledgeAPI(String baseUrl) {
        this.baseUrl = baseUrl + "/api/topics";
    }

    public ResponseEntity<String> triggerProcessing() {
        return restTemplate.postForEntity(baseUrl + "/trigger", null, String.class);
    }

    public ResponseEntity<String> processTopics(List<String> topicNames) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<List<String>> request = new HttpEntity<>(topicNames, headers);
        
        return restTemplate.postForEntity(baseUrl + "/process", request, String.class);
    }

    public ResponseEntity<String> getStats() {
        return restTemplate.getForEntity(baseUrl + "/stats", String.class);
    }
}
```

## Webhooks (Future Feature)

For real-time notifications, webhooks could be implemented:

```json
{
  "event": "topic.processed",
  "data": {
    "topicId": 15,
    "topicName": "Lambda Expressions",
    "status": "completed",
    "processingTime": 45,
    "emailSent": true
  },
  "timestamp": "2025-08-27T14:30:00.123Z"
}
```

## Testing

### Integration Tests

```bash
# Test all endpoints
mvn test -Dtest=TopicControllerIntegrationTest

# Test specific endpoint
curl -X POST http://localhost:8283/api/topics/trigger \
  -H "Content-Type: application/json" \
  -w "\nStatus: %{http_code}\nTime: %{time_total}s\n"
```

### Load Testing

```bash
# Using Apache Bench
ab -n 100 -c 10 http://localhost:8283/api/topics/stats

# Using curl for multiple requests
for i in {1..10}; do
  curl -X POST http://localhost:8283/api/topics/trigger &
done
wait
```

---

**Last Updated**: August 27, 2025  
**API Version**: 1.0.0
