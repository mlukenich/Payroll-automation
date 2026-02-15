package com.czen.payroll_automation.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ImportStatusResponse {
    private String fileName;
    private String timeImportFileTrackingId;
    private String status;
    private List<ImportError> errors;
}
