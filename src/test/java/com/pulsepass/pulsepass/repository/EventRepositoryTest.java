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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class EventRepositoryTest {

    @Autowired private EventRepository eventRepository;
    @Autowired private VenueRepository venueRepository;
    @Autowired private ArtistRepository artistRepository;

    @Test
    void shouldExecuteEventQueriesAndPersistArtistsRelation() {
        Venue venue = venueRepository.saveAndFlush(new Venue("VEN-BOG-01", "Arena", "Bogota",
                "Calle 1", 1000, true, List.of()));
        Artist artist = artistRepository.saveAndFlush(new Artist("Test Waves", "Colombia", "Pop", true));
        LocalDateTime date = LocalDateTime.now().plusDays(10).withNano(0);
        Event event = eventRepository.saveAndFlush(new Event("EVT-BOG-01", "Test concert", null,
                EventCategory.MUSIC, EventStatus.PUBLISHED, date, 18, null, venue,
                Set.of(artist), Set.of()));

        assertEquals(event.getId(), eventRepository.findByEventCode("EVT-BOG-01").orElseThrow().getId());
        assertEquals(1, eventRepository.findByStatusOrderByEventDateAsc(EventStatus.PUBLISHED).stream()
                .filter(found -> found.getId().equals(event.getId())).count());
        assertEquals(List.of(event.getId()), eventRepository.findByVenueCode("VEN-BOG-01").stream()
                .map(Event::getId).toList());
        assertEquals(List.of(event.getId()), eventRepository.findEventsByArtist("Test Waves").stream()
                .map(Event::getId).toList());
        assertEquals(List.of(event.getId()), eventRepository.findEventsByCityAndArtist("Bogota", "Test Waves").stream()
                .map(Event::getId).toList());
        assertTrue(eventRepository.findRecommendedEvents(date.minusDays(1), "Bogota", "waves")
                .stream().anyMatch(found -> found.getId().equals(event.getId())));
    }
}
