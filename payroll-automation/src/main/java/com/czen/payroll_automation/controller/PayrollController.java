package com.czen.payroll_automation.controller;

import com.czen.payroll_automation.model.TimeEntry;
import com.czen.payroll_automation.service.ValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payroll")
public class PayrollController {

    private final ValidationService validationService;

    @Autowired
    public PayrollController(ValidationService validationService) {
        this.validationService = validationService;
    }

    @PostMapping("/validate")
    public ResponseEntity<String> validateTimeEntry(@RequestBody TimeEntry timeEntry) {
        boolean isValid = validationService.validateTimeEntry(timeEntry);
        if (isValid) {
            return ResponseEntity.ok("Time entry is valid.");
        } else {
            return ResponseEntity.badRequest().body("Time entry is invalid.");
        }
    }
}