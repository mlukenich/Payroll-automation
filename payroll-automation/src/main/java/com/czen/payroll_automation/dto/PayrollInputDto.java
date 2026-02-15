package com.czen.payroll_automation.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class PayrollInputDto {
    private LocalDate payPeriodBegin;
    private LocalDate payPeriodEnd;
    private LocalDate checkDate;
    private List<PayrollEntryDto> entries;

    @Data
    public static class PayrollEntryDto {
        private String employeeName;
        private String earningType;
        private Double hours;
    }
}
