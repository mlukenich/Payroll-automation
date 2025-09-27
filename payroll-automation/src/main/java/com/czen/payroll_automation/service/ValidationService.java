package com.czen.payroll_automation.service;

import com.czen.payroll_automation.model.TimeEntry;
import com.czen.payroll_automation.repository.DepartmentRepository;
import com.czen.payroll_automation.repository.EmployeeRepository;
import com.czen.payroll_automation.repository.JobCodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ValidationService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final JobCodeRepository jobCodeRepository;

    @Autowired
    public ValidationService(EmployeeRepository employeeRepository, DepartmentRepository departmentRepository, JobCodeRepository jobCodeRepository) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.jobCodeRepository = jobCodeRepository;
    }

    public boolean validateTimeEntry(TimeEntry timeEntry) {
        if (timeEntry.getEmployee() == null || timeEntry.getDepartment() == null || timeEntry.getJobCode() == null) {
            return false;
        }

        boolean employeeExists = employeeRepository.existsById(timeEntry.getEmployee().getId());
        boolean departmentExists = departmentRepository.existsById(timeEntry.getDepartment().getId());
        boolean jobCodeExists = jobCodeRepository.existsById(timeEntry.getJobCode().getId());

        return employeeExists && departmentExists && jobCodeExists;
    }
}