package com.example.demo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;

@Service
public class MCPService {

    private final ObjectMapper objectMapper;
    private final ToolService toolService;

    public MCPService(ObjectMapper objectMapper, ToolService toolService) {
        this.objectMapper = objectMapper;
        this.toolService = toolService;
    }

    private static final String SERVER_NAME = "java-calculator-springboot";
    private static final String SERVER_VERSION = "1.0.0";
    private static final String PROTOCOL_VERSION = "2025-06-18";

    public JsonNode handleRequest(String method, JsonNode request) {
        ObjectNode response = objectMapper.createObjectNode();
        response.put("jsonrpc", "2.0");

        // Notifications don't have responses, just return null
        if (method.startsWith("notifications/")) {
            handleNotification(method, request);
            return null;
        }

        if (request.has("id")) {
            response.set("id", request.get("id"));
        }

        switch (method) {
            case "initialize":
                handleInitialize(response);
                break;
            case "tools/list":
                handleToolsList(response);
                break;
            case "tools/call":
                handleToolsCall(request, response);
                break;
            case "prompts/list":
                handlePromptsList(response);
                break;
            case "resources/list":
                handleResourcesList(response);
                break;
            default:
                handleError(response, -32601, "Method not found: " + method);
        }

        return response;
    }

    private void handleInitialize(ObjectNode response) {
        ObjectNode result = objectMapper.createObjectNode();
        result.put("protocolVersion", PROTOCOL_VERSION);

        ObjectNode serverInfo = objectMapper.createObjectNode();
        serverInfo.put("name", SERVER_NAME);
        serverInfo.put("version", SERVER_VERSION);
        result.set("serverInfo", serverInfo);

        ObjectNode capabilities = objectMapper.createObjectNode();
        
        ObjectNode tools = objectMapper.createObjectNode();
        tools.put("listChanged", false);
        capabilities.set("tools", tools);
        
        ObjectNode prompts = objectMapper.createObjectNode();
        prompts.put("listChanged", false);
        capabilities.set("prompts", prompts);
        
        ObjectNode resources = objectMapper.createObjectNode();
        resources.put("listChanged", false);
        capabilities.set("resources", resources);
        
        result.set("capabilities", capabilities);

        response.set("result", result);
        System.err.println("Server initialized");
    }

    private void handleToolsList(ObjectNode response) {
        ObjectNode result = objectMapper.createObjectNode();
        ArrayNode tools = objectMapper.createArrayNode();

        // Add tool
        ObjectNode addTool = objectMapper.createObjectNode();
        addTool.put("name", "add");
        addTool.put("description", "Add two numbers together");
        addTool.set("inputSchema", createMathToolSchema());
        tools.add(addTool);

        // Subtract tool
        ObjectNode subtractTool = objectMapper.createObjectNode();
        subtractTool.put("name", "subtract");
        subtractTool.put("description", "Subtract the second number from the first number");
        subtractTool.set("inputSchema", createMathToolSchema());
        tools.add(subtractTool);

        // Multiply tool
        ObjectNode multiplyTool = objectMapper.createObjectNode();
        multiplyTool.put("name", "multiply");
        multiplyTool.put("description", "Multiply two numbers together");
        multiplyTool.set("inputSchema", createMathToolSchema());
        tools.add(multiplyTool);

        // Divide tool
        ObjectNode divideTool = objectMapper.createObjectNode();
        divideTool.put("name", "divide");
        divideTool.put("description", "Divide the first number by the second number");
        divideTool.set("inputSchema", createMathToolSchema());
        tools.add(divideTool);

        result.set("tools", tools);
        response.set("result", result);
        System.err.println("Tools list requested");
    }

    private void handleToolsCall(JsonNode request, ObjectNode response) {
        try {
            JsonNode params = request.get("params");
            String toolName = params.get("name").asText();
            JsonNode arguments = params.get("arguments");

            System.err.println("Tool called: " + toolName);

            String resultText = toolService.executeTool(toolName, arguments);

            ObjectNode result = objectMapper.createObjectNode();
            ArrayNode content = objectMapper.createArrayNode();

            ObjectNode textContent = objectMapper.createObjectNode();
            textContent.put("type", "text");
            textContent.put("text", resultText);
            content.add(textContent);

            result.set("content", content);
            response.set("result", result);

        } catch (IllegalArgumentException e) {
            handleError(response, -32602, e.getMessage());
        } catch (Exception e) {
            System.err.println("Error executing tool: " + e.getMessage());
            handleError(response, -32603, "Error executing tool: " + e.getMessage());
        }
    }

    private void handleError(ObjectNode response, int code, String message) {
        ObjectNode error = objectMapper.createObjectNode();
        error.put("code", code);
        error.put("message", message);
        response.set("error", error);
        System.err.println("Error response: code=" + code + ", message=" + message);
    }

    private void handlePromptsList(ObjectNode response) {
        ObjectNode result = objectMapper.createObjectNode();
        ArrayNode prompts = objectMapper.createArrayNode();
        // Empty prompts list for now
        result.set("prompts", prompts);
        response.set("result", result);
        System.err.println("Prompts list requested");
    }

    private void handleResourcesList(ObjectNode response) {
        ObjectNode result = objectMapper.createObjectNode();
        ArrayNode resources = objectMapper.createArrayNode();
        // Empty resources list for now
        result.set("resources", resources);
        response.set("result", result);
        System.err.println("Resources list requested");
    }

    private void handleNotification(String method, JsonNode request) {
        switch (method) {
            case "notifications/initialized":
                System.err.println("Client initialized notification received");
                break;
            default:
                System.err.println("Unknown notification: " + method);
        }
        // Notifications don't have responses
    }

    private ObjectNode createMathToolSchema() {
        ObjectNode inputSchema = objectMapper.createObjectNode();
        inputSchema.put("type", "object");

        ObjectNode properties = objectMapper.createObjectNode();

        ObjectNode aProperty = objectMapper.createObjectNode();
        aProperty.put("type", "number");
        aProperty.put("description", "First number");
        properties.set("a", aProperty);

        ObjectNode bProperty = objectMapper.createObjectNode();
        bProperty.put("type", "number");
        bProperty.put("description", "Second number");
        properties.set("b", bProperty);

        inputSchema.set("properties", properties);

        ArrayNode required = objectMapper.createArrayNode();
        required.add("a");
        required.add("b");
        inputSchema.set("required", required);

        return inputSchema;
    }
}

