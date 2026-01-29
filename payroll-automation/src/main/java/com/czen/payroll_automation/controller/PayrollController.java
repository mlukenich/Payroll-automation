package com.czen.payroll_automation.controller;

import com.czen.payroll_automation.model.TimeEntry;
import com.czen.payroll_automation.service.TimeEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payroll")
public class PayrollController {

    private final TimeEntryService timeEntryService;

    @Autowired
    public PayrollController(TimeEntryService timeEntryService) {
        this.timeEntryService = timeEntryService;
    }

    @PostMapping("/time-entries")
    public ResponseEntity<String> submitTimeEntry(@RequestBody TimeEntry timeEntry) {
        try {
            timeEntryService.saveTimeEntry(timeEntry);
            return ResponseEntity.ok("Time entry saved successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
