package com.czen.payroll_automation.agent;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class PayrollAgentService {

    @Value("${spring.ai.google.gemini.api-key:}")
    private String apiKey;

    private final PayrollAgentConfiguration.PayrollTools payrollTools;
    private final Map<String, PayrollAgent> agents = new HashMap<>();

    public PayrollAgentService(PayrollAgentConfiguration.PayrollTools payrollTools) {
        this.payrollTools = payrollTools;
    }

    public interface PayrollAgent {
        @SystemMessage("""
                You are a helpful payroll assistant for a small business.
                Your goal is to help the user manage their payroll using Paylocity.

                You can:
                1. Sync employee data from Paylocity.
                2. List employees currently in the system.
                3. Process payroll batches.
                4. Check the status of submitted batches.
                
                GREETINGS & GENERAL QUERIES:
                - If the user says "Hello", "Hi", "Test", or asks what you can do, explain your capabilities clearly and ask how you can help.
                - If you are unsure who the employees are, you can use the `listEmployees` tool.

                CRITICAL INSTRUCTION: When a user wants to submit payroll, you MUST:
                1. Gather all necessary info (Dates, Employees, Hours).
                2. Call the `previewPayroll` tool first.
                3. Present the summary to the user and ask for explicit confirmation.
                4. ONLY after the user says "Yes", "Confirm", "Proceed", or similar, call the `submitPayroll` tool.

                When processing payroll, you MUST have the following information:
                - Pay Period Begin Date
                - Pay Period End Date
                - Check Date
                - A list of employee names, earning types (e.g., Regular, Overtime), and hours.

                If information is missing, ask the user for it.
                Today's date is {{current_date}}.

                Always be professional and concise.
                """)
        String chat(@UserMessage String message, @V("current_date") String currentDate);
    }

    public String chat(String chatId, String message) {
        log.info("Agent session {} processing message", chatId);

        if (apiKey == null || apiKey.isBlank()) {
            return "Error: GOOGLE_API_KEY is not set. Please add it to your application.properties file.";
        }

        final String finalApiKey = apiKey.trim();
        PayrollAgent agent = agents.computeIfAbsent(chatId, id -> AiServices.builder(PayrollAgent.class)
                .chatLanguageModel(GoogleAiGeminiChatModel.builder()
                        .apiKey(finalApiKey)
                        .modelName("gemini-2.5-flash-lite")
                        .build())
                .chatMemory(MessageWindowChatMemory.withMaxMessages(20))
                .tools(payrollTools)
                .build());

        try {
            return agent.chat(message, LocalDate.now().toString());
        } catch (Exception e) {
            log.error("Error during agent chat", e);
            String errorMsg = e.getMessage();
            if (errorMsg == null)
                errorMsg = e.getClass().getSimpleName();
            return "Error from Agent: " + errorMsg;
        }
    }
}
