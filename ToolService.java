package com.example.demo;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;

@Service
public class ToolService {

    public String executeTool(String toolName, JsonNode arguments) {
        switch (toolName) {
            case "add":
                return executeAdd(arguments);
            case "subtract":
                return executeSubtract(arguments);
            case "multiply":
                return executeMultiply(arguments);
            case "divide":
                return executeDivide(arguments);
            default:
                throw new IllegalArgumentException("Unknown tool: " + toolName);
        }
    }

    private String executeAdd(JsonNode arguments) {
        double a = arguments.get("a").asDouble();
        double b = arguments.get("b").asDouble();
        double result = a + b;

        System.err.println("Addition: " + a + " + " + b + " = " + result);
        return String.format("Result: %.2f", result);
    }

    private String executeSubtract(JsonNode arguments) {
        double a = arguments.get("a").asDouble();
        double b = arguments.get("b").asDouble();
        double result = a - b;

        System.err.println("Subtraction: " + a + " - " + b + " = " + result);
        return String.format("Result: %.2f", result);
    }

    private String executeMultiply(JsonNode arguments) {
        double a = arguments.get("a").asDouble();
        double b = arguments.get("b").asDouble();
        double result = a * b;

        System.err.println("Multiplication: " + a + " * " + b + " = " + result);
        return String.format("Result: %.2f", result);
    }

    private String executeDivide(JsonNode arguments) {
        double a = arguments.get("a").asDouble();
        double b = arguments.get("b").asDouble();
        
        if (b == 0) {
            System.err.println("Division error: Cannot divide by zero");
            return "Error: Cannot divide by zero";
        }
        
        double result = a / b;
        System.err.println("Division: " + a + " / " + b + " = " + result);
        return String.format("Result: %.2f", result);
    }
}
