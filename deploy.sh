#!/bin/bash

# Configuration
SERVER_IP="67.211.210.15"
USER="root"
JAR_NAME="erp-0.0.1-SNAPSHOT.jar"
LOCAL_PATH="target/$JAR_NAME"
REMOTE_PATH="/root/"

echo "--- 🚀 Starting Deployment to $SERVER_IP ---"

# 1. Stop old app
echo "1. Stopping old application..."
ssh $USER@$SERVER_IP "pkill -f $JAR_NAME || true"

# 2. Upload new jar
echo "2. Uploading new JAR file..."
if [ -f "$LOCAL_PATH" ]; then
    scp $LOCAL_PATH $USER@$SERVER_IP:$REMOTE_PATH
else
    echo "❌ Error: File $LOCAL_PATH not found! Are you in the project root?"
    exit 1
fi

# 3. Start new app
echo "3. Starting new application..."
ssh $USER@$SERVER_IP "nohup java -jar $REMOTE_PATH$JAR_NAME > app.log 2>&1 &"

echo "--- ✅ Deployment Complete! ---"
