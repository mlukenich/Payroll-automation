package com.czen.payroll_automation.repository;

import com.czen.payroll_automation.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByEmployeeId(String employeeId);
    Optional<Employee> findByNameIgnoreCase(String name);
    Optional<Employee> findByEmailIgnoreCase(String email);
}
