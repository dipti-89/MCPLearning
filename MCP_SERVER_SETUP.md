# MCP Server Configuration

This directory contains configuration files for integrating the Java Calculator MCP Server with LLM applications.

## Issue Resolution

**Problem**: Claude Desktop shows "Unable to locate a Java Runtime" error, "Unexpected token" JSON parsing errors, and "Method not found" errors.

**Root Causes**: 
1. Claude Desktop runs in a restricted environment that may not have access to the system's Java installation or PATH environment.
2. Spring Boot was printing banners and logs to stdout, interfering with MCP JSON communication.
3. Missing MCP protocol methods (`prompts/list`, `resources/list`) causing client errors.
4. Missing notification handling (`notifications/initialized`) causing protocol violations.
5. Protocol version mismatch between client and server.

**Solutions**: 
- ✅ **Fixed Java Runtime**: Created launcher script with proper Java paths
- ✅ **Fixed JSON Communication**: Modified application to use stdio properly for MCP protocol
- ✅ **Disabled Web Server**: Application now runs as pure MCP server (no Tomcat)
- ✅ **All logs redirected to stderr**: Only JSON responses go to stdout
- ✅ **Complete MCP Protocol**: Implemented all required methods (initialize, tools/list, tools/call, prompts/list, resources/list)
- ✅ **Notification Handling**: Proper handling of `notifications/initialized` (no response required)
- ✅ **Protocol Version**: Updated to match Claude Desktop's expected version (2025-06-18)
- ✅ **Proper Error Handling**: Robust error responses and JSON validation

## Supported MCP Methods

The server now implements the complete MCP protocol:

### Core Methods
- ✅ **`initialize`**: Server initialization with full capabilities (protocol version 2025-06-18)
- ✅ **`notifications/initialized`**: Client initialization notification (no response)
- ✅ **`tools/list`**: List available tools (add calculator)
- ✅ **`tools/call`**: Execute tools with parameters
- ✅ **`prompts/list`**: List available prompts (empty for now)
- ✅ **`resources/list`**: List available resources (empty for now)

## Testing Results

All MCP methods work correctly:

```bash
# Complete test sequence
(
echo '{"method":"initialize","params":{"protocolVersion":"2024-11-05","capabilities":{},"clientInfo":{"name":"test","version":"1.0"}},"jsonrpc":"2.0","id":1}'
echo '{"method":"tools/list","params":{},"jsonrpc":"2.0","id":2}'
echo '{"method":"prompts/list","params":{},"jsonrpc":"2.0","id":3}' 
echo '{"method":"resources/list","params":{},"jsonrpc":"2.0","id":4}'
echo '{"method":"tools/call","params":{"name":"add","arguments":{"a":25,"b":17}},"jsonrpc":"2.0","id":5}'
) | ./mcp-launcher.sh
```

## Available Tools

The server provides the following MCP tools:

### `add`
- **Description**: Add two numbers together
- **Parameters**:
  - `a` (number, required): First number
  - `b` (number, required): Second number
- **Returns**: The sum of the two numbers

## Configuration Options (Try in Order)

### Option 1: Using Launcher Script (RECOMMENDED)
Use `claude-desktop-config-fixed.json` - This should resolve the Java runtime issue.

```json
{
  "mcpServers": {
    "java-calculator": {
      "command": "/Users/I320519/Documents/MCPJava/mcp-launcher.sh",
      "args": [],
      "env": {
        "SPRING_PROFILES_ACTIVE": "mcp"
      }
    }
  }
}
```

### Option 2: Using Full Java Path
Use `claude-desktop-config-fullpath.json` if Option 1 doesn't work.

```json
{
  "mcpServers": {
    "java-calculator": {
      "command": "/Users/I320519/SAPDevelop/sfsf/tools/sapjvm/Contents/Home/bin/java",
      "args": [
        "-jar",
        "/Users/I320519/Documents/MCPJava/build/libs/demo-0.0.1-SNAPSHOT.jar"
      ],
      "env": {
        "SPRING_PROFILES_ACTIVE": "mcp",
        "JAVA_HOME": "/Users/I320519/SAPDevelop/sfsf/tools/sapjvm/Contents/Home"
      }
    }
  }
}
```

### Option 3: Using System Java (Original - May Fail)
Use `claude-desktop-config.json` only if your system Java is properly configured.

```json
{
  "mcpServers": {
    "java-calculator": {
      "command": "java",
      "args": [
        "-jar",
        "/Users/I320519/Documents/MCPJava/build/libs/demo-0.0.1-SNAPSHOT.jar"
      ]
    }
  }
}
```

## Setup Instructions

### For Claude Desktop:

1. **Locate your Claude Desktop config file:**
   - **macOS**: `~/Library/Application Support/Claude/claude_desktop_config.json`
   - **Windows**: `%APPDATA%\Claude\claude_desktop_config.json`

2. **Add the server configuration:**
   - Copy the contents of either `mcp-server-config.json` or `mcp-server-config-gradle.json`
   - Merge it into your existing `claude_desktop_config.json`

3. **Restart Claude Desktop** to load the new server

### For other MCP clients:

Use the appropriate configuration format for your MCP client, following the same structure as shown above.

## Server Capabilities

- ✅ **Tools**: Calculator functions (add operation)
- ✅ **Resources**: Available for future extensions
- ✅ **Prompts**: Available for future extensions  
- ✅ **Completions**: Available for future extensions

## Usage Example

Once connected, you can ask the LLM to perform calculations:

```
"Please add 15 and 27 using the calculator tool"
```

The LLM will use the `add` tool with parameters `{"a": 15, "b": 27}` and return the result: `42.00`.

## Troubleshooting

1. **JAR file not found**: Run `./gradlew bootJar` to build the JAR file
2. **Permission denied**: Ensure the JAR file has execute permissions
3. **Port conflicts**: The server uses port 8080 by default
4. **Java version**: Requires Java 17 or later

## Extending the Server

To add more tools:

1. Add new methods to `ToolService.java`
2. Update the tools list in `MCPService.handleToolsList()`
3. Update the tool calling logic in `MCPService.handleToolsCall()`
4. Rebuild and restart the server