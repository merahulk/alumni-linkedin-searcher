package com.example.alumni.service;

import com.example.alumni.client.ProfileScraper;
import com.example.alumni.client.ScrapedProfile;
import com.example.alumni.dto.AlumniResponse;
import com.example.alumni.dto.AlumniSearchRequest;
import com.example.alumni.entity.Alumni;
import com.example.alumni.repository.AlumniRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AlumniService {

    private final ProfileScraper scraper;
    private final AlumniRepository repository;

    public AlumniService(ProfileScraper scraper, AlumniRepository repository) {
        this.scraper = scraper;
        this.repository = repository;
    }

    /** Scrapes profiles, persists the ones not already stored, returns all scraped matches. */
    @Transactional
    public List<AlumniResponse> search(AlumniSearchRequest req) {
        List<ScrapedProfile> scraped = scraper.search(req.university(), req.designation(), req.passoutYear());
        if (scraped.isEmpty()) return List.of();

        Set<String> existing = repository
                .findByProfileUrlIn(scraped.stream().map(ScrapedProfile::profileUrl).toList())
                .stream().map(Alumni::getProfileUrl).collect(Collectors.toSet());

        List<Alumni> all = scraped.stream().map(p -> toEntity(p, req)).toList();
        repository.saveAll(all.stream().filter(a -> !existing.contains(a.getProfileUrl())).toList());

        return all.stream().map(AlumniResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<AlumniResponse> findAll() {
        return repository.findAll().stream().map(AlumniResponse::from).toList();
    }

    private Alumni toEntity(ScrapedProfile p, AlumniSearchRequest req) {
        String headline = p.company() == null ? p.role()
                : (p.role() == null ? p.company() : p.role() + " at " + p.company());
        return new Alumni(p.name(), p.role(), req.university(), p.location(),
                headline, req.passoutYear(), p.profileUrl());
    }
}
