package com.czen.payroll_automation.agent;

import com.czen.payroll_automation.dto.PayrollInputDto;
import com.czen.payroll_automation.service.PayrollService;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
public class PayrollAgentConfiguration {

    @Component
    @RequiredArgsConstructor
    @Slf4j
    public static class PayrollTools {
        private final PayrollService payrollService;
        private final com.czen.payroll_automation.repository.EmployeeRepository employeeRepository;

        @Tool("List all employees currently in the local database")
        public String listEmployees() {
            log.info("AI Tool: Listing employees");
            var employees = employeeRepository.findAll();
            if (employees.isEmpty()) {
                return "No employees found in the local database. You may need to sync them from Paylocity.";
            }
            StringBuilder sb = new StringBuilder("### Current Employees:\n");
            for (var e : employees) {
                sb.append(String.format("- %s (ID: %s, Email: %s)\n", e.getName(), e.getEmployeeId(), e.getEmail()));
            }
            return sb.toString();
        }

        @Tool("Synchronize employee data from Paylocity to the local database")
        public String syncEmployees() {
            log.info("AI Tool: Synchronizing employees");
            payrollService.syncEmployees();
            return "Employee synchronization completed successfully.";
        }

        @Tool("Preview a payroll batch submission. Use this to show the user what will be submitted for their approval.")
        public String previewPayroll(PayrollInputDto input) {
            log.info("AI Tool: Previewing payroll");
            StringBuilder summary = new StringBuilder("### Payroll Submission Preview\n");
            summary.append(String.format("- **Period:** %s to %s\n", input.getPayPeriodBegin(), input.getPayPeriodEnd()));
            summary.append(String.format("- **Check Date:** %s\n", input.getCheckDate()));
            summary.append("- **Entries:**\n");
            
            for (var entry : input.getEntries()) {
                summary.append(String.format("  - %s: %s hrs (%s)\n", 
                    entry.getEmployeeName(), entry.getHours(), entry.getEarningType()));
            }
            summary.append("\n**Please confirm if this is correct. Should I submit this to Paylocity?**");
            return summary.toString();
        }

        @Tool("Submit the confirmed payroll batch to Paylocity. ONLY call this after the user has explicitly confirmed the preview.")
        public String submitPayroll(PayrollInputDto input) {
            log.info("AI Tool: Submitting payroll");
            try {
                var batch = payrollService.processPayroll(input);
                return "Payroll submitted successfully. Tracking ID: " + batch.getTrackingId() + ". Status: " + batch.getStatus();
            } catch (Exception e) {
                log.error("Error in AI Tool submitPayroll", e);
                return "Failed to submit payroll: " + e.getMessage();
            }
        }

        @Tool("Check the status of a previously submitted payroll batch using its tracking ID")
        public String getBatchStatus(String trackingId) {
            log.info("AI Tool: Checking status for batch {}", trackingId);
            try {
                var batch = payrollService.updateBatchStatus(trackingId);
                return "Batch status: " + batch.getStatus() + (batch.getErrors() != null ? ". Errors: " + batch.getErrors() : "");
            } catch (Exception e) {
                return "Failed to get batch status: " + e.getMessage();
            }
        }
    }
}
