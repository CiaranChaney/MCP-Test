package com.ciaranchaney.agentapp;

import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/mcp")
public class McpDebugController {

    private final SyncMcpToolCallbackProvider mcpToolProvider;

    public McpDebugController(SyncMcpToolCallbackProvider mcpToolProvider) {
        this.mcpToolProvider = mcpToolProvider;
    }

    @GetMapping("/tools")
    public List<String> tools() {
        ToolCallback[] callbacks = mcpToolProvider.getToolCallbacks();
        return Arrays.stream(callbacks)
                .map(cb -> cb.getToolDefinition().name())
                .toList();
    }
}