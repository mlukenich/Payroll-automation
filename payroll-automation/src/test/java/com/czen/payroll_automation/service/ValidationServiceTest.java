package com.czen.payroll_automation.service;

import com.czen.payroll_automation.model.Department;
import com.czen.payroll_automation.model.Employee;
import com.czen.payroll_automation.model.JobCode;
import com.czen.payroll_automation.model.TimeEntry;
import com.czen.payroll_automation.repository.DepartmentRepository;
import com.czen.payroll_automation.repository.EmployeeRepository;
import com.czen.payroll_automation.repository.JobCodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class ValidationServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private JobCodeRepository jobCodeRepository;

    @InjectMocks
    private ValidationService validationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testValidateTimeEntry_Valid() {
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

        when(employeeRepository.existsById(1L)).thenReturn(true);
        when(departmentRepository.existsById(1L)).thenReturn(true);
        when(jobCodeRepository.existsById(1L)).thenReturn(true);

        assertTrue(validationService.validateTimeEntry(timeEntry));
    }

    @Test
    void testValidateTimeEntry_InvalidEmployee() {
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

        when(employeeRepository.existsById(1L)).thenReturn(false);
        when(departmentRepository.existsById(1L)).thenReturn(true);
        when(jobCodeRepository.existsById(1L)).thenReturn(true);

        assertFalse(validationService.validateTimeEntry(timeEntry));
    }
}