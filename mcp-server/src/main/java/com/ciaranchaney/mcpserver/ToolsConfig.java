package com.ciaranchaney.mcpserver;

import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

@Component
public class ToolsConfig {

    @McpTool(name = "sayHello", description = "Say hello to a person.")
    public Map<String, Object> sayHello(
            @McpToolParam(description = "Name of the person", required = true) String name
    ) {
        return Map.of(
                "message", "Hello " + name + " 👋",
                "timestamp", Instant.now().toString()
        );
    }

    @McpTool(name = "add", description = "Add two integers.")
    public Map<String, Object> add(
            @McpToolParam(description = "First number", required = true) int a,
            @McpToolParam(description = "Second number", required = true) int b
    ) {
        return Map.of(
                "a", a,
                "b", b,
                "sum", a + b
        );
    }
}
