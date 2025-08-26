# Topic Knowledge Service

This Spring Boot application reads topics from Excel files, generates AI-powered knowledge content using Google Gemini API, and sends dual emails (overview + detailed) for enhanced learning.

## Features

- 📚 **Excel Topic Management** - Read topics from `topics.xlsx` with priority-based processing
- 🤖 **AI Content Generation** - Generate overview and detailed knowledge using Gemini API
- 📧 **Dual Email System** - Send both quick overview and comprehensive detailed emails
- ⏰ **Daily Scheduling** - Automated processing at 5:00 AM daily
- 📊 **Processing Stats** - Track generation metrics and system health
- 🔄 **Manual Triggers** - REST API for immediate processing

## Quick Start

1. **Set Environment Variables:**
   ```bash
   export GEMINI_API_KEY=your-gemini-api-key
   export MAIL_FROM=your-email@gmail.com
   export MAIL_TO=recipient@gmail.com
   export SMTP_USER=your-email@gmail.com
   export SMTP_PASS=your-app-password
   ```

2. **Run the Application:**
   ```bash
   mvn spring-boot:run
   ```

3. **Test Manual Processing:**
   ```bash
   curl -X POST http://localhost:8283/api/topics/trigger
   ```

curl -X POST http://localhost:8283/api/topics/add -H "Content-Type: application/json" -d '[
{
"name": "Artificial Intelligence",
"category": "Technology",
"priority": 5,
"description": "Modern AI systems and machine learning applications"
},
{
"name": "Climate Change",
"category": "Science",
"priority": 5,
"description": "Global warming and environmental impact"
}
]'

curl -s http://localhost:8283/api/topics/stats | python3 -m json.tool

## API Endpoints

- `POST /api/topics/trigger` - Manual processing trigger
- `POST /api/topics/process` - Process specific topics
- `POST /api/topics/add` - Add new topics
- `GET /api/topics/stats` - Processing statistics
- `GET /api/topics/list` - All topics
- `PUT /api/topics/reset/{name}` - Reset topic status

## Configuration

Key settings in `application.properties`:
- `app.daily-topic-limit=1` - Topics per day
- `app.reprocess-after-days=30` - Reprocessing interval
- `app.schedule-enabled=true` - Enable/disable scheduling

## Excel Files

- `topics.xlsx` - Input topics with columns: Topic Name, Category, Priority, Last Processed, Status, Description
- `knowledge_log.xlsx` - Processing history and metrics

## Architecture

The application follows a layered architecture:
- **Controllers** - REST API endpoints
- **Services** - Business logic and orchestration
- **Repositories** - Data access layer
- **Schedulers** - Automated processing
- **DTOs** - Data transfer objects

Built with Spring Boot 3.2.0, Java 17, and includes comprehensive error handling and logging.
