package com.czen.payroll_automation.service;

import com.czen.payroll_automation.config.PaylocityProperties;
import com.czen.payroll_automation.dto.TokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.Base64;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaylocityAuthService {

    private final PaylocityProperties properties;
    private final RestClient.Builder restClientBuilder;

    private String accessToken;
    private Instant tokenExpiry = Instant.MIN;
    private final Lock lock = new ReentrantLock();

    public String getAccessToken() {
        // Double-checked locking pattern could be used but standard lock is simpler given low contention
        lock.lock();
        try {
            // Check if token is valid with a 5-minute (300s) buffer
            if (accessToken != null && Instant.now().isBefore(tokenExpiry.minusSeconds(300))) {
                return accessToken;
            }
            return refreshAccessToken();
        } finally {
            lock.unlock();
        }
    }

    private String refreshAccessToken() {
        log.info("Refreshing Paylocity Access Token...");

        String credentials = properties.getClientId() + ":" + properties.getClientSecret();
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("scope", "WebLinkAPI");

        try {
            TokenResponse response = restClientBuilder.build()
                    .post()
                    .uri(properties.getBaseUrl() + "/IdentityServer/connect/token")
                    .header("Authorization", "Basic " + encodedCredentials)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(body)
                    .retrieve()
                    .body(TokenResponse.class);

            if (response != null && response.getAccess_token() != null) {
                this.accessToken = response.getAccess_token();
                this.tokenExpiry = Instant.now().plusSeconds(response.getExpires_in());
                log.info("Token refreshed successfully. Expires in {} seconds.", response.getExpires_in());
                return this.accessToken;
            }
        } catch (Exception e) {
            log.error("Failed to refresh token", e);
            throw new RuntimeException("Authentication failed: " + e.getMessage(), e);
        }

        throw new RuntimeException("Failed to retrieve access token: Response was null");
    }
}
