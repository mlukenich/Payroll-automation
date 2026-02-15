package com.czen.payroll_automation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayEntryImportRequest {
    private boolean autoAcknowledge;
    private String batchName;
    private LocalDateTime payPeriodBeginDate;
    private LocalDateTime payPeriodEndDate;
    private LocalDateTime checkDate;
    private List<ImportRecord> importRecords;
}
