package com.czen.payroll_automation;

import com.czen.payroll_automation.model.Department;
import com.czen.payroll_automation.model.Employee;
import com.czen.payroll_automation.model.JobCode;
import com.czen.payroll_automation.model.TimeEntry;
import com.czen.payroll_automation.repository.TimeEntryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FullIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TimeEntryRepository timeEntryRepository;

    @Test
    public void testValidTimeEntry() {
        // Given
        Employee employee = new Employee();
        employee.setId(1L);

        Department department = new Department();
        department.setId(1L);

        JobCode jobCode = new JobCode();
        jobCode.setId(1L);

        TimeEntry timeEntry = new TimeEntry();
        timeEntry.setEmployee(employee);
        timeEntry.setDepartment(department);
        timeEntry.setJobCode(jobCode);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<TimeEntry> request = new HttpEntity<>(timeEntry, headers);

        // When
        ResponseEntity<String> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/payroll/time-entries",
                request,
                String.class
        );

        // Then
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isEqualTo("Time entry saved successfully.");

        // Verify persistence
        assertThat(timeEntryRepository.count()).isGreaterThan(0);
    }

    @Test
    public void testInvalidTimeEntry() {
        // Given
        Employee employee = new Employee();
        employee.setId(99L); // Non-existent employee

        Department department = new Department();
        department.setId(1L);

        JobCode jobCode = new JobCode();
        jobCode.setId(1L);

        TimeEntry timeEntry = new TimeEntry();
        timeEntry.setEmployee(employee);
        timeEntry.setDepartment(department);
        timeEntry.setJobCode(jobCode);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<TimeEntry> request = new HttpEntity<>(timeEntry, headers);

        // When
        ResponseEntity<String> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/payroll/time-entries",
                request,
                String.class
        );

        // Then
        assertThat(response.getStatusCode().is4xxClientError()).isTrue();
        assertThat(response.getBody()).isEqualTo("Time entry is invalid.");
    }
}
