package com.czen.payroll_automation;

import com.czen.payroll_automation.domain.PayrollBatchEntity;
import com.czen.payroll_automation.dto.PayrollInputDto;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.LocalDate;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PayrollIntegrationTest {

    private static WireMockServer wireMockServer;

    @Autowired
    private TestRestTemplate restTemplate;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("paylocity.baseUrl", () -> "http://localhost:8089");
        registry.add("paylocity.clientId", () -> "test-client");
        registry.add("paylocity.clientSecret", () -> "test-secret");
        registry.add("paylocity.companyId", () -> "99999");
    }

    @BeforeAll
    static void startWireMock() {
        wireMockServer = new WireMockServer(8089);
        wireMockServer.start();
        WireMock.configureFor("localhost", 8089);
    }

    @AfterAll
    static void stopWireMock() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @BeforeEach
    void setup() {
        wireMockServer.resetAll();

        // Stub Auth
        stubFor(post(urlPathMatching("/IdentityServer/connect/token"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"access_token\": \"mock-token\", \"expires_in\": 3600, \"token_type\": \"Bearer\"}")));

        // Stub Employees
        stubFor(get(urlPathMatching("/v2/companies/99999/employees"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[{\"employeeId\": \"EMP001\", \"firstName\": \"John\", \"lastName\": \"Doe\", \"statusCode\": \"A\", \"emailAddress\": \"john@example.com\"}]")));

        // Stub Codes
        stubFor(get(urlPathMatching("/v2/companies/99999/codes/earnings"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[{\"code\": \"E001\", \"description\": \"Regular Pay\"}, {\"code\": \"E002\", \"description\": \"Overtime\"}]")));

        // Stub Submit
        stubFor(post(urlPathMatching("/v2/companies/99999/payEntryImport"))
                .willReturn(aResponse()
                        .withStatus(202)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"timeImportFileTrackingId\": \"TRACK123\", \"status\": \"Pending\"}")));

        // Stub Status
        stubFor(get(urlPathMatching("/v2/companies/99999/payEntryImport/TRACK123"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"timeImportFileTrackingId\": \"TRACK123\", \"status\": \"Completed\"}")));
    }

    @Test
    void testFullPayrollFlow() {
        // 1. Sync Employees
        ResponseEntity<String> syncResp = restTemplate.postForEntity("/api/payroll/sync-employees", null, String.class);
        assertThat(syncResp.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Verify call to Paylocity
        verify(getRequestedFor(urlPathMatching("/v2/companies/99999/employees")));

        // 2. Submit Payroll
        PayrollInputDto input = new PayrollInputDto();
        input.setPayPeriodBegin(LocalDate.now().minusDays(14));
        input.setPayPeriodEnd(LocalDate.now());
        input.setCheckDate(LocalDate.now().plusDays(2));

        PayrollInputDto.PayrollEntryDto entry = new PayrollInputDto.PayrollEntryDto();
        entry.setEmployeeName("John Doe"); // Matches sync data
        entry.setEarningType("Regular"); // Should match "Regular Pay"
        entry.setHours(40.0);

        input.setEntries(List.of(entry));

        ResponseEntity<PayrollBatchEntity> submitResp = restTemplate.postForEntity("/api/payroll/submit", input, PayrollBatchEntity.class);

        assertThat(submitResp.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(submitResp.getBody()).isNotNull();
        assertThat(submitResp.getBody().getTrackingId()).isEqualTo("TRACK123");

        // Verify calls
        verify(getRequestedFor(urlPathMatching("/v2/companies/99999/codes/earnings")));
        verify(postRequestedFor(urlPathMatching("/v2/companies/99999/payEntryImport"))
                .withRequestBody(matchingJsonPath("$.importRecords[0].employeeId", equalTo("EMP001")))
                .withRequestBody(matchingJsonPath("$.importRecords[0].earnings[0].earningCode", equalTo("E001"))));

        // 3. Check Status
        ResponseEntity<PayrollBatchEntity> statusResp = restTemplate.getForEntity("/api/payroll/batch/TRACK123", PayrollBatchEntity.class);

        assertThat(statusResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(statusResp.getBody().getStatus()).isEqualTo("Completed");

        verify(getRequestedFor(urlPathMatching("/v2/companies/99999/payEntryImport/TRACK123")));
    }
}
