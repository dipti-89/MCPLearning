#!/bin/bash

# MCP Java Calculator Server Launcher Script
# This script ensures Java is found and the server starts correctly

# Set JAVA_HOME if not already set
if [ -z "$JAVA_HOME" ]; then
    export JAVA_HOME="/Users/I320519/SAPDevelop/sfsf/tools/sapjvm/Contents/Home"
fi

# Add Java to PATH
export PATH="$JAVA_HOME/bin:$PATH"

# Set the JAR file path
JAR_PATH="/Users/I320519/Documents/MCPJava/build/libs/demo-0.0.1-SNAPSHOT.jar"

# Ensure the JAR file exists
if [ ! -f "$JAR_PATH" ]; then
    echo "Error: JAR file not found at $JAR_PATH" >&2
    exit 1
fi

# Launch the MCP server
exec "$JAVA_HOME/bin/java" -jar "$JAR_PATH" "$@"