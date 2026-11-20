package com.example.alumni.client;

import java.util.List;

/** Strategy interface: keeps the service independent of the scraping provider. */
public interface ProfileScraper {
    List<ScrapedProfile> search(String university, String designation, Integer passoutYear);
}
