package com.czen.payroll_automation.service;

import com.czen.payroll_automation.model.TimeEntry;
import com.czen.payroll_automation.repository.TimeEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.any;

class TimeEntryServiceTest {

    @Mock
    private ValidationService validationService;

    @Mock
    private TimeEntryRepository timeEntryRepository;

    @InjectMocks
    private TimeEntryService timeEntryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSaveTimeEntry_Valid() {
        TimeEntry timeEntry = new TimeEntry();

        when(validationService.validateTimeEntry(timeEntry)).thenReturn(true);
        when(timeEntryRepository.save(timeEntry)).thenReturn(timeEntry);

        TimeEntry savedEntry = timeEntryService.saveTimeEntry(timeEntry);

        assertEquals(timeEntry, savedEntry);
        verify(validationService).validateTimeEntry(timeEntry);
        verify(timeEntryRepository).save(timeEntry);
    }

    @Test
    void testSaveTimeEntry_Invalid() {
        TimeEntry timeEntry = new TimeEntry();

        when(validationService.validateTimeEntry(timeEntry)).thenReturn(false);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            timeEntryService.saveTimeEntry(timeEntry);
        });

        assertEquals("Time entry is invalid.", exception.getMessage());
        verify(validationService).validateTimeEntry(timeEntry);
        verify(timeEntryRepository, never()).save(any(TimeEntry.class));
    }
}
