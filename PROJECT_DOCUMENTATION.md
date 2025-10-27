# MCP Java Calculator Server - Complete Project Documentation

## Table of Contents
1. [Project Overview](#project-overview)
2. [Architecture](#architecture)
3. [Project Structure](#project-structure)
4. [Complete Data Flow](#complete-data-flow)
5. [Component Details](#component-details)
6. [Example Request Flow](#example-request-flow)
7. [Key Design Decisions](#key-design-decisions)
8. [Setup and Configuration](#setup-and-configuration)
9. [Testing](#testing)

## Project Overview

This project implements a **Java-based MCP (Model Context Protocol) server** that integrates with Claude Desktop, providing calculator tools that Claude can use during conversations. The project uses Spring Boot and implements the MCP protocol for communication over stdin/stdout.

### What is MCP?
The Model Context Protocol (MCP) is a standard that allows AI assistants like Claude to connect to external tools and data sources. Our implementation creates a bridge between Claude Desktop and custom Java functionality.

### Features
- ✅ Four basic mathematical operations (add, subtract, multiply, divide)
- ✅ Full MCP protocol compliance
- ✅ Spring Boot integration
- ✅ Stdio-based communication with Claude Desktop
- ✅ Proper error handling and logging
- ✅ JSON request/response processing

## Architecture

```
┌─────────────────┐    JSON over     ┌──────────────────────┐
│   Claude        │    stdin/stdout  │   MCP Java Server    │
│   Desktop       │ ◄──────────────► │                      │
└─────────────────┘                  └──────────────────────┘
                                               │
                                               ▼
                                     ┌──────────────────┐
                                     │  Spring Boot     │
                                     │  Application     │
                                     └──────────────────┘
                                               │
                          ┌────────────────────┼────────────────────┐
                          │                    │                    │
                          ▼                    ▼                    ▼
                   ┌─────────────┐    ┌──────────────┐    ┌──────────────┐
                   │ MCP Request │    │ MCP Service  │    │ Tool Service │
                   │ Handler     │    │              │    │              │
                   └─────────────┘    └──────────────┘    └──────────────┘
```

## Project Structure

```
MCPJava/
├── src/main/java/com/example/demo/
│   ├── McpDemoApplication.java      # Spring Boot entry point
│   ├── MCPRequestHandler.java       # Handles stdio communication
│   ├── MCPService.java             # MCP protocol implementation
│   └── ToolService.java            # Calculator tool logic
├── build.gradle                    # Build configuration
├── mcp-launcher.sh                 # Launch script for Claude Desktop
├── build/libs/demo-0.0.1-SNAPSHOT.jar  # Compiled JAR
└── PROJECT_DOCUMENTATION.md        # This documentation
```

## Complete Data Flow

### Overview
```
Claude Desktop → reads config → launches Java process → MCP handshake → tool requests → responses → Claude Desktop
```

### Detailed Flow
1. **Client Setup & Connection**
   ```
   Claude Desktop → reads Claude config → finds mcp-launcher.sh → launches Java process
   ```

2. **Server Initialization**
   - Launch script sets up Java environment
   - Spring Boot starts with disabled web server
   - MCP request handler begins listening on stdin

3. **MCP Protocol Handshake**
   - Claude sends `initialize` request
   - Server responds with capabilities and protocol version
   - Claude sends `initialized` notification

4. **Tool Discovery**
   - Claude sends `tools/list` request
   - Server responds with available calculator tools

5. **Tool Execution**
   - Claude sends `tools/call` request with tool name and arguments
   - Server executes calculation and returns result

## Component Details

### 1. McpDemoApplication.java
**Purpose**: Spring Boot entry point that configures the application for MCP communication.

**Key Features**:
- Disables web server (uses `WebApplicationType.NONE`)
- Disables Spring banner to keep stdout clean
- Starts MCP request handler via CommandLineRunner

**Code Highlights**:
```java
@SpringBootApplication
public class McpDemoApplication {
    public static void main(String[] args) {
        System.setProperty("logging.level.root", "ERROR");
        SpringApplication app = new SpringApplication(McpDemoApplication.class);
        app.setBannerMode(Banner.Mode.OFF);
        app.setWebApplicationType(WebApplicationType.NONE);
        app.run(args);
    }

    @Bean
    public CommandLineRunner runner(MCPRequestHandler handler, Environment env) {
        return args -> handler.start();
    }
}
```

### 2. MCPRequestHandler.java
**Purpose**: Manages stdio-based communication between Claude Desktop and our MCP service.

**Key Features**:
- Reads JSON requests from stdin
- Writes JSON responses to stdout
- Routes all logging to stderr
- Handles JSON parsing and error recovery

**Communication Flow**:
```java
public void start() {
    try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(System.in, StandardCharsets.UTF_8))) {
        
        PrintWriter writer = new PrintWriter(System.out, true);
        System.err.println("MCP Server started and listening for requests...");

        String line;
        while ((line = reader.readLine()) != null) {
            handleRequest(line.trim(), writer);
        }
    }
}
```

### 3. MCPService.java
**Purpose**: Implements the complete MCP protocol specification.

**Key Features**:
- Handles all MCP method types (initialize, tools/list, tools/call, etc.)
- Manages protocol version negotiation
- Provides server capabilities declaration
- Routes tool execution requests

**MCP Methods Implemented**:
- `initialize`: Server setup and capability exchange
- `tools/list`: Returns available calculator tools
- `tools/call`: Executes specific calculator operations
- `prompts/list`: Returns empty list (not used in this implementation)
- `resources/list`: Returns empty list (not used in this implementation)
- `notifications/initialized`: Acknowledges successful initialization

**Tool List Response**:
```json
{
  "tools": [
    {
      "name": "add",
      "description": "Add two numbers",
      "inputSchema": {
        "type": "object",
        "properties": {
          "a": {"type": "number", "description": "First number"},
          "b": {"type": "number", "description": "Second number"}
        },
        "required": ["a", "b"]
      }
    }
    // ... subtract, multiply, divide tools
  ]
}
```

### 4. ToolService.java
**Purpose**: Implements the actual calculator functionality.

**Key Features**:
- Four mathematical operations: add, subtract, multiply, divide
- JSON argument parsing
- Formatted result output
- Debug logging to stderr

**Tool Execution Example**:
```java
private String executeAdd(JsonNode arguments) {
    double a = arguments.get("a").asDouble();
    double b = arguments.get("b").asDouble();
    double result = a + b;
    
    System.err.println("Addition: " + a + " + " + b + " = " + result);
    return String.format("Result: %.2f", result);
}
```

### 5. mcp-launcher.sh
**Purpose**: Shell script that sets up the Java environment and launches the server.

**Key Features**:
- Sets JAVA_HOME if not configured
- Launches the JAR file with proper classpath
- Ensures stderr logging doesn't interfere with MCP communication

```bash
#!/bin/bash
if [ -z "$JAVA_HOME" ]; then
    export JAVA_HOME="/Users/I320519/SAPDevelop/sfsf/tools/sapjvm/Contents/Home"
fi

JAR_PATH="/Users/I320519/Documents/MCPJava/build/libs/demo-0.0.1-SNAPSHOT.jar"

if [ ! -f "$JAR_PATH" ]; then
    echo "Error: JAR file not found at $JAR_PATH" >&2
    exit 1
fi

exec "$JAVA_HOME/bin/java" -jar "$JAR_PATH" 2>/dev/null
```

## Example Request Flow

Let's trace through a complete request when someone asks Claude to "add 15 and 25":

### Step 1: Claude Desktop sends JSON request
```json
{
  "method": "tools/call",
  "params": {
    "name": "add",
    "arguments": {"a": 15, "b": 25}
  },
  "jsonrpc": "2.0",
  "id": 1
}
```

### Step 2: MCPRequestHandler receives it
- Reads JSON from stdin
- Parses using Jackson ObjectMapper
- Logs: "Received request: method=tools/call"

### Step 3: MCPService routes the request
- Identifies method as "tools/call"
- Calls `handleToolsCall()` method
- Extracts tool name "add" and arguments

### Step 4: ToolService executes the calculation
```java
// In executeAdd method:
double a = 15.0;  // from arguments.get("a").asDouble()
double b = 25.0;  // from arguments.get("b").asDouble()
double result = 40.0;  // a + b

System.err.println("Addition: 15.0 + 25.0 = 40.0");
return "Result: 40.00";  // formatted result
```

### Step 5: Response travels back up the chain
- ToolService returns "Result: 40.00"
- MCPService wraps it in proper MCP response format
- MCPRequestHandler sends JSON response to stdout

### Step 6: Claude Desktop receives response
```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "result": {
    "content": [
      {"type": "text", "text": "Result: 40.00"}
    ]
  }
}
```

### Step 7: Claude shows the result to user
Claude Desktop processes the response and displays: "I calculated 15 + 25 = 40.00"

## Key Design Decisions

### 1. Stdio Communication
**Decision**: Use stdin/stdout for communication instead of HTTP
**Reason**: MCP protocol requires pure JSON over stdin/stdout. HTTP would not be compatible with Claude Desktop's expectations.

### 2. Error Stream Separation
**Decision**: Route all logging to stderr
**Reason**: stdout must remain clean for JSON communication. Any debug output on stdout would break the MCP protocol.

### 3. Spring Boot Configuration
**Decision**: Disable web server and banner
**Reason**: 
- Web server is unnecessary for stdio communication
- Banner output would interfere with JSON protocol
- Reduces startup time and resource usage

### 4. Protocol Compliance
**Decision**: Implement all required MCP methods
**Reason**: Claude Desktop expects complete protocol implementation. Missing methods would cause connection failures.

### 5. Tool Architecture
**Decision**: Separate concerns across multiple classes
**Reason**: 
- `MCPRequestHandler`: Pure I/O handling
- `MCPService`: Protocol implementation
- `ToolService`: Business logic
- This separation makes the code maintainable and testable

### 6. JSON Processing
**Decision**: Use Jackson ObjectMapper for JSON handling
**Reason**: Robust, well-tested library that handles edge cases and provides clean API for JSON manipulation.

## Setup and Configuration

### Prerequisites
- Java 17 or higher
- Gradle 8.x
- Claude Desktop application

### Build Process
```bash
# Clean and build the project
./gradlew clean build

# The JAR will be created at:
# build/libs/demo-0.0.1-SNAPSHOT.jar
```

### Claude Desktop Configuration
Add to Claude Desktop's configuration file:
```json
{
  "mcpServers": {
    "mcp-java-demo": {
      "command": "/Users/I320519/Documents/MCPJava/mcp-launcher.sh"
    }
  }
}
```

### Dependencies (build.gradle)
```gradle
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter'
    implementation 'com.fasterxml.jackson.core:jackson-databind'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}
```

## Testing

### Manual Testing
Test individual tools using command line:
```bash
# Test addition
echo '{"method":"tools/call","params":{"name":"add","arguments":{"a":15,"b":25}},"jsonrpc":"2.0","id":1}' | ./mcp-launcher.sh

# Test all operations
(
echo '{"method":"tools/call","params":{"name":"add","arguments":{"a":15,"b":25}},"jsonrpc":"2.0","id":1}'
echo '{"method":"tools/call","params":{"name":"subtract","arguments":{"a":100,"b":30}},"jsonrpc":"2.0","id":2}'
echo '{"method":"tools/call","params":{"name":"multiply","arguments":{"a":7,"b":8}},"jsonrpc":"2.0","id":3}'
echo '{"method":"tools/call","params":{"name":"divide","arguments":{"a":84,"b":12}},"jsonrpc":"2.0","id":4}'
) | ./mcp-launcher.sh
```

### Expected Outputs
- **Add**: 15 + 25 = 40.00
- **Subtract**: 100 - 30 = 70.00
- **Multiply**: 7 × 8 = 56.00
- **Divide**: 84 ÷ 12 = 7.00

### Integration Testing
1. Start Claude Desktop
2. Verify MCP server appears in tools list
3. Ask Claude to perform calculations
4. Verify results are accurate and properly formatted

## Troubleshooting

### Common Issues

1. **"JAR file not found"**
   - Run `./gradlew clean build` to create the JAR
   - Verify the path in mcp-launcher.sh

2. **"Command not found" in Claude Desktop**
   - Check the launcher script path in Claude config
   - Ensure launcher script has execute permissions: `chmod +x mcp-launcher.sh`

3. **No response from server**
   - Check stderr output for error messages
   - Verify Java environment is properly configured
   - Ensure no other process is using stdin/stdout

4. **JSON parsing errors**
   - Verify input JSON format matches MCP specification
   - Check that stdout is not contaminated with debug output

### Debug Logging
All debug information is logged to stderr and can be viewed by running the server manually:
```bash
./mcp-launcher.sh 2>&1
```

## Future Enhancements

Potential improvements to consider:
1. **Advanced Mathematical Functions**: logarithms, trigonometry, etc.
2. **Unit Conversion Tools**: length, weight, temperature conversions
3. **Statistical Functions**: mean, median, standard deviation
4. **Financial Calculations**: compound interest, loan payments
5. **Configuration Management**: external configuration files for tool definitions
6. **Enhanced Error Handling**: more specific error messages and recovery strategies

---

## Conclusion

This MCP Java Calculator Server demonstrates how to create a robust integration between Claude Desktop and custom Java applications. The architecture prioritizes clean separation of concerns, protocol compliance, and maintainable code structure.

The project successfully bridges the gap between AI conversation interfaces and traditional Java business logic, opening possibilities for integrating existing Java systems with modern AI assistants.

**Project Status**: ✅ Complete and functional
**Last Updated**: October 25, 2025
**Version**: 1.0.0