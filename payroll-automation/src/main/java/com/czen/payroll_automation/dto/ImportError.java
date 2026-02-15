package com.czen.payroll_automation.dto;

import lombok.Data;

@Data
public class ImportError {
    private int rowNumber;
    private String message;
}
