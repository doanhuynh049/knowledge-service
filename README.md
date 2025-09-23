# Knowledge Service Platform

This Spring Boot application provides AI-powered knowledge generation and email delivery for educational content. It includes two specialized workflows: **Topic Knowledge Service** for general educational content and **Stock Knowledge Service** for financial education.

## 🚀 Features

### Topic Knowledge Service
- 📚 **Excel Topic Management** - Read topics from `topics.xlsx` with priority-based processing
- 🤖 **AI Content Generation** - Generate overview and detailed knowledge using Gemini API
- 📧 **Dual Email System** - Send both quick overview and comprehensive detailed emails
- ⏰ **Daily Scheduling** - Automated processing at 5:00 AM daily
- 📊 **Processing Stats** - Track generation metrics and system health
- 🔄 **Manual Triggers** - REST API for immediate processing

### Stock Knowledge Service (NEW)
- 📈 **Stock Topic Management** - Dedicated Excel file for financial education topics
- 💰 **Financial AI Content** - Specialized prompts for investment and trading education
- 📨 **Stock Email Templates** - Professional financial education email designs
- 🕕 **Independent Scheduling** - Daily processing at 6:00 AM, weekly reports on Sundays
- 📋 **Comprehensive Topics** - Pre-loaded with 10 essential stock market topics
- ⚡ **Batch Processing** - Process up to 5 stock topics per day

## 🏗️ Architecture

```
Knowledge Service Platform
├── Topic Knowledge Service (Original)
│   ├── Daily processing at 5:00 AM
│   ├── General educational content
│   └── Dual email format (overview + detailed)
└── Stock Knowledge Service (NEW)
    ├── Daily processing at 6:00 AM
    ├── Weekly analysis on Sundays at 7:00 AM
    ├── Financial education content
    └── Professional email templates
```

## 📋 Pre-loaded Stock Topics

The stock knowledge service comes with these essential topics:
1. Technical Analysis Fundamentals
2. Value Investing Principles
3. Risk Management in Trading
4. Market Psychology and Sentiment
5. Options Trading Strategies
6. Dividend Growth Investing
7. Economic Indicators Impact
8. Portfolio Diversification
9. Financial Statement Analysis
10. Cryptocurrency Investment

## 🚀 Quick Start

### 1. Environment Setup
```bash
# AI API Configuration
export GEMINI_API_KEY=your-gemini-api-key

# Email Configuration
export MAIL_FROM=your-email@gmail.com
export MAIL_TO=recipient@gmail.com
export SMTP_USER=your-email@gmail.com
export SMTP_PASS=your-app-password

# Optional: Excel file paths
export TOPICS_EXCEL_PATH=topics.xlsx
export STOCK_EXCEL_PATH=stock_knowledge_topics.xlsx
```

### 2. Run the Application
```bash
mvn clean compile
mvn spring-boot:run
```

### 3. Test Both Services
```bash
# Test Topic Knowledge Service
curl -X POST http://localhost:8283/api/topics/trigger

# Test Stock Knowledge Service
curl -X POST http://localhost:8283/api/stock/process/daily

# Check Stock Service Status
curl http://localhost:8283/api/stock/sta tus
```

## 📡 API Endpoints

### Topic Knowledge Service
- `POST /api/topics/trigger` - Manual processing trigger
- `POST /api/topics/process` - Process specific topics
- `POST /api/topics/add` - Add new topics
- `GET /api/topics/stats` - Processing statistics
- `GET /api/topics/list` - All topics
- `PUT /api/topics/reset/{name}` - Reset topic status

### Stock Knowledge Service (NEW)
- `POST /api/stock/process/daily` - Manual daily stock processing
- `POST /api/stock/process/weekly` - Manual weekly analysis
- `GET /api/stock/topics/unprocessed?limit=10` - Get unprocessed topics
- `GET /api/stock/topics/all` - Get all stock topics
- `GET /api/stock/status` - Service statistics and completion rate
- `GET /api/stock/health` - Health check

## ⚙️ Configuration

### Core Settings (`application.properties`)
```properties
# Server
server.port=8283

# AI API
app.gemini.api-key=${GEMINI_API_KEY}
app.gemini.url=https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent

# Email
app.email.from=${MAIL_FROM}
app.email.to=${MAIL_TO}
app.email.enabled=true

# Excel Files
app.topics-excel-path=${TOPICS_EXCEL_PATH:topics.xlsx}
app.stock-excel-path=${STOCK_EXCEL_PATH:stock_knowledge_topics.xlsx}

# Processing Limits
app.daily-topic-limit=1
app.daily-stock-limit=5

# Scheduling
app.schedule-enabled=true          # Topic service at 5:00 AM
app.stock-schedule-enabled=true    # Stock service at 6:00 AM
```

## 📊 Excel File Structure

### Topic Knowledge (`topics.xlsx`)
| Column | Description |
|--------|-------------|
| Topic Name | Educational topic title |
| Category | Subject category |
| Priority | Processing priority (1-5) |
| Last Processed | Timestamp of last processing |
| Status | NEW, PROCESSED, ERROR |
| Description | Topic description |

### Stock Knowledge (`stock_knowledge_topics.xlsx`)
| Column | Description |
|--------|-------------|
| Topic | Stock/finance topic title |
| Category | Financial category (Analysis, Strategy, Risk, etc.) |
| Priority | Processing priority (1-3) |
| Status | PENDING, PROCESSED, ERROR |
| Last Processed | Timestamp of last processing |
| Error Message | Error details if processing failed |

## 🕐 Scheduling

### Automatic Processing
- **Topic Knowledge**: Daily at 5:00 AM (Asia/Ho_Chi_Minh)
- **Stock Knowledge**: Daily at 6:00 AM (Asia/Ho_Chi_Minh)
- **Weekly Stock Report**: Sundays at 7:00 AM (Asia/Ho_Chi_Minh)
- **Health Checks**: 
  - Topic service: Every hour
  - Stock service: Every 2 hours

### Manual Processing
Both services support manual triggering via REST API for immediate processing.

## 📧 Email Templates

### Topic Knowledge Emails
- **Overview Email**: Quick summary with key points
- **Detailed Email**: Comprehensive educational content

### Stock Knowledge Emails
- **Daily Topic Email**: Professional financial education format
- **Weekly Report Email**: Comprehensive market analysis and action items

## 🔧 Advanced Usage

### Adding Custom Stock Topics
```bash
# The Excel file is automatically created with sample topics
# You can add more topics directly to stock_knowledge_topics.xlsx
# or programmatically via the Excel service
```

### Monitoring Processing
```bash
# Check overall service status
curl http://localhost:8283/api/stock/status

# View unprocessed topics
curl "http://localhost:8283/api/stock/topics/unprocessed?limit=5"

# Traditional topic service stats
curl http://localhost:8283/api/topics/stats
```

### Customizing Content Generation
The AI prompts are specifically tailored for each service:
- **Topic Service**: General educational content with overview/detailed structure
- **Stock Service**: Financial education with risk disclaimers and practical examples

## 🛠️ Development

### Technology Stack
- **Framework**: Spring Boot 3.2.0
- **Java Version**: 17
- **Database**: H2 (in-memory)
- **Excel Processing**: Apache POI 5.2.4
- **AI Integration**: Google Gemini API
- **Email**: Spring Mail with HTML templates

### Project Structure
```
src/main/java/com/knowledge/
├── topic/                    # Original topic knowledge service
│   ├── controller/
│   ├── service/
│   ├── scheduler/
│   └── model/
└── stock/                    # NEW: Stock knowledge service
    ├── controller/
    ├── service/
    ├── scheduler/
    └── (shares models with topic service)
```

## 📈 Workflow Comparison

| Feature | Topic Knowledge | Stock Knowledge |
|---------|----------------|-----------------|
| **Processing Time** | 5:00 AM daily | 6:00 AM daily + Sunday 7:00 AM |
| **Content Focus** | General education | Financial education |
| **Topics per Day** | 1 topic | Up to 5 topics |
| **Email Format** | Overview + Detailed | Professional financial |
| **Special Features** | Dual email system | Weekly market reports |
| **API Prefix** | `/api/topics/` | `/api/stock/` |

## 🚨 Important Notes

### Financial Disclaimer
All stock knowledge content includes appropriate financial disclaimers and educational warnings. The service is designed for educational purposes only and does not provide financial advice.

### Error Handling
Both services include comprehensive error handling:
- Fallback content when AI service is unavailable
- Excel file auto-creation with sample data
- Graceful degradation for email failures
- Detailed logging for troubleshooting

## 📚 Documentation

For detailed documentation, see the `/docs` folder:
- `API.md` - Complete API reference
- `CONFIGURATION.md` - Advanced configuration options
- `DEPLOYMENT.md` - Production deployment guide
- `DEVELOPMENT.md` - Development setup and guidelines

Built with Spring Boot 3.2.0, Java 17, and includes comprehensive error handling, logging, and professional email templates for both educational and financial content delivery.
