package com.czen.payroll_automation.agent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/agent")
@RequiredArgsConstructor
@Slf4j
public class AgentController {

    private final PayrollAgentService payrollAgentService;

    @PostMapping("/chat")
    public ChatResponse chat(@RequestBody ChatRequest request) {
        try {
            log.info("Received chat request for session {}: {}", request.chatId(), request.message());
            String response = payrollAgentService.chat(request.chatId(), request.message());
            if (response == null) {
                log.warn("Agent returned null response for message: {}", request.message());
                return new ChatResponse("The agent was unable to generate a response. Please check backend logs.");
            }
            return new ChatResponse(response);
        } catch (Exception e) {
            log.error("Error in agent chat", e);
            return new ChatResponse("Error: " + e.getMessage());
        }
    }

    public record ChatRequest(String chatId, String message) {}
    public record ChatResponse(String response) {}
}
