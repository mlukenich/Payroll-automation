package com.czen.payroll_automation.config;

import com.czen.payroll_automation.model.Department;
import com.czen.payroll_automation.model.Employee;
import com.czen.payroll_automation.model.JobCode;
import com.czen.payroll_automation.repository.DepartmentRepository;
import com.czen.payroll_automation.repository.EmployeeRepository;
import com.czen.payroll_automation.repository.JobCodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final JobCodeRepository jobCodeRepository;

    @Autowired
    public DataInitializer(EmployeeRepository employeeRepository,
                           DepartmentRepository departmentRepository,
                           JobCodeRepository jobCodeRepository) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.jobCodeRepository = jobCodeRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Insert Departments
        Department engineering = new Department();
        engineering.setName("Engineering");
        departmentRepository.save(engineering);

        Department hr = new Department();
        hr.setName("Human Resources");
        departmentRepository.save(hr);

        Department sales = new Department();
        sales.setName("Sales");
        departmentRepository.save(sales);

        // Insert Job Codes
        JobCode dev01 = new JobCode();
        dev01.setCode("DEV-01");
        dev01.setDescription("Software Engineer");
        jobCodeRepository.save(dev01);

        JobCode hr01 = new JobCode();
        hr01.setCode("HR-01");
        hr01.setDescription("Recruiter");
        jobCodeRepository.save(hr01);

        JobCode sale01 = new JobCode();
        sale01.setCode("SALE-01");
        sale01.setDescription("Sales Representative");
        jobCodeRepository.save(sale01);

        // Insert Employees
        Employee alice = new Employee();
        alice.setName("Alice");
        alice.setEmail("alice@example.com");
        employeeRepository.save(alice);

        Employee bob = new Employee();
        bob.setName("Bob");
        bob.setEmail("bob@example.com");
        employeeRepository.save(bob);

        Employee charlie = new Employee();
        charlie.setName("Charlie");
        charlie.setEmail("charlie@example.com");
        employeeRepository.save(charlie);
    }
}