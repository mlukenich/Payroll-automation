package com.czen.payroll_automation.controller;

import com.czen.payroll_automation.domain.PayrollBatchEntity;
import com.czen.payroll_automation.dto.PayrollInputDto;
import com.czen.payroll_automation.model.TimeEntry;
import com.czen.payroll_automation.service.PayrollService;
import com.czen.payroll_automation.service.TimeEntryService;
import com.czen.payroll_automation.service.ValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payroll")
@RequiredArgsConstructor
public class PayrollController {

    private final PayrollService payrollService;
    private final TimeEntryService timeEntryService;
    private final ValidationService validationService;

    @PostMapping("/sync-employees")
    public ResponseEntity<String> syncEmployees() {
        payrollService.syncEmployees();
        return ResponseEntity.ok("Employee synchronization started/completed successfully.");
    }

    @PostMapping("/time-entries")
    public ResponseEntity<String> submitTimeEntry(@RequestBody TimeEntry timeEntry) {
        if (!validationService.validateTimeEntry(timeEntry)) {
            return ResponseEntity.badRequest().body("Time entry is invalid.");
        }
        timeEntryService.saveTimeEntry(timeEntry);
        return ResponseEntity.ok("Time entry saved successfully.");
    }

    @PostMapping("/submit")
    public ResponseEntity<PayrollBatchEntity> submitPayroll(@RequestBody PayrollInputDto input) {
        PayrollBatchEntity batch = payrollService.processPayroll(input);
        return ResponseEntity.accepted().body(batch);
    }

    @GetMapping("/batch/{trackingId}")
    public ResponseEntity<PayrollBatchEntity> getBatchStatus(@PathVariable String trackingId) {
        PayrollBatchEntity batch = payrollService.updateBatchStatus(trackingId);
        return ResponseEntity.ok(batch);
    }
}
