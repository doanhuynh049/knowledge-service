in Vie# Knowledge Service Platform - API Commands

This document contains all available curl commands for both the **Topic Knowledge Service** and **Stock Knowledge Service** APIs.

## 🔧 Prerequisites

Make sure the application is running on port 8283:
```bash
mvn spring-boot:run
```

Base URL: `http://localhost:8283`

---

## 📚 Topic Knowledge Service Commands

### 🚀 Processing Commands

#### Trigger Manual Processing
```bash
# Trigger immediate topic processing
curl -X POST http://localhost:8283/api/topics/trigger
```

#### Process Specific Topics
```bash
# Process specific topics by name
curl -X POST http://localhost:8283/api/topics/process \
  -H "Content-Type: application/json" \
  -d '["Artificial Intelligence", "Climate Change"]'
```

#### Add New Topics
```bash
# Add new topics to the system
curl -X POST http://localhost:8283/api/topics/add \
  -H "Content-Type: application/json" \
  -d '[
    {
      "name": "Quantum Computing",
      "category": "Technology",
      "priority": 5,
      "description": "Introduction to quantum computing principles"
    },
    {
      "name": "Sustainable Energy",
      "category": "Environment",
      "priority": 4,
      "description": "Renewable energy sources and sustainability"
    }
  ]'
```

### 📊 Information & Statistics Commands

#### Get Processing Statistics
```bash
# Get comprehensive processing statistics
curl -s http://localhost:8283/api/topics/stats | python3 -m json.tool
```

#### List All Topics
```bash
# Get all topics with their status
curl -s http://localhost:8283/api/topics/list | python3 -m json.tool
```

#### Get Topics by Status
```bash
# Get topics by specific status (NEW, PROCESSING, DONE, ERROR, ARCHIVED)
curl -s http://localhost:8283/api/topics/status/NEW | python3 -m json.tool
curl -s http://localhost:8283/api/topics/status/DONE | python3 -m json.tool
curl -s http://localhost:8283/api/topics/status/ERROR | python3 -m json.tool
```

#### Get Recently Processed Topics
```bash
# Get topics processed in the last N days
curl -s http://localhost:8283/api/topics/recent/7 | python3 -m json.tool   # Last 7 days
curl -s http://localhost:8283/api/topics/recent/30 | python3 -m json.tool  # Last 30 days
```

### 🔄 Management Commands

#### Reset Topic Status
```bash
# Reset a specific topic's status to allow reprocessing
curl -X PUT http://localhost:8283/api/topics/reset/Artificial%20Intelligence
curl -X PUT http://localhost:8283/api/topics/reset/Climate%20Change
```

---

## 📈 Stock Knowledge Service Commands

### 🚀 Processing Commands

#### Trigger Daily Stock Processing
```bash
# Manually trigger daily stock knowledge processing
curl -X POST http://localhost:8283/api/stock/process/daily
```

#### Trigger Weekly Stock Analysis
```bash
# Manually trigger weekly comprehensive stock analysis
curl -X POST http://localhost:8283/api/stock/process/weekly
```

### 📊 Information & Statistics Commands

#### Get Service Status
```bash
# Get comprehensive stock service statistics
curl -s http://localhost:8283/api/stock/status | python3 -m json.tool
```

#### Health Check
```bash
# Check if stock service is running
curl -s http://localhost:8283/api/stock/health | python3 -m json.tool
```

#### Get All Stock Topics
```bash
# Get all available stock topics
curl -s http://localhost:8283/api/stock/topics/all | python3 -m json.tool
```

#### Get Unprocessed Stock Topics
```bash
# Get unprocessed topics (default limit: 10)
curl -s http://localhost:8283/api/stock/topics/unprocessed | python3 -m json.tool

# Get unprocessed topics with custom limit
curl -s "http://localhost:8283/api/stock/topics/unprocessed?limit=5" | python3 -m json.tool
curl -s "http://localhost:8283/api/stock/topics/unprocessed?limit=20" | python3 -m json.tool
```

---

## 🔍 Monitoring & Debugging Commands

### Quick Health Check for Both Services
```bash
# Check both services are running
echo "=== Topic Service Stats ==="
curl -s http://localhost:8283/api/topics/stats | python3 -m json.tool

echo -e "\n=== Stock Service Status ==="
curl -s http://localhost:8283/api/stock/status | python3 -m json.tool

echo -e "\n=== Stock Service Health ==="
curl -s http://localhost:8283/api/stock/health | python3 -m json.tool
```

### View Current Processing Queue
```bash
# Check what's pending in both services
echo "=== Pending Topic Processing ==="
curl -s http://localhost:8283/api/topics/status/NEW | python3 -m json.tool

echo -e "\n=== Pending Stock Processing ==="
curl -s "http://localhost:8283/api/stock/topics/unprocessed?limit=10" | python3 -m json.tool
```

### Service Comparison
```bash
# Compare both services' current state
echo "=== Topic Knowledge Service ==="
echo "Total Topics:"
curl -s http://localhost:8283/api/topics/list | jq length

echo "Processed Topics:"
curl -s http://localhost:8283/api/topics/status/DONE | jq length

echo -e "\n=== Stock Knowledge Service ==="
curl -s http://localhost:8283/api/stock/status | jq '.totalTopics, .processedTopics, .completionRate'
```

---

## 🚀 Quick Testing Scenarios

### Test Complete Topic Workflow
```bash
# 1. Add a new topic
curl -X POST http://localhost:8283/api/topics/add \
  -H "Content-Type: application/json" \
  -d '[{"name": "Test Topic", "category": "Testing", "priority": 5, "description": "Test topic for workflow"}]'

# 2. Process the topic
curl -X POST http://localhost:8283/api/topics/process \
  -H "Content-Type: application/json" \
  -d '["Test Topic"]'

# 3. Check status
curl -s http://localhost:8283/api/topics/stats | python3 -m json.tool
```

### Test Stock Service Workflow
```bash
# 1. Check available topics
curl -s http://localhost:8283/api/stock/topics/unprocessed | python3 -m json.tool

# 2. Process daily topics
curl -X POST http://localhost:8283/api/stock/process/daily

# 3. Check completion status
curl -s http://localhost:8283/api/stock/status | python3 -m json.tool
```

### Force Processing Reset (Topic Service)
```bash
# Reset a topic that had errors
curl -X PUT http://localhost:8283/api/topics/reset/Technical%20Analysis%20Fundamentals

# Then reprocess it
curl -X POST http://localhost:8283/api/topics/process \
  -H "Content-Type: application/json" \
  -d '["Technical Analysis Fundamentals"]'
```

---

## 📋 Pre-configured Stock Topics

The stock service comes with these 10 pre-loaded topics that you can reference:

1. **Technical Analysis Fundamentals**
2. **Value Investing Principles**
3. **Risk Management in Trading**
4. **Market Psychology and Sentiment**
5. **Options Trading Strategies**
6. **Dividend Growth Investing**
7. **Economic Indicators Impact**
8. **Portfolio Diversification**
9. **Financial Statement Analysis**
10. **Cryptocurrency Investment**

---

## 🛠️ Advanced Usage

### Batch Processing Multiple Topics
```bash
# Process multiple specific topics at once
curl -X POST http://localhost:8283/api/topics/process \
  -H "Content-Type: application/json" \
  -d '[
    "Artificial Intelligence",
    "Machine Learning",
    "Data Science",
    "Cloud Computing"
  ]'
```

### Monitor Processing Over Time
```bash
# Create a monitoring script
echo '#!/bin/bash
while true; do
  echo "=== $(date) ==="
  echo "Topic Stats:"
  curl -s http://localhost:8283/api/topics/stats | jq ".totalTopics, .processedTopics, .newTopics"
  echo "Stock Stats:"
  curl -s http://localhost:8283/api/stock/status | jq ".totalTopics, .processedTopics, .completionRate"
  echo "========================"
  sleep 60
done' > monitor.sh && chmod +x monitor.sh && ./monitor.sh
```

### Error Investigation
```bash
# Check for topics with errors
curl -s http://localhost:8283/api/topics/status/ERROR | python3 -m json.tool

# Check recent processing (last 24 hours)
curl -s http://localhost:8283/api/topics/recent/1 | python3 -m json.tool
```

---

## 📝 Response Format Examples

### Topic Service Responses
```json
// GET /api/topics/stats
{
  "totalTopics": 25,
  "newTopics": 5,
  "processedTopics": 18,
  "errorTopics": 2,
  "archivedTopics": 0,
  "processingTopics": 0,
  "totalContentGenerated": 18,
  "pendingEmailContent": 0,
  "averageOverviewWords": 250.5,
  "averageDetailedWords": 1200.3,
  "contentGeneratedToday": 3,
  "lastUpdated": "2025-09-23T10:30:00"
}
```

### Stock Service Responses
```json
// GET /api/stock/status
{
  "success": true,
  "totalTopics": 10,
  "unprocessedTopics": 7,
  "processedTopics": 3,
  "completionRate": 30.0,
  "timestamp": 1695456789000
}

// GET /api/stock/topics/unprocessed
{
  "success": true,
  "topics": [
    "Technical Analysis Fundamentals",
    "Value Investing Principles",
    "Risk Management in Trading"
  ],
  "count": 3,
  "timestamp": 1695456789000
}
```

---

## 🚨 Common Error Responses

```json
// Error Response Format
{
  "success": false,
  "message": "Error description here",
  "timestamp": 1695456789000
}
```

```bash
# Handle errors in scripts
response=$(curl -s http://localhost:8283/api/stock/process/daily)
if echo "$response" | jq -e '.success == false' > /dev/null; then
  echo "Error: $(echo "$response" | jq -r '.message')"
else
  echo "Success: $(echo "$response" | jq -r '.message')"
fi
```

---

## 📚 Enhanced Stock Learning Service Commands (NEW)

The enhanced stock learning service provides a structured 20-day curriculum with comprehensive logging and progress tracking.

### 🚀 Learning Processing Commands

#### Process Today's Learning
```bash
# Process the next available learning day
curl -X POST http://localhost:8283/api/enhanced-stock/learn/today
```

#### Process Specific Learning Day
```bash
# Process a specific day by number (1-20)
curl -X POST http://localhost:8283/api/enhanced-stock/learn/day/1
curl -X POST http://localhost:8283/api/enhanced-stock/learn/day/5
curl -X POST http://localhost:8283/api/enhanced-stock/learn/day/15
```

#### Reset Learning Day for Reprocessing
```bash
# Reset a specific day back to OPEN status
curl -X PUT http://localhost:8283/api/enhanced-stock/learn/day/1/reset
curl -X PUT http://localhost:8283/api/enhanced-stock/learn/day/10/reset
```

### 📊 Learning Progress & Statistics Commands

#### Get Learning Progress
```bash
# Get comprehensive learning progress statistics
curl -s http://localhost:8283/api/enhanced-stock/progress | python3 -m json.tool
```

#### Get Complete Curriculum
```bash
# View all 20 learning days with their status
curl -s http://localhost:8283/api/enhanced-stock/curriculum | python3 -m json.tool
```

#### Get Next Learning Day
```bash
# See what's coming up next
curl -s http://localhost:8283/api/enhanced-stock/next | python3 -m json.tool
```

#### Get Upcoming Days Preview
```bash
# Preview next 5 upcoming days (default)
curl -s http://localhost:8283/api/enhanced-stock/upcoming | python3 -m json.tool

# Preview next 10 upcoming days
curl -s "http://localhost:8283/api/enhanced-stock/upcoming?count=10" | python3 -m json.tool

# Preview next 3 upcoming days
curl -s "http://localhost:8283/api/enhanced-stock/upcoming?count=3" | python3 -m json.tool
```

### 📖 Learning Phase Commands

#### Get Days by Learning Phase
```bash
# Get all Foundation phase days (Week 1)
curl -s http://localhost:8283/api/enhanced-stock/phase/Foundations | python3 -m json.tool

# Get all Analysis phase days (Week 2)
curl -s http://localhost:8283/api/enhanced-stock/phase/Analysis | python3 -m json.tool

# Get all Technical phase days (Week 3)
curl -s http://localhost:8283/api/enhanced-stock/phase/Technical | python3 -m json.tool

# Get all Strategy phase days (Week 4)
curl -s http://localhost:8283/api/enhanced-stock/phase/Strategy | python3 -m json.tool
```

### 🏥 Enhanced Service Health Check
```bash
# Check enhanced stock learning service status
curl -s http://localhost:8283/api/enhanced-stock/health | python3 -m json.tool
```

---

## 🔍 Enhanced Monitoring & Debugging Commands

### Complete System Status Check
```bash
# Check all services at once
echo "=== Original Topic Service ==="
curl -s http://localhost:8283/api/topics/stats | python3 -m json.tool

echo -e "\n=== Original Stock Service ==="
curl -s http://localhost:8283/api/stock/status | python3 -m json.tool

echo -e "\n=== Enhanced Stock Learning Service ==="
curl -s http://localhost:8283/api/enhanced-stock/progress | python3 -m json.tool

echo -e "\n=== Enhanced Service Health ==="
curl -s http://localhost:8283/api/enhanced-stock/health | python3 -m json.tool
```

### Learning Progress Dashboard
```bash
# Create a comprehensive learning dashboard
echo "=== STOCK LEARNING DASHBOARD ==="
echo "Current Progress:"
curl -s http://localhost:8283/api/enhanced-stock/progress | jq '.completedDays, .totalDays, .completionRate'

echo -e "\nNext Learning Day:"
curl -s http://localhost:8283/api/enhanced-stock/next | jq '.nextDay.day, .nextDay.topic, .nextDay.phase'

echo -e "\nUpcoming This Week:"
curl -s "http://localhost:8283/api/enhanced-stock/upcoming?count=5" | jq '.upcomingDays[] | "Day \(.day): \(.topic)"'
```

---

## 🚀 Enhanced Testing Scenarios

### Complete Enhanced Learning Workflow Test
```bash
# 1. Check current progress
echo "=== Initial Progress ==="
curl -s http://localhost:8283/api/enhanced-stock/progress | python3 -m json.tool

# 2. See what's next
echo -e "\n=== Next Learning Day ==="
curl -s http://localhost:8283/api/enhanced-stock/next | python3 -m json.tool

# 3. Process today's learning
echo -e "\n=== Processing Today's Learning ==="
curl -X POST http://localhost:8283/api/enhanced-stock/learn/today

# 4. Check updated progress
echo -e "\n=== Updated Progress ==="
curl -s http://localhost:8283/api/enhanced-stock/progress | python3 -m json.tool
```

### Test Specific Day Processing
```bash
# Process Day 1 (Stock Basics)
curl -X POST http://localhost:8283/api/enhanced-stock/learn/day/1

# Check what Day 1 covers
curl -s http://localhost:8283/api/enhanced-stock/curriculum | jq '.curriculum[] | select(.day == 1)'

# Process Day 5 (Order Types)
curl -X POST http://localhost:8283/api/enhanced-stock/learn/day/5
```

### Test Phase-Based Learning
```bash
# See all Foundation topics (Week 1)
curl -s http://localhost:8283/api/enhanced-stock/phase/Foundations | jq '.days[] | "Day \(.day): \(.topic)"'

# See all Technical Analysis topics (Week 3)  
curl -s http://localhost:8283/api/enhanced-stock/phase/Technical | jq '.days[] | "Day \(.day): \(.topic)"'
```

### Reset and Reprocess Workflow
```bash
# Reset Day 3 for reprocessing
curl -X PUT http://localhost:8283/api/enhanced-stock/learn/day/3/reset

# Verify it's back to OPEN status
curl -s http://localhost:8283/api/enhanced-stock/curriculum | jq '.curriculum[] | select(.day == 3) | .status'

# Reprocess Day 3
curl -X POST http://localhost:8283/api/enhanced-stock/learn/day/3
```

---

## 📋 Enhanced Curriculum Overview

The enhanced stock learning service includes a **20-day structured curriculum**:

### **Week 1 - Foundations (Days 1-5)**
1. Stock basics: What is a stock?
2. Exchanges & indices (VN-Index, S&P500, Nasdaq)
3. Types of stocks: growth, value, dividend
4. How stock trading works (brokers, clearing)
5. Order types: market, limit, stop-loss

### **Week 2 - Analysis (Days 6-10)**
6. Reading financial statements basics
7. Key financial ratios (P/E, ROE, Debt-to-Equity)
8. Revenue and profit analysis
9. Cash flow statement analysis
10. Industry and competitor analysis

### **Week 3 - Technical Analysis (Days 11-15)**
11. Chart reading basics: candlesticks, trends
12. Support and resistance levels
13. Moving averages and volume analysis
14. Technical indicators: RSI, MACD, Bollinger Bands
15. Chart patterns: triangles, flags, head & shoulders

### **Week 4 - Risk & Strategy (Days 16-20)**
16. Portfolio diversification principles
17. Risk management and position sizing
18. Value investing vs growth investing
19. Dollar-cost averaging and timing strategies
20. Market psychology and behavioral finance

---

## 📝 Enhanced Response Format Examples

### Enhanced Learning Progress Response
```json
// GET /api/enhanced-stock/progress
{
  "success": true,
  "totalDays": 20,
  "completedDays": 5,
  "errorDays": 0,
  "openDays": 15,
  "completionRate": 25.0,
  "timestamp": 1695456789000
}
```

### Next Learning Day Response
```json
// GET /api/enhanced-stock/next
{
  "success": true,
  "nextDay": {
    "day": 6,
    "week": "Week 2",
    "phase": "Analysis",
    "topic": "Reading financial statements basics",
    "learningGoal": "Understand and apply: Reading financial statements basics",
    "emailSubject": "📈 Daily Stock Knowledge – Reading financial statements basics",
    "practiceTask": "Study 20m + Apply 15m. Analyze 1 VN and 1 US ticker using today's topic; write 3 bullet insights and a decision.",
    "status": "OPEN"
  },
  "hasNext": true,
  "timestamp": 1695456789000
}
```

### Upcoming Days Response
```json
// GET /api/enhanced-stock/upcoming?count=3
{
  "success": true,
  "upcomingDays": [
    {
      "day": 6,
      "topic": "Reading financial statements basics",
      "phase": "Analysis"
    },
    {
      "day": 7, 
      "topic": "Key financial ratios (P/E, ROE, Debt-to-Equity)",
      "phase": "Analysis"
    },
    {
      "day": 8,
      "topic": "Revenue and profit analysis", 
      "phase": "Analysis"
    }
  ],
  "count": 3,
  "timestamp": 1695456789000
}
```

---
