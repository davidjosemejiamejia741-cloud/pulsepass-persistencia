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
import org.springframework.jdbc.core.JdbcTemplate;

import jakarta.persistence.EntityManager;

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
    @Autowired private EntityManager entityManager;
    @Autowired private JdbcTemplate jdbcTemplate;

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

        assertEquals(2, ticketRepository.findByUserEmailIgnoreCase("andrea@example.com").size());
        assertEquals(List.of(paid.getId()), ticketRepository.findByUserEmailIgnoreCaseAndStatus("andrea@example.com", TicketStatus.PAID)
                .stream().map(Ticket::getId).toList());
        assertEquals(1, ticketRepository.findByEventEventCodeAndStatus("EVT-TKT-01", TicketStatus.PAID).size());
        assertEquals(1,ticketRepository.countByEventEventCodeAndStatus("EVT-TKT-01",TicketStatus.PAID)
);
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

    @Test
    void shouldAllowZeroTicketPriceAndRejectDuplicatedCode() {
        User user = saveUser("zero-user", "zero@example.com");
        Event event = saveEvent("EVT-ZERO-01");
        ticketRepository.saveAndFlush(new Ticket("TCK-ZERO-01", TicketType.GENERAL, BigDecimal.ZERO,
                TicketStatus.RESERVED, LocalDateTime.now(), user, event));

        assertThrows(DataIntegrityViolationException.class, () -> ticketRepository.saveAndFlush(
                new Ticket("TCK-ZERO-01", TicketType.VIP, BigDecimal.ONE, TicketStatus.PAID,
                        LocalDateTime.now(), user, event)));
    }

    @Test
    void shouldRequireUserForTicket() {
        Event event = saveEvent("EVT-NO-USER-01");

        assertThrows(DataIntegrityViolationException.class, () -> ticketRepository.saveAndFlush(
                new Ticket("TCK-NO-USER-01", TicketType.GENERAL, BigDecimal.ONE, TicketStatus.PAID,
                        LocalDateTime.now(), null, event)));
    }

    @Test
    void shouldRequireEventForTicket() {
        User user = saveUser("no-event-user", "no-event@example.com");

        assertThrows(DataIntegrityViolationException.class, () -> ticketRepository.saveAndFlush(
                new Ticket("TCK-NO-EVENT-01", TicketType.GENERAL, BigDecimal.ONE, TicketStatus.PAID,
                        LocalDateTime.now(), user, null)));
    }

    @Test
    void shouldRejectTicketWithUnknownUserForeignKey() {
        Event event = saveEvent("EVT-UNKNOWN-USER-01");
        User unknownUser = entityManager.getReference(User.class, 999_999L);

        assertThrows(DataIntegrityViolationException.class, () -> ticketRepository.saveAndFlush(
                new Ticket("TCK-UNKNOWN-USER-01", TicketType.GENERAL, BigDecimal.ONE, TicketStatus.PAID,
                        LocalDateTime.now(), unknownUser, event)));
    }

    @Test
    void shouldRejectInvalidTicketEnumValueInPostgres() {
        User user = saveUser("enum-user", "enum@example.com");
        Event event = saveEvent("EVT-ENUM-01");

        assertThrows(DataIntegrityViolationException.class, () -> jdbcTemplate.update("""
                INSERT INTO tickets (ticket_code, type, price, status, purchase_date, user_id, event_id)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """, "TCK-INVALID-ENUM", "INVALID", BigDecimal.ONE, "PAID", LocalDateTime.now(),
                user.getId(), event.getId()));
    }

    private User saveUser(String username, String email) {
        return userRepository.saveAndFlush(new User(username, email, true, null, List.of()));
    }

    private Event saveEvent(String eventCode) {
        Venue venue = venueRepository.saveAndFlush(new Venue("VEN-" + eventCode, "Theatre", "Cali", "Street 2",
                100, true, List.of()));
        return eventRepository.saveAndFlush(new Event(eventCode, "Ticket event", null, EventCategory.MUSIC,
                EventStatus.PUBLISHED, LocalDateTime.now().plusDays(5), 0, null, venue, Set.of(), Set.of()));
    }
}
