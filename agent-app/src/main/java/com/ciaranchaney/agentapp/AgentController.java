package com.ciaranchaney.agentapp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/agent")
public class AgentController {

    private final ChatClient chatClient;
    private final ToolCallbackProvider mcpToolProvider;
    private final ObjectMapper mapper = new ObjectMapper();

    public AgentController(ChatClient.Builder builder, ToolCallbackProvider mcpToolProvider) {
        this.chatClient = builder.build();
        this.mcpToolProvider = mcpToolProvider;
    }

    @PostMapping("/ask")
    public Object ask(@RequestBody Map<String, String> body) throws Exception {
        String prompt = body.getOrDefault("prompt", "");

        String answer = chatClient
                .prompt(prompt)
                .toolCallbacks(mcpToolProvider)
                .call()
                .content();

        // If the model returned JSON, return it as JSON
        if (answer != null && answer.trim().startsWith("{")) {
            JsonNode node = mapper.readTree(answer);
            return Map.of("answer", node);
        }
        return Map.of("answer", answer);
    }
}
