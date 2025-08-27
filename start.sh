#!/bin/bash

# Crypto Advisory Notifier Startup Script

echo "🚀 Starting Crypto Advisory Notifier..."

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed. Please install Java 17 or higher."
    exit 1
fi
export JAVA_HOME=/opt/java-17
export PATH=$JAVA_HOME/bin:$PATH
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

# Create logs directory if it doesn't exist
mkdir -p logs
echo "✅ Logs directory created"

# Check if topics.xlsx exists
if [ ! -f "topics.xlsx" ]; then
    echo "❌ topics.xlsx not found. Please create your topics configuration."
    exit 1
fi

echo "✅ Topics configuration found"

# Build the application
echo "🔨 Building application..."
mvn clean compile -q

if [ $? -ne 0 ]; then
    echo "❌ Build failed. Please check for compilation errors."
    exit 1
fi

echo "✅ Build successful"

# Kill any existing process on port 8283
echo "🔍 Checking for existing processes on port 8283..."
EXISTING_PID=$(lsof -ti:8283)
if [ ! -z "$EXISTING_PID" ]; then
    echo "⚠️  Found existing process(es) on port 8283: $EXISTING_PID"
    echo "🔪 Killing existing process(es)..."
    kill -9 $EXISTING_PID
    sleep 2
    echo "✅ Existing process(es) killed"
else
    echo "✅ No existing processes found on port 8283"
fi

# Start the application
echo "🎯 Starting Crypto Advisory Notifier..."
echo "📊 The service will run daily at 7:30 AM Asia/Ho_Chi_Minh"
echo "🌐 Manual trigger available at: http://localhost:8283/api/topics/trigger"
echo "❤️  Health check available at: http://localhost:8283/api/health"
echo ""
echo "Press Ctrl+C to stop the service"
echo "----------------------------------------"

mvn spring-boot:run
