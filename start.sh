#!/bin/bash

# Knowledge Service Startup Script

echo "🚀 Starting Daily Embedded Knowledge Service..."

# Set Java environment as requested
export JAVA_HOME=/opt/java-17
export PATH=$JAVA_HOME/bin:$PATH

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed. Please install Java 17 or higher."
    exit 1
fi

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | grep -oP 'version "([0-9]+)' | grep -oP '[0-9]+')
if [ "$JAVA_VERSION" -lt "17" ]; then
    echo "❌ Java 17 or higher is required. Current version: $JAVA_VERSION"
    exit 1
fi

echo "✅ Java version check passed"

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven is not installed. Please install Maven 3.6 or higher."
    exit 1
fi

echo "✅ Maven check passed"

# Load environment variables from .env file
if [ -f ".env" ]; then
    echo "✅ Loading environment variables from .env file"
    export $(cat .env | grep -v '^#' | xargs)
else
    echo "⚠️  .env file not found. Using default configuration."
    echo "   Please create .env file with your API keys and SMTP settings."
fi

# Create logs directory if it doesn't exist
mkdir -p logs
echo "✅ Logs directory created"

# Check required environment variables
if [ -z "$LLM_API_KEY" ] || [ "$LLM_API_KEY" = "your-gemini-api-key-here" ]; then
    echo "⚠️  LLM_API_KEY not configured. Please set your Gemini API key in .env file."
fi

if [ -z "$APP_API_KEY" ] || [ "$APP_API_KEY" = "your-secure-api-key-here" ]; then
    echo "⚠️  APP_API_KEY not configured. Please set a secure API key in .env file."
fi

# Build the application
echo "🔨 Building application..."
mvn clean compile -q

if [ $? -ne 0 ]; then
    echo "❌ Build failed. Please check for compilation errors."
    exit 1
fi

echo "✅ Build successful"

# Kill any existing process on port 8181 (default port from config)
echo "🔍 Checking for existing processes on port 8181..."
EXISTING_PID=$(lsof -ti:8181 2>/dev/null)
if [ ! -z "$EXISTING_PID" ]; then
    echo "⚠️  Found existing process(es) on port 8181: $EXISTING_PID"
    echo "🔪 Killing existing process(es)..."
    kill -9 $EXISTING_PID
    sleep 2
    echo "✅ Existing process(es) killed"
else
    echo "✅ No existing processes found on port 8181"
fi

# Start the application
echo "🎯 Starting Knowledge Service..."
echo "📊 The service will run daily at 6:00 AM (configurable per user)"
echo "🌐 API available at: http://localhost:8181/api/"
echo "❤️  Health check available at: http://localhost:8181/actuator/health"
echo "💾 H2 Console (dev mode): http://localhost:8181/h2-console"
echo ""
echo "Press Ctrl+C to stop the service"
echo "----------------------------------------"

mvn spring-boot:run
