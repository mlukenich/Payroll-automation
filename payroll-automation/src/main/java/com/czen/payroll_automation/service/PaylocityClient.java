package com.czen.payroll_automation.service;

import com.czen.payroll_automation.config.PaylocityProperties;
import com.czen.payroll_automation.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class PaylocityClient {

    private final PaylocityProperties properties;
    private final PaylocityAuthService authService;
    private final RestClient baseClient;

    public PaylocityClient(PaylocityProperties properties,
                           PaylocityAuthService authService,
                           RestClient.Builder restClientBuilder) {
        this.properties = properties;
        this.authService = authService;
        this.baseClient = restClientBuilder
                .baseUrl(properties.getBaseUrl())
                .build();
    }

    private RestClient getClient() {
        return baseClient.mutate()
                .defaultHeader("Authorization", "Bearer " + authService.getAccessToken())
                .build();
    }

    public List<EmployeeDto> getAllEmployees() {
        List<EmployeeDto> allEmployees = new ArrayList<>();
        int pageNumber = 0;
        int pageSize = 100;
        boolean hasMore = true;

        while (hasMore) {
            log.debug("Fetching employees page {}", pageNumber);
            try {
                // Need to use effectively final or standard variable for lambda
                final int currentPage = pageNumber;
                List<EmployeeDto> page = getClient().get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/v2/companies/{companyId}/employees")
                                .queryParam("pagesize", pageSize)
                                .queryParam("pagenumber", currentPage)
                                .queryParam("includetotalcount", true)
                                .build(properties.getCompanyId()))
                        .retrieve()
                        .body(new ParameterizedTypeReference<List<EmployeeDto>>() {});

                if (page == null || page.isEmpty()) {
                    hasMore = false;
                } else {
                    allEmployees.addAll(page);
                    if (page.size() < pageSize) {
                        hasMore = false;
                    } else {
                        pageNumber++;
                    }
                }
            } catch (Exception e) {
                log.error("Error fetching employees page {}", pageNumber, e);
                throw new RuntimeException("Failed to fetch employees", e);
            }
        }
        log.info("Fetched {} total employees", allEmployees.size());
        return allEmployees;
    }

    public List<CodeDto> getEarningCodes() {
        log.debug("Fetching earning codes");
        try {
            return getClient().get()
                    .uri("/v2/companies/{companyId}/codes/earnings", properties.getCompanyId())
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<CodeDto>>() {});
        } catch (Exception e) {
            log.error("Error fetching earning codes", e);
            throw new RuntimeException("Failed to fetch earning codes", e);
        }
    }

    public ImportStatusResponse submitPayrollBatch(PayEntryImportRequest request) {
        log.info("Submitting payroll batch: {}", request.getBatchName());
        try {
            return getClient().post()
                    .uri("/v2/companies/{companyId}/payEntryImport", properties.getCompanyId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(ImportStatusResponse.class);
        } catch (Exception e) {
            log.error("Error submitting payroll batch", e);
            throw new RuntimeException("Failed to submit payroll batch", e);
        }
    }

    public ImportStatusResponse getBatchStatus(String trackingId) {
        log.debug("Checking batch status for trackingId: {}", trackingId);
        try {
            return getClient().get()
                    .uri("/v2/companies/{companyId}/payEntryImport/{trackingId}",
                            properties.getCompanyId(), trackingId)
                    .retrieve()
                    .body(ImportStatusResponse.class);
        } catch (Exception e) {
            log.error("Error checking batch status", e);
            throw new RuntimeException("Failed to check batch status", e);
        }
    }
}
