package com.czen.payroll_automation.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmployeeDto {
    private String employeeId;
    private String firstName;
    private String lastName;
    private String emailAddress;
    private String statusCode;

    public String getFullName() {
        if (firstName == null && lastName == null) return "";
        return (firstName + " " + lastName).trim();
    }
}
