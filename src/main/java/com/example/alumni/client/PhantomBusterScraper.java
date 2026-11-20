package com.example.alumni.client;
import org.springframework.context.annotation.Profile;
import com.example.alumni.config.PhantomBusterProperties;
import com.example.alumni.exception.ScraperException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * PhantomBuster v2 integration: launch the "LinkedIn Search Export" agent,
 * poll until finished, then read the result object.
 */
@Component
@Profile("!mock")
public class PhantomBusterScraper implements ProfileScraper {

    private static final Logger log = LoggerFactory.getLogger(PhantomBusterScraper.class);
    private static final String KEY_HEADER = "X-Phantombuster-Key";

    private final PhantomBusterProperties props;
    private final RestClient http;
    private final ObjectMapper mapper;

    public PhantomBusterScraper(PhantomBusterProperties props, RestClient.Builder builder, ObjectMapper mapper) {
        this.props = props;
        this.mapper = mapper;
        this.http = builder.baseUrl(props.baseUrl()).defaultHeader(KEY_HEADER, props.apiKey()).build();
    }

    @Override
    public List<ScrapedProfile> search(String university, String designation, Integer passoutYear) {
        if (isBlank(props.apiKey()) || isBlank(props.agentId())) {
            throw new ScraperException("PhantomBuster is not configured (API key / agent id missing)");
        }
        try {
            String containerId = launch(buildSearchUrl(university, designation, passoutYear));
            awaitFinished();
            return parse(fetchResult(containerId));
        } catch (RestClientException e) {
            log.error("PhantomBuster call failed", e);
            throw new ScraperException("PhantomBuster request failed: " + e.getMessage(), e);
        }
    }

    static String buildSearchUrl(String university, String designation, Integer year) {
        String keywords = designation + " " + university + (year != null ? " " + year : "");
        return UriComponentsBuilder.fromHttpUrl("https://www.linkedin.com/search/results/people/")
                .queryParam("keywords", keywords).build().encode(StandardCharsets.UTF_8).toUriString();
    }

    private String launch(String searchUrl) {
        Map<String, Object> argument = Map.of(
                "search", searchUrl,
                "sessionCookie", props.linkedinSessionCookie(),
                "userAgent", props.userAgent(),
                "numberOfProfiles", props.maxProfiles());
        JsonNode res = http.post().uri("/agents/launch")
                .body(Map.of("id", props.agentId(), "argument", argument))
                .retrieve().body(JsonNode.class);
        if (res == null || !res.hasNonNull("containerId")) {
            throw new ScraperException("PhantomBuster did not return a containerId");
        }
        return res.get("containerId").asText();
    }

    private void awaitFinished() {
        for (int i = 0; i < props.maxPollAttempts(); i++) {
            JsonNode out = http.get().uri("/agents/fetch-output?id={id}", props.agentId())
                    .retrieve().body(JsonNode.class);
            String status = out == null ? "" : out.path("status").asText();
            if ("finished".equalsIgnoreCase(status)) return;
            sleep(props.pollIntervalMs());
        }
        throw new ScraperException("Timed out waiting for PhantomBuster agent to finish");
    }

    private String fetchResult(String containerId) {
        JsonNode res = http.get().uri("/containers/fetch-result-object?id={id}", containerId)
                .retrieve().body(JsonNode.class);
        return res == null ? null : res.path("resultObject").asText(null);
    }

    private List<ScrapedProfile> parse(String resultObject) {
        if (isBlank(resultObject) || "null".equals(resultObject)) return List.of();
        try {
            List<Map<String, Object>> rows = mapper.readValue(resultObject, new TypeReference<>() { });
            List<ScrapedProfile> profiles = new ArrayList<>();
            for (Map<String, Object> r : rows) {
                String url = str(r.get("profileUrl"));
                String name = str(r.get("fullName"));
                if (url == null || name == null) continue;      // skip unusable rows
                profiles.add(new ScrapedProfile(name, str(r.get("job")), str(r.get("company")),
                        str(r.get("location")), url));
            }
            return profiles;
        } catch (Exception e) {
            throw new ScraperException("Could not parse PhantomBuster result", e);
        }
    }

    private static String str(Object o) { return o == null || o.toString().isBlank() ? null : o.toString(); }
    private static boolean isBlank(String s) { return s == null || s.isBlank(); }

    private static void sleep(long ms) {
        try { Thread.sleep(ms); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new ScraperException("Interrupted while polling", e); }
    }
}
