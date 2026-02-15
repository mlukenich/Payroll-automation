package com.czen.payroll_automation.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // Full Name
    private String email;

    @Column(unique = true)
    private String employeeId; // Paylocity ID

    private String status;

    public Employee(String name, String email) {
        this.name = name;
        this.email = email;
    }
}
