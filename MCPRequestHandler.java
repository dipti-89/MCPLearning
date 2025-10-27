package com.example.demo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

@Component
public class MCPRequestHandler {
    
    private final ObjectMapper objectMapper;
    private final MCPService mcpService;

    public MCPRequestHandler(ObjectMapper objectMapper, MCPService mcpService) {
        this.objectMapper = objectMapper;
        this.mcpService = mcpService;
    }

    public void start() {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(System.in, StandardCharsets.UTF_8))) {

            // Use unbuffered output to ensure immediate JSON responses
            PrintWriter writer = new PrintWriter(System.out, true);

            // Send error messages to stderr to avoid interfering with MCP JSON communication
            System.err.println("MCP Server started and listening for requests...");

            String line;
            while ((line = reader.readLine()) != null) {
                handleRequest(line.trim(), writer);
            }
        } catch (Exception e) {
            System.err.println("Error in MCP server: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }

    private void handleRequest(String requestJson, PrintWriter writer) {
        try {
            if (requestJson.isEmpty()) {
                return; // Skip empty lines
            }
            
            JsonNode request = objectMapper.readTree(requestJson);
            String method = request.get("method").asText();

            // Log to stderr to avoid interfering with stdout JSON
            System.err.println("Received request: method=" + method);

            JsonNode response = mcpService.handleRequest(method, request);
            
            // Only send response if it's not null (notifications return null)
            if (response != null) {
                String responseJson = objectMapper.writeValueAsString(response);
                
                // Write response and flush immediately
                writer.println(responseJson);
                writer.flush();
            }

        } catch (Exception e) {
            System.err.println("Error handling request: " + e.getMessage());
            e.printStackTrace(System.err);
            
            // Send error response if possible
            try {
                JsonNode request = objectMapper.readTree(requestJson);
                ObjectNode errorResponse = objectMapper.createObjectNode();
                errorResponse.put("jsonrpc", "2.0");
                if (request.has("id")) {
                    errorResponse.set("id", request.get("id"));
                }
                ObjectNode error = objectMapper.createObjectNode();
                error.put("code", -32603);
                error.put("message", "Internal error: " + e.getMessage());
                errorResponse.set("error", error);
                
                writer.println(objectMapper.writeValueAsString(errorResponse));
                writer.flush();
            } catch (Exception ex) {
                System.err.println("Failed to send error response: " + ex.getMessage());
            }
        }
    }
}
