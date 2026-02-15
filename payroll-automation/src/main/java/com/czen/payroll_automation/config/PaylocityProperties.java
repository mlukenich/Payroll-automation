package com.czen.payroll_automation.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "paylocity")
public class PaylocityProperties {

    private String clientId;
    private String clientSecret;
    private String companyId;
    private String environment = "sandbox"; // Default to sandbox
    private String baseUrl;

    public String getBaseUrl() {
        if (baseUrl != null && !baseUrl.isEmpty()) {
            return baseUrl;
        }
        return "production".equalsIgnoreCase(environment)
                ? "https://api.paylocity.com"
                : "https://apisandbox.paylocity.com";
    }
}
