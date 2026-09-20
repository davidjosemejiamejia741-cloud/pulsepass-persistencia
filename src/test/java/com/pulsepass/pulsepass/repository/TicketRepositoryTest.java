package com.pulsepass.pulsepass.repository;

import com.pulsepass.pulsepass.TestcontainersConfiguration;
import com.pulsepass.pulsepass.domain.Event;
import com.pulsepass.pulsepass.domain.EventCategory;
import com.pulsepass.pulsepass.domain.EventStatus;
import com.pulsepass.pulsepass.domain.Ticket;
import com.pulsepass.pulsepass.domain.TicketStatus;
import com.pulsepass.pulsepass.domain.TicketType;
import com.pulsepass.pulsepass.domain.User;
import com.pulsepass.pulsepass.domain.Venue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class TicketRepositoryTest {

    @Autowired private TicketRepository ticketRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private VenueRepository venueRepository;
    @Autowired private EventRepository eventRepository;

    @Test
    void shouldFindAndCountPaidTickets() {
        User user = userRepository.saveAndFlush(new User("andrea", "Andrea@Example.com", true, null, List.of()));
        Venue venue = venueRepository.saveAndFlush(new Venue("VEN-TKT-01", "Theatre", "Cali", "Street 2", 100, true, List.of()));
        LocalDateTime eventDate = LocalDateTime.now().plusDays(5).withNano(0);
        Event event = eventRepository.saveAndFlush(new Event("EVT-TKT-01", "Ticket event", null,
                EventCategory.MUSIC, EventStatus.PUBLISHED, eventDate, 0, null, venue, Set.of(), Set.of()));
        Ticket paid = ticketRepository.saveAndFlush(new Ticket("TCK-PAID-01", TicketType.VIP,
                new BigDecimal("250000.00"), TicketStatus.PAID, LocalDateTime.now(), user, event));
        ticketRepository.saveAndFlush(new Ticket("TCK-RES-01", TicketType.GENERAL,
                new BigDecimal("120000.00"), TicketStatus.RESERVED, LocalDateTime.now(), user, event));

        assertEquals(List.of(paid.getId()), ticketRepository.findByUserEmailIgnoreCaseAndStatus("andrea@example.com", TicketStatus.PAID)
                .stream().map(Ticket::getId).toList());
        assertEquals(1, ticketRepository.findByEventEventCodeAndStatus("EVT-TKT-01", TicketStatus.PAID).size());
        assertEquals(1, ticketRepository.countPaidTicketsByEventCode("EVT-TKT-01"));
        assertTrue(ticketRepository.findByEventEventDateAfterOrderByEventEventDateAsc(eventDate.minusDays(1))
                .stream().anyMatch(found -> found.getId().equals(paid.getId())));
    }

    @Test
    void shouldRejectNegativeTicketPrice() {
        User user = userRepository.saveAndFlush(new User("price-user", "price@example.com", true, null, List.of()));
        Venue venue = venueRepository.saveAndFlush(new Venue("VEN-PRICE-01", "Theatre", "Cali", "Street 2", 100, true, List.of()));
        Event event = eventRepository.saveAndFlush(new Event("EVT-PRICE-01", "Price event", null,
                EventCategory.MUSIC, EventStatus.PUBLISHED, LocalDateTime.now().plusDays(1), 0, null,
                venue, Set.of(), Set.of()));

        assertThrows(DataIntegrityViolationException.class, () -> ticketRepository.saveAndFlush(
                new Ticket("TCK-NEG-01", TicketType.GENERAL, new BigDecimal("-1.00"), TicketStatus.PAID,
                        LocalDateTime.now(), user, event)));
    }
}
