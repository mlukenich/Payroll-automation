package com.czen.payroll_automation.service;

import com.czen.payroll_automation.model.Employee;
import com.czen.payroll_automation.domain.PayrollBatchEntity;
import com.czen.payroll_automation.dto.*;
import com.czen.payroll_automation.repository.EmployeeRepository;
import com.czen.payroll_automation.repository.PayrollBatchRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class PayrollService {

    private final PaylocityClient paylocityClient;
    private final EmployeeRepository employeeRepository;
    private final PayrollBatchRepository payrollBatchRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void syncEmployees() {
        log.info("Starting employee synchronization...");
        try {
            List<EmployeeDto> apiEmployees = paylocityClient.getAllEmployees();

            for (EmployeeDto dto : apiEmployees) {
                if (dto.getEmployeeId() == null) continue;

                Optional<Employee> existing = employeeRepository.findByEmployeeId(dto.getEmployeeId());
                Employee entity = existing.orElse(new Employee());

                entity.setEmployeeId(dto.getEmployeeId());
                entity.setName(dto.getFullName());
                entity.setEmail(dto.getEmailAddress());
                entity.setStatus(dto.getStatusCode());

                employeeRepository.save(entity);
            }
            log.info("Employee synchronization completed. Processed {} employees.", apiEmployees.size());
        } catch (Exception e) {
            log.error("Employee synchronization failed", e);
            throw new RuntimeException("Employee synchronization failed", e);
        }
    }

    @Transactional
    public PayrollBatchEntity processPayroll(PayrollInputDto input) {
        log.info("Processing payroll submission...");

        // 1. Resolve Codes
        Map<String, String> codeMap = resolveEarningCodes();

        // 2. Group Entries by Employee
        Map<String, List<EarningLine>> employeeEarnings = new HashMap<>();

        for (PayrollInputDto.PayrollEntryDto entry : input.getEntries()) {
            // Find Employee
            Employee employee = findEmployee(entry.getEmployeeName());
            if (employee == null) {
                log.warn("Employee not found for name: {}", entry.getEmployeeName());
                continue;
            }

            // Find Code
            String earningCode = resolveCode(codeMap, entry.getEarningType());
            if (earningCode == null) {
                 log.warn("Earning code not found for type: {}", entry.getEarningType());
                 continue;
            }

            EarningLine line = EarningLine.builder()
                    .earningCode(earningCode)
                    .hours(entry.getHours())
                    .build();

            employeeEarnings.computeIfAbsent(employee.getEmployeeId(), k -> new ArrayList<>()).add(line);
        }

        if (employeeEarnings.isEmpty()) {
            throw new RuntimeException("No valid payroll records generated. Check employee names and earning types.");
        }

        // 3. Build Import Records
        List<ImportRecord> importRecords = employeeEarnings.entrySet().stream()
                .map(e -> ImportRecord.builder()
                        .employeeId(e.getKey())
                        .earnings(e.getValue())
                        .build())
                .collect(Collectors.toList());

        // 4. Create Request
        PayEntryImportRequest request = PayEntryImportRequest.builder()
                .autoAcknowledge(true)
                .batchName("Batch_" + input.getCheckDate())
                .payPeriodBeginDate(input.getPayPeriodBegin().atStartOfDay())
                .payPeriodEndDate(input.getPayPeriodEnd().atStartOfDay())
                .checkDate(input.getCheckDate().atStartOfDay())
                .importRecords(importRecords)
                .build();

        // 5. Submit
        ImportStatusResponse response = paylocityClient.submitPayrollBatch(request);

        // 6. Save Status
        PayrollBatchEntity batchEntity = new PayrollBatchEntity();
        batchEntity.setTrackingId(response.getTimeImportFileTrackingId());
        batchEntity.setStatus(response.getStatus());
        batchEntity.setSubmitDate(LocalDateTime.now());

        return payrollBatchRepository.save(batchEntity);
    }

    private Map<String, String> resolveEarningCodes() {
        List<CodeDto> codes = paylocityClient.getEarningCodes();
        Map<String, String> map = new HashMap<>();
        for (CodeDto code : codes) {
            map.put(code.getDescription().toLowerCase(), code.getCode());
            // Add direct code mapping if needed, e.g. "E001" -> "E001"
            map.put(code.getCode().toLowerCase(), code.getCode());
        }
        return map;
    }

    private String resolveCode(Map<String, String> map, String type) {
        if (type == null) return null;
        String key = type.toLowerCase();

        // Direct match
        if (map.containsKey(key)) return map.get(key);

        // Fuzzy match (contains)
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (entry.getKey().contains(key)) {
                return entry.getValue();
            }
        }
        return null; // Not found
    }

    private Employee findEmployee(String nameOrEmail) {
        if (nameOrEmail == null) return null;

        // Try by email
        Optional<Employee> byEmail = employeeRepository.findByEmailIgnoreCase(nameOrEmail);
        if (byEmail.isPresent()) return byEmail.get();

        // Try by name
        Optional<Employee> byName = employeeRepository.findByNameIgnoreCase(nameOrEmail);
        return byName.orElse(null);
    }

    @Transactional
    public PayrollBatchEntity updateBatchStatus(String trackingId) {
        PayrollBatchEntity entity = payrollBatchRepository.findByTrackingId(trackingId)
                .orElseThrow(() -> new RuntimeException("Batch not found: " + trackingId));

        ImportStatusResponse response = paylocityClient.getBatchStatus(trackingId);

        entity.setStatus(response.getStatus());
        if (response.getErrors() != null && !response.getErrors().isEmpty()) {
            try {
                entity.setErrors(objectMapper.writeValueAsString(response.getErrors()));
            } catch (Exception e) {
                entity.setErrors("Error serializing errors: " + e.getMessage());
            }
        }

        return payrollBatchRepository.save(entity);
    }
}
