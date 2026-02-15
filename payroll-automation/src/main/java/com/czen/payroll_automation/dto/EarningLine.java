package com.czen.payroll_automation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EarningLine {
    private String earningCode;
    private Double hours;
    private Double amount;
}
