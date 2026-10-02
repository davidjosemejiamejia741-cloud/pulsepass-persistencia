package com.pulsepass.pulsepass.repository;

import com.pulsepass.pulsepass.TestcontainersConfiguration;
import com.pulsepass.pulsepass.domain.Venue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class VenueRepositoryTest {

    @Autowired private VenueRepository venueRepository;

    @Test
    void shouldFindVenueByBusinessCode() {
        Venue venue = venueRepository.saveAndFlush(new Venue("VEN-SMR-01", "Marina Convention Center",
                "Santa Marta", "Avenida del Mar", 5000, true, List.of()));

        assertEquals(venue.getId(), venueRepository.findByCode("VEN-SMR-01").orElseThrow().getId());
    }

    @Test
    void shouldRejectNonPositiveCapacity() {
        assertThrows(DataIntegrityViolationException.class, () -> venueRepository.saveAndFlush(
                new Venue("VEN-INVALID-01", "Invalid", "Cali", "Street 3", 0, true, List.of())));
    }

    @Test
    void shouldRejectNegativeCapacity() {
        assertThrows(DataIntegrityViolationException.class, () -> venueRepository.saveAndFlush(
                new Venue("VEN-NEGATIVE-01", "Invalid", "Cali", "Street 3", -1, true, List.of())));
    }

    @Test
    void shouldFindOnlyActiveVenuesOrderedByName() {
    venueRepository.saveAndFlush(
            new Venue(
                    "VEN-ACTIVE-02",
                    "Zulu Arena",
                    "Santa Marta",
                    "Street 1",
                    3000,
                    true,
                    List.of()
            )
    );

    venueRepository.saveAndFlush(
            new Venue(
                    "VEN-INACTIVE-01",
                    "Inactive Arena",
                    "Santa Marta",
                    "Street 2",
                    3000,
                    false,
                    List.of()
            )
    );

    venueRepository.saveAndFlush(
            new Venue(
                    "VEN-ACTIVE-01",
                    "Alpha Arena",
                    "Santa Marta",
                    "Street 3",
                    3000,
                    true,
                    List.of()
            )
    );

    List<Venue> venues =
            venueRepository.findByActiveTrueOrderByNameAsc();

    assertEquals(2, venues.size());
    assertEquals("Alpha Arena", venues.get(0).getName());
    assertEquals("Zulu Arena", venues.get(1).getName());
}
}
