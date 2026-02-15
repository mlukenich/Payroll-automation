package com.czen.payroll_automation.controller;

import com.czen.payroll_automation.domain.PayrollBatchEntity;
import com.czen.payroll_automation.dto.PayrollInputDto;
import com.czen.payroll_automation.service.PayrollService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payroll")
@RequiredArgsConstructor
public class PayrollController {

    private final PayrollService payrollService;

    @PostMapping("/sync-employees")
    public ResponseEntity<String> syncEmployees() {
        payrollService.syncEmployees();
        return ResponseEntity.ok("Employee synchronization started/completed successfully.");
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
