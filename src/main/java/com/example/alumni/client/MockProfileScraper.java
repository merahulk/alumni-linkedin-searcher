package com.example.alumni.client;



import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@Profile("mock")
public class MockProfileScraper implements ProfileScraper {

    @Override
    public List<ScrapedProfile> search(String university, String designation, Integer passoutYear) {
        int id = Math.abs((university + designation + passoutYear).hashCode());
        return List.of(
                new ScrapedProfile("John Doe", designation, "XYZ Corp", "New York, NY",
                        "https://www.linkedin.com/in/mock-john-" + id),
                new ScrapedProfile("Jane Smith", designation, "ABC Labs", "San Francisco, CA",
                        "https://www.linkedin.com/in/mock-jane-" + id));
    }
}

