package com.example.alumni.service;

import com.example.alumni.client.ProfileScraper;
import com.example.alumni.client.ScrapedProfile;
import com.example.alumni.dto.AlumniResponse;
import com.example.alumni.dto.AlumniSearchRequest;
import com.example.alumni.entity.Alumni;
import com.example.alumni.repository.AlumniRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlumniServiceTest {

    @Mock ProfileScraper scraper;
    @Mock AlumniRepository repository;

    private AlumniService service() { return new AlumniService(scraper, repository); }

    private final AlumniSearchRequest req = new AlumniSearchRequest("XYZ Univ", "Engineer", 2020);

    @Test
    @SuppressWarnings("unchecked")
    void savesOnlyNewProfilesAndReturnsAllMatches() {
        when(scraper.search("XYZ Univ", "Engineer", 2020)).thenReturn(List.of(
                new ScrapedProfile("John", "Engineer", "Acme", "NY", "u1"),
                new ScrapedProfile("Jane", "Engineer", null, "SF", "u2")));
        when(repository.findByProfileUrlIn(any())).thenReturn(
                List.of(new Alumni("John", "Engineer", "XYZ Univ", "NY", "x", 2020, "u1")));

        List<AlumniResponse> result = service().search(req);

        assertEquals(2, result.size());
        assertEquals("Engineer at Acme", result.get(0).linkedinHeadline());
        ArgumentCaptor<List<Alumni>> captor = ArgumentCaptor.forClass(List.class);
        verify(repository).saveAll(captor.capture());
        assertEquals(1, captor.getValue().size());
        assertEquals("u2", captor.getValue().get(0).getProfileUrl());
    }

    @Test
    void returnsEmptyAndSkipsDbWhenNothingScraped() {
        when(scraper.search(any(), any(), any())).thenReturn(List.of());
        assertTrue(service().search(req).isEmpty());
        verifyNoInteractions(repository);
    }

    @Test
    void findAllMapsEntities() {
        when(repository.findAll()).thenReturn(List.of(new Alumni("A", "R", "U", "L", "H", 2019, "u")));
        assertEquals("A", service().findAll().get(0).name());
    }
}
