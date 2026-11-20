package com.example.alumni.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "phantombuster")
public record PhantomBusterProperties(
        String baseUrl,
        String apiKey,
        String agentId,
        String linkedinSessionCookie,
        String userAgent,
        int maxProfiles,
        long pollIntervalMs,
        int maxPollAttempts) {
}
