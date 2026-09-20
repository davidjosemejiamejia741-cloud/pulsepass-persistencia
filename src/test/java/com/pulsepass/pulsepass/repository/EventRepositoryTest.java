package com.pulsepass.pulsepass.repository;

import com.pulsepass.pulsepass.TestcontainersConfiguration;
import com.pulsepass.pulsepass.domain.Artist;
import com.pulsepass.pulsepass.domain.Event;
import com.pulsepass.pulsepass.domain.EventCategory;
import com.pulsepass.pulsepass.domain.EventStatus;
import com.pulsepass.pulsepass.domain.Venue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class EventRepositoryTest {

    @Autowired private EventRepository eventRepository;
    @Autowired private VenueRepository venueRepository;
    @Autowired private ArtistRepository artistRepository;
    @Autowired private JdbcTemplate jdbcTemplate;

    @Test
    void shouldExecuteEventQueriesWithOrderingAndDistinctResults() {
        Venue venue = saveVenue("VEN-BOG-01", "Bogota");
        Artist waveOne = artistRepository.saveAndFlush(new Artist("Wave One", "Colombia", "Pop", true));
        Artist waveTwo = artistRepository.saveAndFlush(new Artist("Wave Two", "Colombia", "Pop", true));
        Artist other = artistRepository.saveAndFlush(new Artist("Other Artist", "Colombia", "Rock", true));
        LocalDateTime baseDate = LocalDateTime.now().plusDays(10).withNano(0);
        Event early = saveEvent("EVT-BOG-01", EventStatus.PUBLISHED, baseDate, venue, Set.of(waveOne, waveTwo), null);
        Event late = saveEvent("EVT-BOG-02", EventStatus.PUBLISHED, baseDate.plusDays(1), venue, Set.of(other), null);
        saveEvent("EVT-BOG-03", EventStatus.DRAFT, baseDate.minusDays(1), venue, Set.of(waveOne), null);
        saveEvent("EVT-BOG-04", EventStatus.CANCELLED, baseDate.minusDays(2), venue, Set.of(waveOne), null);

        assertEquals(early.getId(), eventRepository.findByEventCode("EVT-BOG-01").orElseThrow().getId());
        assertEquals(List.of(early.getId(), late.getId()), eventRepository
                .findByStatusOrderByEventDateAsc(EventStatus.PUBLISHED).stream().map(Event::getId).toList());
        assertEquals(Set.of(early.getId(), late.getId()), eventRepository.findByVenueCode("VEN-BOG-01").stream()
                .filter(found -> found.getStatus() == EventStatus.PUBLISHED)
                .map(Event::getId).collect(java.util.stream.Collectors.toSet()));
        assertEquals(Set.of(early.getId(), eventRepository.findByEventCode("EVT-BOG-03").orElseThrow().getId(),
                        eventRepository.findByEventCode("EVT-BOG-04").orElseThrow().getId()),
                eventRepository.findEventsByArtist("Wave One").stream()
                        .map(Event::getId).collect(java.util.stream.Collectors.toSet()));
        assertEquals(Set.of(early.getId(), eventRepository.findByEventCode("EVT-BOG-03").orElseThrow().getId(),
                        eventRepository.findByEventCode("EVT-BOG-04").orElseThrow().getId()),
                eventRepository.findEventsByCityAndArtist("Bogota", "Wave One").stream()
                        .map(Event::getId).collect(java.util.stream.Collectors.toSet()));
        assertEquals(List.of(early.getId()), eventRepository.findRecommendedEvents(baseDate.minusDays(1), "Bogota", "wave")
                .stream().map(Event::getId).toList());
        assertNull(eventRepository.findByEventCode("EVT-BOG-01").orElseThrow().getStreamingUrl());
    }

    @Test
    void shouldRejectDuplicatedEventCode() {
        Venue venue = saveVenue("VEN-DUP-01", "Cali");
        saveEvent("EVT-DUP-01", EventStatus.PUBLISHED, LocalDateTime.now().plusDays(1), venue, Set.of(), null);

        assertThrows(DataIntegrityViolationException.class, () -> saveEvent("EVT-DUP-01", EventStatus.DRAFT,
                LocalDateTime.now().plusDays(2), venue, Set.of(), null));
    }

    @Test
    void shouldRequireVenueForEvent() {
        assertThrows(DataIntegrityViolationException.class, () -> eventRepository.saveAndFlush(new Event(
                "EVT-NO-VENUE", "Invalid", null, EventCategory.MUSIC, EventStatus.DRAFT,
                LocalDateTime.now(), 0, null, null, Set.of(), Set.of())));
    }

    @Test
    void shouldLimitStreamingUrlLength() {
        Venue venue = saveVenue("VEN-URL-01", "Cali");
        assertThrows(DataIntegrityViolationException.class, () -> saveEvent("EVT-LONG-URL", EventStatus.DRAFT,
                LocalDateTime.now().plusDays(1), venue, Set.of(), "a".repeat(501)));
    }

    @Test
    void shouldRejectDuplicatedEventArtistPair() {
        Venue venue = saveVenue("VEN-ART-01", "Cali");
        Artist artist = artistRepository.saveAndFlush(new Artist("Unique Pair Artist", "Colombia", "Jazz", true));
        Event event = saveEvent("EVT-ART-01", EventStatus.PUBLISHED, LocalDateTime.now().plusDays(1), venue,
                Set.of(artist), null);

        assertThrows(DataIntegrityViolationException.class, () -> jdbcTemplate.update(
                "INSERT INTO event_artists (event_id, artist_id) VALUES (?, ?)", event.getId(), artist.getId()));
    }

    @Test
    void shouldRejectInvalidEventEnumValueInPostgres() {
        Venue venue = saveVenue("VEN-ENUM-01", "Cali");

        assertThrows(DataIntegrityViolationException.class, () -> jdbcTemplate.update("""
                INSERT INTO events (event_code, name, category, status, event_date, minimum_age, venue_id)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """, "EVT-INVALID-ENUM", "Invalid", "INVALID", "PUBLISHED",
                Timestamp.valueOf(LocalDateTime.now()), 0, venue.getId()));
    }

    private Venue saveVenue(String code, String city) {
        return venueRepository.saveAndFlush(new Venue(code, "Venue " + code, city, "Address", 1000, true, List.of()));
    }

    private Event saveEvent(String code, EventStatus status, LocalDateTime date, Venue venue,
                            Set<Artist> artists, String streamingUrl) {
        return eventRepository.saveAndFlush(new Event(code, "Event " + code, null, EventCategory.MUSIC,
                status, date, 0, streamingUrl, venue, artists, Set.of()));
    }
}
