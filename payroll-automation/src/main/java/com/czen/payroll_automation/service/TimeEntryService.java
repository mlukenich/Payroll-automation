package com.czen.payroll_automation.service;

import com.czen.payroll_automation.model.TimeEntry;
import com.czen.payroll_automation.repository.TimeEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TimeEntryService {

    private final ValidationService validationService;
    private final TimeEntryRepository timeEntryRepository;

    @Autowired
    public TimeEntryService(ValidationService validationService, TimeEntryRepository timeEntryRepository) {
        this.validationService = validationService;
        this.timeEntryRepository = timeEntryRepository;
    }

    public TimeEntry saveTimeEntry(TimeEntry timeEntry) {
        if (!validationService.validateTimeEntry(timeEntry)) {
            throw new IllegalArgumentException("Time entry is invalid.");
        }
        return timeEntryRepository.save(timeEntry);
    }
}
